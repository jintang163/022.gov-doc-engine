package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatTrendVO {

    private String date;
    private Long docCount;
    private Long completedCount;
    private Long avgDurationMinutes;
}
