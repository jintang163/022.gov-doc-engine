package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocSupervisionDTO;
import com.gov.doc.engine.dto.DocSupervisionQueryDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.entity.DocSupervision;
import com.gov.doc.engine.entity.DocUrgeLog;
import com.gov.doc.engine.enums.DocHandlingStatusEnum;
import com.gov.doc.engine.enums.DocIncomingStatusEnum;
import com.gov.doc.engine.enums.SupervisionStatusEnum;
import com.gov.doc.engine.enums.SupervisionTypeEnum;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocIncomingMapper;
import com.gov.doc.engine.mapper.DocSupervisionMapper;
import com.gov.doc.engine.mapper.DocUrgeLogMapper;
import com.gov.doc.engine.service.DocSupervisionService;
import com.gov.doc.engine.vo.DocIncomingVO;
import com.gov.doc.engine.vo.DocSupervisionVO;
import com.gov.doc.engine.vo.DocUrgeLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocSupervisionServiceImpl extends ServiceImpl<DocSupervisionMapper, DocSupervision> implements DocSupervisionService {

    @Autowired
    private DocSupervisionMapper docSupervisionMapper;

    @Autowired
    private DocIncomingMapper docIncomingMapper;

    @Autowired
    private DocHandlingMapper docHandlingMapper;

    @Autowired
    private DocUrgeLogMapper docUrgeLogMapper;

    @Override
    public PageResult<DocSupervisionVO> pageList(DocSupervisionQueryDTO queryDTO) {
        LambdaQueryWrapper<DocSupervision> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(DocSupervision::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        Page<DocSupervision> page = new Page<>(pageNum, pageSize);
        Page<DocSupervision> resultPage = docSupervisionMapper.selectPage(page, queryWrapper);

        List<DocSupervisionVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public DocSupervisionVO getDetail(Long id) {
        DocSupervision supervision = docSupervisionMapper.selectById(id);
        if (supervision == null) {
            throw new RuntimeException("督办单不存在");
        }
        return convertToVO(supervision);
    }

    @Override
    public List<DocSupervisionVO> identifyTimeoutDocs() {
        LambdaQueryWrapper<DocHandling> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocHandling::getStatus, DocHandlingStatusEnum.PENDING.getCode())
                .le(DocHandling::getDeadline, LocalDate.now())
                .isNotNull(DocHandling::getDeadline);

        List<DocHandling> timeoutHandlings = docHandlingMapper.selectList(queryWrapper);
        List<DocSupervisionVO> result = new ArrayList<>();

        for (DocHandling handling : timeoutHandlings) {
            LambdaQueryWrapper<DocSupervision> existingWrapper = new LambdaQueryWrapper<>();
            existingWrapper.eq(DocSupervision::getIncomingId, handling.getIncomingId())
                    .eq(DocSupervision::getHandlingId, handling.getId())
                    .eq(DocSupervision::getSupervisionType, SupervisionTypeEnum.TIMEOUT.getCode())
                    .ne(DocSupervision::getStatus, SupervisionStatusEnum.CLOSED.getCode());
            Long existingCount = docSupervisionMapper.selectCount(existingWrapper);
            if (existingCount > 0) {
                continue;
            }

            DocSupervisionVO vo = new DocSupervisionVO();
            DocIncoming incoming = docIncomingMapper.selectById(handling.getIncomingId());
            if (incoming != null) {
                vo.setIncomingId(incoming.getId());
                vo.setSourceDocNumber(incoming.getSourceDocNumber());
                vo.setDocTitle(incoming.getDocTitle());
            }
            vo.setHandlingId(handling.getId());
            vo.setSupervisionType(SupervisionTypeEnum.TIMEOUT.getCode());
            vo.setSupervisionTypeName(SupervisionTypeEnum.TIMEOUT.getName());
            vo.setResponsibleUserId(handling.getTargetUserId());
            vo.setResponsibleUserName(handling.getTargetUserName());
            vo.setResponsibleDeptId(handling.getTargetDeptId());
            vo.setResponsibleDeptName(handling.getTargetDeptName());
            vo.setDeadline(handling.getDeadline());

            long overdueDays = ChronoUnit.DAYS.between(handling.getDeadline(), LocalDate.now());
            vo.setOverdueDays((int) overdueDays);

            LambdaQueryWrapper<DocUrgeLog> urgeWrapper = new LambdaQueryWrapper<>();
            urgeWrapper.eq(DocUrgeLog::getIncomingId, handling.getIncomingId());
            Long urgeCount = docUrgeLogMapper.selectCount(urgeWrapper);
            vo.setUrgeCount(urgeCount.intValue());

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<DocSupervisionVO> identifyUrgeOverdueDocs() {
        LambdaQueryWrapper<DocUrgeLog> urgeWrapper = new LambdaQueryWrapper<>();
        urgeWrapper.groupBy(DocUrgeLog::getIncomingId);
        List<DocUrgeLog> allUrges = docUrgeLogMapper.selectList(null);

        java.util.Map<Long, Long> urgeCountMap = allUrges.stream()
                .collect(java.util.stream.Collectors.groupingBy(DocUrgeLog::getIncomingId, java.util.stream.Collectors.counting()));

        List<DocSupervisionVO> result = new ArrayList<>();
        for (java.util.Map.Entry<Long, Long> entry : urgeCountMap.entrySet()) {
            if (entry.getValue() < 3) {
                continue;
            }

            Long incomingId = entry.getKey();
            LambdaQueryWrapper<DocSupervision> existingWrapper = new LambdaQueryWrapper<>();
            existingWrapper.eq(DocSupervision::getIncomingId, incomingId)
                    .eq(DocSupervision::getSupervisionType, SupervisionTypeEnum.URGE_OVERDUE.getCode())
                    .ne(DocSupervision::getStatus, SupervisionStatusEnum.CLOSED.getCode());
            Long existingCount = docSupervisionMapper.selectCount(existingWrapper);
            if (existingCount > 0) {
                continue;
            }

            DocIncoming incoming = docIncomingMapper.selectById(incomingId);
            if (incoming == null) {
                continue;
            }
            if (DocIncomingStatusEnum.COMPLETED.getCode().equals(incoming.getStatus())) {
                continue;
            }

            LambdaQueryWrapper<DocHandling> handlingWrapper = new LambdaQueryWrapper<>();
            handlingWrapper.eq(DocHandling::getIncomingId, incomingId)
                    .eq(DocHandling::getStatus, DocHandlingStatusEnum.PENDING.getCode())
                    .orderByDesc(DocHandling::getCreateTime)
                    .last("LIMIT 1");
            DocHandling handling = docHandlingMapper.selectOne(handlingWrapper);

            DocSupervisionVO vo = new DocSupervisionVO();
            vo.setIncomingId(incomingId);
            vo.setSourceDocNumber(incoming.getSourceDocNumber());
            vo.setDocTitle(incoming.getDocTitle());
            vo.setSupervisionType(SupervisionTypeEnum.URGE_OVERDUE.getCode());
            vo.setSupervisionTypeName(SupervisionTypeEnum.URGE_OVERDUE.getName());
            vo.setUrgeCount(entry.getValue().intValue());

            if (handling != null) {
                vo.setHandlingId(handling.getId());
                vo.setResponsibleUserId(handling.getTargetUserId());
                vo.setResponsibleUserName(handling.getTargetUserName());
                vo.setResponsibleDeptId(handling.getTargetDeptId());
                vo.setResponsibleDeptName(handling.getTargetDeptName());
                vo.setDeadline(handling.getDeadline());
                if (handling.getDeadline() != null) {
                    long overdueDays = ChronoUnit.DAYS.between(handling.getDeadline(), LocalDate.now());
                    vo.setOverdueDays(Math.max((int) overdueDays, 0));
                }
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public DocSupervisionVO generateSupervision(DocSupervisionDTO dto) {
        DocIncoming incoming = docIncomingMapper.selectById(dto.getIncomingId());
        if (incoming == null) {
            throw new RuntimeException("收文记录不存在");
        }

        DocSupervision supervision = new DocSupervision();
        supervision.setSupervisionNo(BizNoGenerator.generateSupervisionNo());
        supervision.setIncomingId(dto.getIncomingId());
        supervision.setHandlingId(dto.getHandlingId());
        supervision.setSourceDocNumber(incoming.getSourceDocNumber());
        supervision.setDocTitle(incoming.getDocTitle());
        supervision.setSupervisionType(dto.getSupervisionType());
        supervision.setResponsibleUserId(dto.getResponsibleUserId());
        supervision.setResponsibleUserName(dto.getResponsibleUserName());
        supervision.setResponsibleDeptId(dto.getResponsibleDeptId());
        supervision.setResponsibleDeptName(dto.getResponsibleDeptName());
        supervision.setLeaderId(dto.getLeaderId());
        supervision.setLeaderName(dto.getLeaderName());

        if (dto.getHandlingId() != null) {
            DocHandling handling = docHandlingMapper.selectById(dto.getHandlingId());
            if (handling != null) {
                supervision.setDeadline(handling.getDeadline());
                if (handling.getDeadline() != null) {
                    long overdueDays = ChronoUnit.DAYS.between(handling.getDeadline(), LocalDate.now());
                    supervision.setOverdueDays(Math.max((int) overdueDays, 0));
                }
                if (!StringUtils.hasText(dto.getResponsibleUserId())) {
                    supervision.setResponsibleUserId(handling.getTargetUserId());
                    supervision.setResponsibleUserName(handling.getTargetUserName());
                    supervision.setResponsibleDeptId(handling.getTargetDeptId());
                    supervision.setResponsibleDeptName(handling.getTargetDeptName());
                }
            }
        }

        LambdaQueryWrapper<DocUrgeLog> urgeWrapper = new LambdaQueryWrapper<>();
        urgeWrapper.eq(DocUrgeLog::getIncomingId, dto.getIncomingId());
        Long urgeCount = docUrgeLogMapper.selectCount(urgeWrapper);
        supervision.setUrgeCount(urgeCount.intValue());

        supervision.setSupervisionContent(dto.getSupervisionContent());
        supervision.setPushStatus("pending");
        supervision.setStatus(SupervisionStatusEnum.GENERATED.getCode());
        supervision.setRemark(dto.getRemark());

        docSupervisionMapper.insert(supervision);
        return convertToVO(supervision);
    }

    @Override
    public List<DocSupervisionVO> batchGenerateSupervision(List<DocSupervisionDTO> dtoList) {
        List<DocSupervisionVO> result = new ArrayList<>();
        for (DocSupervisionDTO dto : dtoList) {
            result.add(generateSupervision(dto));
        }
        return result;
    }

    @Override
    public DocSupervisionVO pushToLeader(Long id) {
        DocSupervision supervision = docSupervisionMapper.selectById(id);
        if (supervision == null) {
            throw new RuntimeException("督办单不存在");
        }
        if (!SupervisionStatusEnum.GENERATED.getCode().equals(supervision.getStatus())
                && !SupervisionStatusEnum.NOTIFIED.getCode().equals(supervision.getStatus())) {
            throw new RuntimeException("当前督办单状态不允许推送");
        }

        LambdaUpdateWrapper<DocSupervision> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocSupervision::getId, id)
                .set(DocSupervision::getPushStatus, "pushed")
                .set(DocSupervision::getPushTime, LocalDateTime.now())
                .set(DocSupervision::getStatus, SupervisionStatusEnum.NOTIFIED.getCode());
        docSupervisionMapper.update(null, updateWrapper);

        return convertToVO(docSupervisionMapper.selectById(id));
    }

    @Override
    public List<DocSupervisionVO> batchPushToLeader(List<Long> ids) {
        List<DocSupervisionVO> result = new ArrayList<>();
        for (Long id : ids) {
            result.add(pushToLeader(id));
        }
        return result;
    }

    @Override
    public DocSupervisionVO updateStatus(Long id, String status) {
        DocSupervision supervision = docSupervisionMapper.selectById(id);
        if (supervision == null) {
            throw new RuntimeException("督办单不存在");
        }

        LambdaUpdateWrapper<DocSupervision> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocSupervision::getId, id)
                .set(DocSupervision::getStatus, status);

        if (SupervisionStatusEnum.COMPLETED.getCode().equals(status)) {
            updateWrapper.set(DocSupervision::getCompleteTime, LocalDateTime.now());
        }

        docSupervisionMapper.update(null, updateWrapper);
        return convertToVO(docSupervisionMapper.selectById(id));
    }

    @Override
    public DocSupervisionVO completeSupervision(Long id) {
        return updateStatus(id, SupervisionStatusEnum.COMPLETED.getCode());
    }

    @Override
    public PageResult<DocSupervisionVO> getMySupervisions(DocSupervisionQueryDTO queryDTO) {
        UserContext currentUser = UserContext.getCurrentUser();
        if (queryDTO == null) {
            queryDTO = new DocSupervisionQueryDTO();
        }
        queryDTO.setLeaderId(String.valueOf(currentUser.getUserId()));
        return pageList(queryDTO);
    }

    private LambdaQueryWrapper<DocSupervision> buildQueryWrapper(DocSupervisionQueryDTO queryDTO) {
        LambdaQueryWrapper<DocSupervision> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getIncomingId() != null) {
            queryWrapper.eq(DocSupervision::getIncomingId, queryDTO.getIncomingId());
        }
        if (StringUtils.hasText(queryDTO.getSupervisionType())) {
            queryWrapper.eq(DocSupervision::getSupervisionType, queryDTO.getSupervisionType());
        }
        if (StringUtils.hasText(queryDTO.getResponsibleUserId())) {
            queryWrapper.eq(DocSupervision::getResponsibleUserId, queryDTO.getResponsibleUserId());
        }
        if (StringUtils.hasText(queryDTO.getResponsibleDeptId())) {
            queryWrapper.eq(DocSupervision::getResponsibleDeptId, queryDTO.getResponsibleDeptId());
        }
        if (StringUtils.hasText(queryDTO.getLeaderId())) {
            queryWrapper.eq(DocSupervision::getLeaderId, queryDTO.getLeaderId());
        }
        if (StringUtils.hasText(queryDTO.getPushStatus())) {
            queryWrapper.eq(DocSupervision::getPushStatus, queryDTO.getPushStatus());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(DocSupervision::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.and(w -> w.like(DocSupervision::getSourceDocNumber, queryDTO.getKeyword())
                    .or().like(DocSupervision::getDocTitle, queryDTO.getKeyword())
                    .or().like(DocSupervision::getSupervisionNo, queryDTO.getKeyword()));
        }

        return queryWrapper;
    }

    private DocSupervisionVO convertToVO(DocSupervision supervision) {
        DocSupervisionVO vo = new DocSupervisionVO();
        BeanUtils.copyProperties(supervision, vo);
        vo.setStatusName(SupervisionStatusEnum.getNameByCode(supervision.getStatus()));
        vo.setSupervisionTypeName(SupervisionTypeEnum.getNameByCode(supervision.getSupervisionType()));
        vo.setPushStatusName(getPushStatusName(supervision.getPushStatus()));

        DocIncoming incoming = docIncomingMapper.selectById(supervision.getIncomingId());
        if (incoming != null) {
            DocIncomingVO incomingVO = new DocIncomingVO();
            BeanUtils.copyProperties(incoming, incomingVO);
            incomingVO.setStatusName(DocIncomingStatusEnum.getNameByCode(incoming.getStatus()));
            incomingVO.setSourceName("manual".equals(incoming.getSource()) ? "手动录入" : "接口接入");
            vo.setIncoming(incomingVO);
        }

        LambdaQueryWrapper<DocUrgeLog> urgeWrapper = new LambdaQueryWrapper<>();
        urgeWrapper.eq(DocUrgeLog::getIncomingId, supervision.getIncomingId())
                .orderByDesc(DocUrgeLog::getCreateTime);
        List<DocUrgeLog> urgeLogs = docUrgeLogMapper.selectList(urgeWrapper);
        List<DocUrgeLogVO> urgeLogVOs = urgeLogs.stream().map(this::convertUrgeLogToVO).collect(Collectors.toList());
        vo.setUrgeLogs(urgeLogVOs);

        return vo;
    }

    private DocUrgeLogVO convertUrgeLogToVO(DocUrgeLog urgeLog) {
        DocUrgeLogVO vo = new DocUrgeLogVO();
        BeanUtils.copyProperties(urgeLog, vo);
        vo.setStatusName(com.gov.doc.engine.enums.UrgeStatusEnum.getNameByCode(urgeLog.getStatus()));
        return vo;
    }

    private String getPushStatusName(String pushStatus) {
        if ("pending".equals(pushStatus)) {
            return "待推送";
        } else if ("pushed".equals(pushStatus)) {
            return "已推送";
        } else if ("failed".equals(pushStatus)) {
            return "推送失败";
        }
        return pushStatus;
    }
}
