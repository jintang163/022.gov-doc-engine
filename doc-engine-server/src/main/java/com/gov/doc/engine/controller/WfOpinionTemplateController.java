package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.WfOpinionTemplateQueryDTO;
import com.gov.doc.engine.dto.WfOpinionTemplateSaveDTO;
import com.gov.doc.engine.service.WfOpinionTemplateService;
import com.gov.doc.engine.vo.WfOpinionTemplateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wf/opinion-template")
public class WfOpinionTemplateController {

    @Autowired
    private WfOpinionTemplateService wfOpinionTemplateService;

    @GetMapping("/list")
    public Result<List<WfOpinionTemplateVO>> list(@ModelAttribute WfOpinionTemplateQueryDTO queryDTO) {
        List<WfOpinionTemplateVO> list = wfOpinionTemplateService.list(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<WfOpinionTemplateVO> getDetail(@PathVariable Long id) {
        WfOpinionTemplateVO vo = wfOpinionTemplateService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Long> save(@RequestBody @Validated WfOpinionTemplateSaveDTO saveDTO) {
        Long id = wfOpinionTemplateService.saveTemplate(saveDTO);
        return Result.success("保存成功", id);
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated WfOpinionTemplateSaveDTO saveDTO) {
        wfOpinionTemplateService.updateTemplate(saveDTO);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wfOpinionTemplateService.deleteTemplate(id);
        return Result.success("删除成功", null);
    }
}
