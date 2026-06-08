package com.gov.doc.engine.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.entity.WfApprovalOpinion;
import com.gov.doc.engine.enums.WfApprovalTypeEnum;
import com.gov.doc.engine.enums.WfSignResultEnum;
import com.gov.doc.engine.mapper.WfApprovalOpinionMapper;
import com.gov.doc.engine.service.WfApprovalOpinionService;
import com.gov.doc.engine.vo.WfApprovalOpinionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WfApprovalOpinionServiceImpl extends ServiceImpl<WfApprovalOpinionMapper, WfApprovalOpinion> implements WfApprovalOpinionService {

    @Autowired
    private WfApprovalOpinionMapper approvalOpinionMapper;

    @Override
    public List<WfApprovalOpinionVO> getByProcessInstanceId(Long processInstanceId) {
        List<WfApprovalOpinion> list = approvalOpinionMapper.selectList(
                new LambdaQueryWrapper<WfApprovalOpinion>()
                        .eq(WfApprovalOpinion::getProcessInstanceId, processInstanceId)
                        .orderByDesc(WfApprovalOpinion::getApprovalTime));
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<WfApprovalOpinionVO> getByTaskId(Long taskId) {
        List<WfApprovalOpinion> list = approvalOpinionMapper.selectList(
                new LambdaQueryWrapper<WfApprovalOpinion>()
                        .eq(WfApprovalOpinion::getTaskId, taskId)
                        .orderByDesc(WfApprovalOpinion::getApprovalTime));
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public WfApprovalOpinionVO convertToVO(WfApprovalOpinion opinion) {
        WfApprovalOpinionVO vo = new WfApprovalOpinionVO();
        BeanUtils.copyProperties(opinion, vo);

        vo.setApprovalTypeName(WfApprovalTypeEnum.getNameByCode(opinion.getApprovalType()));
        if (StringUtils.hasText(opinion.getApprovalResult())) {
            vo.setApprovalResultName(getResultName(opinion.getApprovalResult()));
        }
        if (StringUtils.hasText(opinion.getAttachments())) {
            vo.setAttachments(JSON.parseArray(opinion.getAttachments(), Map.class));
        }
        if (StringUtils.hasText(opinion.getVariables())) {
            vo.setVariables(JSON.parseObject(opinion.getVariables(), Map.class));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOpinion(WfApprovalOpinion opinion) {
        if (opinion.getApprovalTime() == null) {
            opinion.setApprovalTime(LocalDateTime.now());
        }
        this.save(opinion);
    }

    private String getResultName(String result) {
        if ("pass".equals(result)) {
            return "通过";
        } else if ("reject".equals(result)) {
            return "驳回";
        } else if ("return".equals(result)) {
            return "退回修改";
        } else if ("terminate".equals(result)) {
            return "终止";
        } else if ("delegate".equals(result)) {
            return "转办";
        } else if ("addSign".equals(result)) {
            return "加签";
        } else if ("countersign".equals(result)) {
            return "会签";
        } else {
            return WfSignResultEnum.getNameByCode(result);
        }
    }
}
