package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WfProcessDefinitionQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String processCode;
    private String processName;
    private String processType;
    private String processCategory;
    private Integer status;
    private String unitCode;
    private String keyword;
}
