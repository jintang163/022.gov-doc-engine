package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatDeptDraftVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String deptId;
    private String deptName;
    private Long docCount;
    private Long avgDraftMinutes;
    private String avgDraftText;
    private Long minDraftMinutes;
    private String minDraftText;
    private Long maxDraftMinutes;
    private String maxDraftText;
}
