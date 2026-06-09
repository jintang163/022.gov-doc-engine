package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_audit_log")
public class DocAuditLog {

    private Long id;
    private String logNo;
    private String userId;
    private String userName;
    private String deptId;
    private String deptName;
    private String action;
    private String actionName;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String securityLevel;
    private String description;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private Long costTime;
    private String result;
    private String errorMessage;
    private LocalDateTime createTime;
}
