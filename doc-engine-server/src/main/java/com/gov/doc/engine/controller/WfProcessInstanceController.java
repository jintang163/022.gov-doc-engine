package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.WfProcessStartDTO;
import com.gov.doc.engine.service.WfProcessDefinitionService;
import com.gov.doc.engine.service.WfProcessInstanceService;
import com.gov.doc.engine.vo.WfProcessGraphVO;
import com.gov.doc.engine.vo.WfProcessInstanceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wf/process-instance")
public class WfProcessInstanceController {

    @Autowired
    private WfProcessInstanceService wfProcessInstanceService;

    @Autowired
    private WfProcessDefinitionService wfProcessDefinitionService;

    @GetMapping("/page")
    public Result<PageResult<WfProcessInstanceVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PageResult<WfProcessInstanceVO> pageResult = wfProcessInstanceService.pageList(pageNum, pageSize, startUserId, status, keyword);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<WfProcessInstanceVO> getDetail(@PathVariable Long id) {
        WfProcessInstanceVO vo = wfProcessInstanceService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/business/{businessKey}")
    public Result<WfProcessInstanceVO> getByBusinessKey(@PathVariable String businessKey) {
        WfProcessInstanceVO vo = wfProcessInstanceService.getByBusinessKey(businessKey);
        return Result.success(vo);
    }

    @PostMapping("/start")
    public Result<Long> start(@RequestBody @Validated WfProcessStartDTO startDTO) {
        Long id = wfProcessInstanceService.startProcess(startDTO);
        return Result.success("启动成功", id);
    }

    @PostMapping("/suspend/{id}")
    public Result<Void> suspend(@PathVariable Long id) {
        wfProcessInstanceService.suspendProcess(id);
        return Result.success("挂起成功", null);
    }

    @PostMapping("/activate/{id}")
    public Result<Void> activate(@PathVariable Long id) {
        wfProcessInstanceService.activateProcess(id);
        return Result.success("激活成功", null);
    }

    @PostMapping("/terminate/{id}")
    public Result<Void> terminate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        wfProcessInstanceService.terminateProcess(id, reason);
        return Result.success("终止成功", null);
    }

    @GetMapping("/graph/{id}")
    public Result<WfProcessGraphVO> getProcessGraph(@PathVariable Long id) {
        WfProcessInstanceVO instanceVO = wfProcessInstanceService.getDetail(id);
        WfProcessGraphVO vo = wfProcessDefinitionService.getProcessGraph(instanceVO.getProcessDefId(), id);
        return Result.success(vo);
    }
}
