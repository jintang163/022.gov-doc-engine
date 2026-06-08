package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.entity.DocStatusLog;
import com.gov.doc.engine.service.DocStatusLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "公文状态日志")
@RestController
@RequestMapping("/api/doc/status-log")
public class DocStatusLogController {

    @Autowired
    private DocStatusLogService docStatusLogService;

    @ApiOperation("查询公文状态流转日志")
    @GetMapping("/doc/{docId}")
    public Result<List<DocStatusLog>> getByDocId(@PathVariable Long docId) {
        return Result.success(docStatusLogService.getByDocId(docId));
    }

    @ApiOperation("查询公文最新状态记录")
    @GetMapping("/latest/{docId}")
    public Result<DocStatusLog> getLatestStatus(@PathVariable Long docId) {
        return Result.success(docStatusLogService.getLatestStatus(docId));
    }
}
