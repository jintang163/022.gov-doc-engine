package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WfOpinionTemplateQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String templateType;
    private String userId;
    private Integer isPreset;
    private Integer status;
    private String keyword;
}
