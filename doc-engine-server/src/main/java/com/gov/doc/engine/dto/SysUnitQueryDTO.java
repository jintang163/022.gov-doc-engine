package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUnitQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unitCode;
    private String unitName;
    private String unitType;
    private Long parentId;
    private Integer status;
    private String keyword;
}
