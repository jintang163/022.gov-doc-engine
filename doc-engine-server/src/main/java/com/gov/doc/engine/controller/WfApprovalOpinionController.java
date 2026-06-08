package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.service.WfApprovalOpinionService;
import com.gov.doc.engine.vo.WfApprovalOpinionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wf/approval-opinion")
public class WfApprovalOpinionController {

    @Autowired
    private WfApprovalOpinionService wfApprovalOpinionService;

    @GetMapping("/process/{processInstanceId}")
    public Result<List<WfApprovalOpinionVO>> getByProcessInstanceId(@PathVariable Long processInstanceId) {
        List<WfApprovalOpinionVO> list = wfApprovalOpinionService.getByProcessInstanceId(processInstanceId);
        return Result.success(list);
    }

    @GetMapping("/task/{taskId}")
    public Result<List<WfApprovalOpinionVO>> getByTaskId(@PathVariable Long taskId) {
        List<WfApprovalOpinionVO> list = wfApprovalOpinionService.getByTaskId(taskId);
        return Result.success(list);
    }
}
