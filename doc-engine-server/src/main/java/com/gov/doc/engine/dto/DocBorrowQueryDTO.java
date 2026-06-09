package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class DocBorrowQueryDTO {

    private String keyword;
    private String status;
    private String applicantId;
    private String archiveId;
    private Long docId;
    private String borrowType;
    private Integer pageNum;
    private Integer pageSize;
}
