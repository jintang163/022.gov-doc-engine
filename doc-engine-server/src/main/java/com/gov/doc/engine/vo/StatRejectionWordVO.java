package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class StatRejectionWordVO {

    private String word;

    private Long count;

    private Double weight;
}
