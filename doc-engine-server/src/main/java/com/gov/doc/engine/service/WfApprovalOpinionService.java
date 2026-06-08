package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.entity.WfApprovalOpinion;
import com.gov.doc.engine.vo.WfApprovalOpinionVO;

import java.util.List;

public interface WfApprovalOpinionService extends IService<WfApprovalOpinion> {

    List<WfApprovalOpinionVO> getByProcessInstanceId(Long processInstanceId);

    List<WfApprovalOpinionVO> getByTaskId(Long taskId);

    WfApprovalOpinionVO convertToVO(WfApprovalOpinion opinion);

    void saveOpinion(WfApprovalOpinion opinion);
}
