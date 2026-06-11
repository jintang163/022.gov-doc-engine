package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocSupervisionDTO;
import com.gov.doc.engine.dto.DocSupervisionQueryDTO;
import com.gov.doc.engine.dto.DocUrgeDTO;
import com.gov.doc.engine.service.DocSupervisionService;
import com.gov.doc.engine.service.DocUrgeLogService;
import com.gov.doc.engine.vo.DocSupervisionVO;
import com.gov.doc.engine.vo.DocUrgeLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/supervision")
public class DocSupervisionController {

    @Autowired
    private DocSupervisionService docSupervisionService;

    @Autowired
    private DocUrgeLogService docUrgeLogService;

    @GetMapping("/page")
    public Result<PageResult<DocSupervisionVO>> page(DocSupervisionQueryDTO queryDTO) {
        PageResult<DocSupervisionVO> pageResult = docSupervisionService.pageList(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<DocSupervisionVO> getDetail(@PathVariable Long id) {
        DocSupervisionVO vo = docSupervisionService.getDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/identify-timeout")
    public Result<List<DocSupervisionVO>> identifyTimeoutDocs() {
        List<DocSupervisionVO> list = docSupervisionService.identifyTimeoutDocs();
        return Result.success(list);
    }

    @GetMapping("/identify-urge-overdue")
    public Result<List<DocSupervisionVO>> identifyUrgeOverdueDocs() {
        List<DocSupervisionVO> list = docSupervisionService.identifyUrgeOverdueDocs();
        return Result.success(list);
    }

    @PostMapping("/generate")
    public Result<DocSupervisionVO> generateSupervision(@RequestBody @Validated DocSupervisionDTO dto) {
        DocSupervisionVO vo = docSupervisionService.generateSupervision(dto);
        return Result.success("督办单生成成功", vo);
    }

    @PostMapping("/batch-generate")
    public Result<List<DocSupervisionVO>> batchGenerateSupervision(@RequestBody @Validated List<DocSupervisionDTO> dtoList) {
        List<DocSupervisionVO> list = docSupervisionService.batchGenerateSupervision(dtoList);
        return Result.success("批量生成督办单成功", list);
    }

    @PostMapping("/{id}/push")
    public Result<DocSupervisionVO> pushToLeader(@PathVariable Long id) {
        DocSupervisionVO vo = docSupervisionService.pushToLeader(id);
        return Result.success("推送至分管领导成功", vo);
    }

    @PostMapping("/batch-push")
    public Result<List<DocSupervisionVO>> batchPushToLeader(@RequestBody List<Long> ids) {
        List<DocSupervisionVO> list = docSupervisionService.batchPushToLeader(ids);
        return Result.success("批量推送成功", list);
    }

    @PutMapping("/{id}/status")
    public Result<DocSupervisionVO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        DocSupervisionVO vo = docSupervisionService.updateStatus(id, status);
        return Result.success("状态更新成功", vo);
    }

    @PutMapping("/{id}/complete")
    public Result<DocSupervisionVO> completeSupervision(@PathVariable Long id) {
        DocSupervisionVO vo = docSupervisionService.completeSupervision(id);
        return Result.success("督办完成", vo);
    }

    @GetMapping("/my-supervisions")
    public Result<PageResult<DocSupervisionVO>> getMySupervisions(DocSupervisionQueryDTO queryDTO) {
        PageResult<DocSupervisionVO> pageResult = docSupervisionService.getMySupervisions(queryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/urge")
    public Result<DocUrgeLogVO> createUrge(@RequestBody @Validated DocUrgeDTO dto) {
        DocUrgeLogVO vo = docUrgeLogService.createUrge(dto);
        return Result.success("催办发送成功", vo);
    }

    @PutMapping("/urge/{id}/acknowledge")
    public Result<DocUrgeLogVO> acknowledgeUrge(@PathVariable Long id) {
        DocUrgeLogVO vo = docUrgeLogService.acknowledge(id);
        return Result.success("催办确认成功", vo);
    }

    @GetMapping("/urge/list/{incomingId}")
    public Result<List<DocUrgeLogVO>> listUrgeLogs(@PathVariable Long incomingId) {
        List<DocUrgeLogVO> list = docUrgeLogService.listByIncomingId(incomingId);
        return Result.success(list);
    }
}
