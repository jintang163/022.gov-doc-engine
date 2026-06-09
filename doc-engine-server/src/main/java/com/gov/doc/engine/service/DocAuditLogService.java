package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocAuditLogQueryDTO;
import com.gov.doc.engine.entity.DocAuditLog;
import com.gov.doc.engine.vo.DocAuditLogVO;

public interface DocAuditLogService extends IService<DocAuditLog> {

    PageResult<DocAuditLogVO> pageList(DocAuditLogQueryDTO queryDTO);

    void log(String action, String resourceType, String resourceId, String resourceName,
             String securityLevel, String description, String oldValue, String newValue,
             String result, String errorMessage);

    void logSuccess(String action, String resourceType, String resourceId, String resourceName, String description);

    void logFailure(String action, String resourceType, String resourceId, String resourceName, String errorMessage);
}
