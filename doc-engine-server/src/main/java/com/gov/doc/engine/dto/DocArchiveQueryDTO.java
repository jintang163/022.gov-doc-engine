package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocArchiveQueryDTO {

    private String keyword;
    private Integer archiveYear;
    private String archiveType;
    private String archiveDeptId;
    private String securityLevel;
    private String status;
    private String docNumber;
    private String docType;
    private String unitCode;
    private Integer pageNum;
    private Integer pageSize;
}
