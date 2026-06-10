package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatOverviewVO {

    private Long totalDocCount;
    private Long draftCount;
    private Long reviewingCount;
    private Long signedCount;
    private Long archivedCount;
    private Long abolishedCount;
    private Double completionRate;
    private Long avgDurationMinutes;
    private String avgDurationText;
    private Long todayCount;
    private Long weekCount;
    private Long monthCount;
}
