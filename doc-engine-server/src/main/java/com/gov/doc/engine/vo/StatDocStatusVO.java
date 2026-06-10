package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatDocStatusVO {

    private String status;
    private String statusName;
    private Long count;
    private Double percentage;
}
