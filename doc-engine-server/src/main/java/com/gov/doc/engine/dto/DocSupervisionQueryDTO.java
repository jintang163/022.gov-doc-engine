package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocSupervisionQueryDTO {

    private String keyword;
    private Long incomingId;
    private String supervisionType;
    private String responsibleUserId;
    private String responsibleDeptId;
    private String leaderId;
    private String pushStatus;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
}
