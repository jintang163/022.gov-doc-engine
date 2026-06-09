package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocIncomingQueryDTO {

    private String keyword;
    private String source;
    private String sourceUnit;
    private String docType;
    private String securityLevel;
    private String urgencyLevel;
    private String status;
    private String receivedDateStart;
    private String receivedDateEnd;
    private Integer pageNum;
    private Integer pageSize;
}
