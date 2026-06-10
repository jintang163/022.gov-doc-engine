package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatCountersignCycleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String docType;
    private String docTypeName;
    private Long countersignCount;
    private Long avgCycleMinutes;
    private String avgCycleText;
    private Long minCycleMinutes;
    private String minCycleText;
    private Long maxCycleMinutes;
    private String maxCycleText;
    private Double withinRate;
}
