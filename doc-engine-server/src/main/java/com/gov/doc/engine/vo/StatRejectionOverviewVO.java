package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatRejectionOverviewVO {

    private Long totalRejectionCount;

    private Long totalReturnCount;

    private Long totalCount;

    private Double returnRate;
}
