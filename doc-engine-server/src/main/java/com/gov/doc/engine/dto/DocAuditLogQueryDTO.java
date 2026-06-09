package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocAuditLogQueryDTO {

    private String userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String securityLevel;
    private String result;
    private String startTime;
    private String endTime;
    private Integer pageNum;
    private Integer pageSize;
}
