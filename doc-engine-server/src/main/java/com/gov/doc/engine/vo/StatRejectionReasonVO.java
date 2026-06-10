package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatRejectionReasonVO {

    private String reason;

    private Long count;

    private Double percentage;

    private String deptId;

    private String deptName;
}
