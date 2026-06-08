package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.WfProcessDefinitionQueryDTO;
import com.gov.doc.engine.dto.WfProcessDefinitionSaveDTO;
import com.gov.doc.engine.service.WfProcessDefinitionService;
import com.gov.doc.engine.vo.WfProcessDefinitionVO;
import com.gov.doc.engine.vo.WfProcessGraphVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wf/process-definition")
public class WfProcessDefinitionController {

    @Autowired
    private WfProcessDefinitionService wfProcessDefinitionService;

    @GetMapping("/page")
    public Result<PageResult<WfProcessDefinitionVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ModelAttribute WfProcessDefinitionQueryDTO queryDTO) {
        PageResult<WfProcessDefinitionVO> pageResult = wfProcessDefinitionService.pageList(pageNum, pageSize, queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<WfProcessDefinitionVO> getDetail(@PathVariable Long id) {
        WfProcessDefinitionVO vo = wfProcessDefinitionService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/code/{processCode}")
    public Result<WfProcessDefinitionVO> getByCode(@PathVariable String processCode) {
        WfProcessDefinitionVO vo = wfProcessDefinitionService.getByCode(processCode);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Validated WfProcessDefinitionSaveDTO saveDTO) {
        Long id = wfProcessDefinitionService.saveProcessDefinition(saveDTO);
        return Result.success("保存成功", id);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated WfProcessDefinitionSaveDTO saveDTO) {
        wfProcessDefinitionService.updateProcessDefinition(saveDTO);
        return Result.success("更新成功", null);
    }

    @PostMapping("/publish/{id}")
    public Result<Void> publish(@PathVariable Long id) {
        wfProcessDefinitionService.publish(id);
        return Result.success("发布成功", null);
    }

    @PostMapping("/suspend/{id}")
    public Result<Void> suspend(@PathVariable Long id) {
        wfProcessDefinitionService.suspend(id);
        return Result.success("停用成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wfProcessDefinitionService.deleteProcessDefinition(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/list/published")
    public Result<List<WfProcessDefinitionVO>> listAllPublished() {
        List<WfProcessDefinitionVO> list = wfProcessDefinitionService.listAllPublished();
        return Result.success(list);
    }

    @GetMapping("/graph/{processDefId}")
    public Result<WfProcessGraphVO> getProcessGraph(
            @PathVariable Long processDefId,
            @RequestParam(required = false) Long processInstanceId) {
        WfProcessGraphVO vo = wfProcessDefinitionService.getProcessGraph(processDefId, processInstanceId);
        return Result.success(vo);
    }

    @PostMapping("/deploy/{id}")
    public Result<Void> deploy(@PathVariable Long id) {
        wfProcessDefinitionService.deployProcessDefinition(id);
        return Result.success("部署成功", null);
    }
}
