package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class DeptRecommendVO {

    private String deptId;

    private String deptName;

    private Double matchScore;

    private String matchScoreText;

    private Integer matchCount;

    private String lastHandleTime;

    private String matchReason;
}
