package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocUrgeDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocUrgeLog;
import com.gov.doc.engine.enums.UrgeStatusEnum;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocUrgeLogMapper;
import com.gov.doc.engine.service.DocUrgeLogService;
import com.gov.doc.engine.vo.DocUrgeLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocUrgeLogServiceImpl extends ServiceImpl<DocUrgeLogMapper, DocUrgeLog> implements DocUrgeLogService {

    @Autowired
    private DocUrgeLogMapper docUrgeLogMapper;

    @Autowired
    private DocHandlingMapper docHandlingMapper;

    @Override
    public DocUrgeLogVO createUrge(DocUrgeDTO dto) {
        DocUrgeLog urgeLog = new DocUrgeLog();
        urgeLog.setIncomingId(dto.getIncomingId());
        urgeLog.setHandlingId(dto.getHandlingId());
        urgeLog.setUrgeNo(BizNoGenerator.generateUrgeNo());
        urgeLog.setUrgeType(StringUtils.hasText(dto.getUrgeType()) ? dto.getUrgeType() : "system");
        urgeLog.setUrgeContent(dto.getUrgeContent());

        if (dto.getHandlingId() != null) {
            DocHandling handling = docHandlingMapper.selectById(dto.getHandlingId());
            if (handling != null) {
                urgeLog.setUrgedUserId(handling.getTargetUserId());
                urgeLog.setUrgedUserName(handling.getTargetUserName());
                urgeLog.setUrgedDeptId(handling.getTargetDeptId());
                urgeLog.setUrgedDeptName(handling.getTargetDeptName());
            }
        }

        urgeLog.setStatus(UrgeStatusEnum.SENT.getCode());
        urgeLog.setRemark(dto.getRemark());

        docUrgeLogMapper.insert(urgeLog);
        return convertToVO(urgeLog);
    }

    @Override
    public DocUrgeLogVO acknowledge(Long id) {
        DocUrgeLog urgeLog = docUrgeLogMapper.selectById(id);
        if (urgeLog == null) {
            throw new RuntimeException("催办记录不存在");
        }

        LambdaUpdateWrapper<DocUrgeLog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocUrgeLog::getId, id)
                .set(DocUrgeLog::getStatus, UrgeStatusEnum.ACKNOWLEDGED.getCode())
                .set(DocUrgeLog::getAcknowledgeTime, LocalDateTime.now());
        docUrgeLogMapper.update(null, updateWrapper);

        return convertToVO(docUrgeLogMapper.selectById(id));
    }

    @Override
    public int countByIncomingId(Long incomingId) {
        LambdaQueryWrapper<DocUrgeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocUrgeLog::getIncomingId, incomingId);
        return Math.toIntExact(docUrgeLogMapper.selectCount(queryWrapper));
    }

    @Override
    public List<DocUrgeLogVO> listByIncomingId(Long incomingId) {
        LambdaQueryWrapper<DocUrgeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocUrgeLog::getIncomingId, incomingId)
                .orderByDesc(DocUrgeLog::getCreateTime);
        List<DocUrgeLog> urgeLogs = docUrgeLogMapper.selectList(queryWrapper);
        return urgeLogs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private DocUrgeLogVO convertToVO(DocUrgeLog urgeLog) {
        DocUrgeLogVO vo = new DocUrgeLogVO();
        BeanUtils.copyProperties(urgeLog, vo);
        vo.setStatusName(UrgeStatusEnum.getNameByCode(urgeLog.getStatus()));
        return vo;
    }
}
