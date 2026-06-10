package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatUnitVO {

    private String unitCode;
    private String unitName;
    private Long docCount;
    private Long completedCount;
    private Double completionRate;
    private Long avgDurationMinutes;
}
