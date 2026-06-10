package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatNodeDwellVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String postId;
    private String postName;
    private Long taskCount;
    private Long avgDwellMinutes;
    private String avgDwellText;
    private Long minDwellMinutes;
    private String minDwellText;
    private Long maxDwellMinutes;
    private String maxDwellText;
    private Double withinRate;
}
