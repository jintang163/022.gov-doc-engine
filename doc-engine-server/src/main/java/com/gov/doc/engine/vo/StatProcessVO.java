package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatProcessVO {

    private String processName;
    private Long totalCount;
    private Long completedCount;
    private Long runningCount;
    private Long terminatedCount;
    private Double completionRate;
    private Long avgDurationMinutes;
    private String avgDurationText;
}
