package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatTimelinessTrendVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String date;
    private Long avgDraftMinutes;
    private Long avgDwellMinutes;
    private Long avgCountersignMinutes;
    private Double completionRate;
}
