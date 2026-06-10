package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class StatQueryDTO {

    private String startDate;
    private String endDate;
    private String unitCode;
    private String docType;
    private String processType;
}
