package com.gov.doc.engine.dto;

import lombok.Data;

@Data
public class EfficiencyRankQueryDTO {

    private String statMonth;

    private String unitCode;

    private String deptId;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
