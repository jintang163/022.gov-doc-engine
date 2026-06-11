package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatEfficiencyVO {

    private Long id;
    private String statMonth;
    private String rankType;
    private String targetId;
    private String targetName;
    private String deptId;
    private String deptName;
    private String unitCode;
    private String unitName;
    private Long totalTask;
    private Long completedTask;
    private Long overdueTask;
    private Double completionRate;
    private Long avgDurationMinutes;
    private String avgDurationText;
    private Double efficiencyScore;
    private Integer rankNo;
    private String rankLevel;
}
