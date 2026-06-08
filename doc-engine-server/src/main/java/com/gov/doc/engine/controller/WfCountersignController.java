package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.WfCountersignSignDTO;
import com.gov.doc.engine.service.WfCountersignService;
import com.gov.doc.engine.vo.WfCountersignItemVO;
import com.gov.doc.engine.vo.WfCountersignVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wf/countersign")
public class WfCountersignController {

    @Autowired
    private WfCountersignService wfCountersignService;

    @GetMapping("/{id}")
    public Result<WfCountersignVO> getDetail(@PathVariable Long id) {
        WfCountersignVO vo = wfCountersignService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/process/{processInstanceId}")
    public Result<WfCountersignVO> getByProcessInstanceId(@PathVariable Long processInstanceId) {
        WfCountersignVO vo = wfCountersignService.getByProcessInstanceId(processInstanceId);
        return Result.success(vo);
    }

    @GetMapping("/items/{countersignId}")
    public Result<List<WfCountersignItemVO>> getCountersignItems(@PathVariable Long countersignId) {
        List<WfCountersignItemVO> list = wfCountersignService.getCountersignItems(countersignId);
        return Result.success(list);
    }

    @PostMapping("/sign")
    public Result<Void> sign(@RequestBody @Validated WfCountersignSignDTO signDTO) {
        wfCountersignService.signCountersign(signDTO);
        return Result.success("签署成功", null);
    }
}
