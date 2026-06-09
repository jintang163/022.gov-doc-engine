package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocHandlingDTO;
import com.gov.doc.engine.dto.DocHandlingFeedbackDTO;
import com.gov.doc.engine.dto.DocHandlingQueryDTO;
import com.gov.doc.engine.dto.DocSignReceiptDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.enums.DocHandlingStatusEnum;
import com.gov.doc.engine.enums.DocHandlingTypeEnum;
import com.gov.doc.engine.enums.DocIncomingStatusEnum;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocIncomingMapper;
import com.gov.doc.engine.service.DocHandlingService;
import com.gov.doc.engine.vo.DocHandlingVO;
import com.gov.doc.engine.vo.DocIncomingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocHandlingServiceImpl extends ServiceImpl<DocHandlingMapper, DocHandling> implements DocHandlingService {

    @Autowired
    private DocHandlingMapper docHandlingMapper;

    @Autowired
    private DocIncomingMapper docIncomingMapper;

    @Override
    public PageResult<DocHandlingVO> pageList(DocHandlingQueryDTO queryDTO) {
        LambdaQueryWrapper<DocHandling> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(DocHandling::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        Page<DocHandling> page = new Page<>(pageNum, pageSize);
        Page<DocHandling> resultPage = docHandlingMapper.selectPage(page, queryWrapper);

        java.util.List<DocHandlingVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public DocHandlingVO getDetail(Long id) {
        DocHandling handling = docHandlingMapper.selectById(id);
        if (handling == null) {
            throw new RuntimeException("处理记录不存在");
        }
        return convertToVO(handling);
    }

    @Override
    public DocHandlingVO draftOpinion(DocHandlingDTO dto) {
        Long incomingId = dto.getIncomingId();
        DocIncoming incoming = docIncomingMapper.selectById(incomingId);
        if (incoming == null) {
            throw new RuntimeException("收文记录不存在");
        }

        UserContext currentUser = UserContext.getCurrentUser();

        DocHandling handling = new DocHandling();
        handling.setIncomingId(incomingId);
        handling.setHandlingNo(BizNoGenerator.generateHandlingNo());
        handling.setHandlingType(DocHandlingTypeEnum.DRAFT_OPINION.getCode());
        handling.setOpinion(dto.getOpinion());
        handling.setHandlerId(String.valueOf(currentUser.getUserId()));
        handling.setHandlerName(currentUser.getRealName());
        handling.setHandlerDeptId(String.valueOf(currentUser.getDeptId()));
        handling.setHandlerDeptName(currentUser.getDeptName());
        handling.setTargetDeptId(dto.getTargetDeptId());
        handling.setTargetDeptName(dto.getTargetDeptName());
        handling.setTargetUserId(dto.getTargetUserId());
        handling.setTargetUserName(dto.getTargetUserName());
        if (StringUtils.hasText(dto.getDeadline())) {
            handling.setDeadline(LocalDate.parse(dto.getDeadline()));
        }
        handling.setHandlingTime(LocalDateTime.now());
        handling.setStatus(DocHandlingStatusEnum.COMPLETED.getCode());
        handling.setRemark(dto.getRemark());

        docHandlingMapper.insert(handling);

        LambdaUpdateWrapper<DocIncoming> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocIncoming::getId, incomingId)
                .set(DocIncoming::getStatus, DocIncomingStatusEnum.PROPOSED.getCode());
        docIncomingMapper.update(null, updateWrapper);

        return convertToVO(handling);
    }

    @Override
    public DocHandlingVO assignHandling(DocHandlingDTO dto) {
        Long incomingId = dto.getIncomingId();
        DocIncoming incoming = docIncomingMapper.selectById(incomingId);
        if (incoming == null) {
            throw new RuntimeException("收文记录不存在");
        }

        if (!DocIncomingStatusEnum.PROPOSED.getCode().equals(incoming.getStatus())
                && !DocIncomingStatusEnum.REGISTERED.getCode().equals(incoming.getStatus())) {
            throw new RuntimeException("当前收文状态不允许转承办");
        }

        UserContext currentUser = UserContext.getCurrentUser();

        DocHandling handling = new DocHandling();
        handling.setIncomingId(incomingId);
        handling.setHandlingNo(BizNoGenerator.generateHandlingNo());
        handling.setHandlingType(DocHandlingTypeEnum.ASSIGN.getCode());
        handling.setOpinion(dto.getOpinion());
        handling.setHandlerId(String.valueOf(currentUser.getUserId()));
        handling.setHandlerName(currentUser.getRealName());
        handling.setHandlerDeptId(String.valueOf(currentUser.getDeptId()));
        handling.setHandlerDeptName(currentUser.getDeptName());
        handling.setTargetDeptId(dto.getTargetDeptId());
        handling.setTargetDeptName(dto.getTargetDeptName());
        handling.setTargetUserId(dto.getTargetUserId());
        handling.setTargetUserName(dto.getTargetUserName());
        if (StringUtils.hasText(dto.getDeadline())) {
            handling.setDeadline(LocalDate.parse(dto.getDeadline()));
        }
        handling.setStatus(DocHandlingStatusEnum.PENDING.getCode());
        handling.setRemark(dto.getRemark());

        docHandlingMapper.insert(handling);

        LambdaUpdateWrapper<DocIncoming> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocIncoming::getId, incomingId)
                .set(DocIncoming::getStatus, DocIncomingStatusEnum.HANDLING.getCode());
        docIncomingMapper.update(null, updateWrapper);

        return convertToVO(handling);
    }

    @Override
    public DocHandlingVO submitFeedback(DocHandlingFeedbackDTO dto) {
        DocHandling handling = docHandlingMapper.selectById(dto.getHandlingId());
        if (handling == null) {
            throw new RuntimeException("处理记录不存在");
        }

        if (!DocHandlingTypeEnum.ASSIGN.getCode().equals(handling.getHandlingType())
                && !DocHandlingTypeEnum.DRAFT_OPINION.getCode().equals(handling.getHandlingType())) {
            throw new RuntimeException("当前处理类型不允许提交反馈");
        }

        LambdaUpdateWrapper<DocHandling> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocHandling::getId, dto.getHandlingId())
                .set(DocHandling::getFeedbackContent, dto.getFeedbackContent())
                .set(DocHandling::getFeedbackAttachment, dto.getFeedbackAttachment())
                .set(DocHandling::getFeedbackTime, LocalDateTime.now())
                .set(DocHandling::getStatus, DocHandlingStatusEnum.COMPLETED.getCode());
        docHandlingMapper.update(null, updateWrapper);

        LambdaQueryWrapper<DocHandling> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(DocHandling::getIncomingId, handling.getIncomingId())
                .eq(DocHandling::getStatus, DocHandlingStatusEnum.PENDING.getCode());
        long pendingCount = docHandlingMapper.selectCount(pendingWrapper);

        if (pendingCount <= 1) {
            LambdaUpdateWrapper<DocIncoming> incomingUpdate = new LambdaUpdateWrapper<>();
            incomingUpdate.eq(DocIncoming::getId, handling.getIncomingId())
                    .set(DocIncoming::getStatus, DocIncomingStatusEnum.COMPLETED.getCode());
            docIncomingMapper.update(null, incomingUpdate);
        }

        return convertToVO(docHandlingMapper.selectById(dto.getHandlingId()));
    }

    @Override
    public DocHandlingVO signReceipt(DocSignReceiptDTO dto) {
        DocHandling handling = docHandlingMapper.selectById(dto.getHandlingId());
        if (handling == null) {
            throw new RuntimeException("处理记录不存在");
        }

        UserContext currentUser = UserContext.getCurrentUser();

        LambdaUpdateWrapper<DocHandling> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocHandling::getId, dto.getHandlingId())
                .set(DocHandling::getSignReceiptTime, LocalDateTime.now())
                .set(DocHandling::getStatus, DocHandlingStatusEnum.COMPLETED.getCode())
                .set(DocHandling::getHandlerId, String.valueOf(currentUser.getUserId()))
                .set(DocHandling::getHandlerName, currentUser.getRealName())
                .set(DocHandling::getHandlerDeptId, String.valueOf(currentUser.getDeptId()))
                .set(DocHandling::getHandlerDeptName, currentUser.getDeptName())
                .set(DocHandling::getHandlingTime, LocalDateTime.now());

        docHandlingMapper.update(null, updateWrapper);

        return convertToVO(docHandlingMapper.selectById(dto.getHandlingId()));
    }

    @Override
    public PageResult<DocHandlingVO> getMyHandlings(DocHandlingQueryDTO queryDTO) {
        UserContext currentUser = UserContext.getCurrentUser();
        if (queryDTO == null) {
            queryDTO = new DocHandlingQueryDTO();
        }
        queryDTO.setHandlerId(String.valueOf(currentUser.getUserId()));
        return pageList(queryDTO);
    }

    @Override
    public PageResult<DocHandlingVO> getPendingSignReceipts(DocHandlingQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new DocHandlingQueryDTO();
        }
        queryDTO.setHandlingType(DocHandlingTypeEnum.SIGN_RECEIPT.getCode());
        queryDTO.setStatus(DocHandlingStatusEnum.PENDING.getCode());
        return pageList(queryDTO);
    }

    private LambdaQueryWrapper<DocHandling> buildQueryWrapper(DocHandlingQueryDTO queryDTO) {
        LambdaQueryWrapper<DocHandling> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getIncomingId() != null) {
            queryWrapper.eq(DocHandling::getIncomingId, queryDTO.getIncomingId());
        }
        if (StringUtils.hasText(queryDTO.getHandlingType())) {
            queryWrapper.eq(DocHandling::getHandlingType, queryDTO.getHandlingType());
        }
        if (StringUtils.hasText(queryDTO.getHandlerId())) {
            queryWrapper.eq(DocHandling::getHandlerId, queryDTO.getHandlerId());
        }
        if (StringUtils.hasText(queryDTO.getHandlerDeptId())) {
            queryWrapper.eq(DocHandling::getHandlerDeptId, queryDTO.getHandlerDeptId());
        }
        if (StringUtils.hasText(queryDTO.getTargetDeptId())) {
            queryWrapper.eq(DocHandling::getTargetDeptId, queryDTO.getTargetDeptId());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(DocHandling::getStatus, queryDTO.getStatus());
        }

        return queryWrapper;
    }

    private DocHandlingVO convertToVO(DocHandling handling) {
        DocHandlingVO vo = new DocHandlingVO();
        BeanUtils.copyProperties(handling, vo);
        vo.setStatusName(DocHandlingStatusEnum.getNameByCode(handling.getStatus()));
        vo.setHandlingTypeName(DocHandlingTypeEnum.getNameByCode(handling.getHandlingType()));

        DocIncoming incoming = docIncomingMapper.selectById(handling.getIncomingId());
        if (incoming != null) {
            DocIncomingVO incomingVO = new DocIncomingVO();
            BeanUtils.copyProperties(incoming, incomingVO);
            incomingVO.setStatusName(DocIncomingStatusEnum.getNameByCode(incoming.getStatus()));
            incomingVO.setSourceName("manual".equals(incoming.getSource()) ? "手动录入" : "接口接入");
            vo.setIncoming(incomingVO);
        }

        return vo;
    }
}
