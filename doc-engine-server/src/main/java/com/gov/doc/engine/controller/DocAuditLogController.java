package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocAuditLogQueryDTO;
import com.gov.doc.engine.service.DocAuditLogService;
import com.gov.doc.engine.vo.DocAuditLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doc/audit-log")
public class DocAuditLogController {

    @Autowired
    private DocAuditLogService auditLogService;

    @GetMapping("/page")
    public Result<PageResult<DocAuditLogVO>> page(DocAuditLogQueryDTO queryDTO) {
        PageResult<DocAuditLogVO> pageResult = auditLogService.pageList(queryDTO);
        return Result.success(pageResult);
    }
}
