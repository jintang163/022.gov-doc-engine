package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDeptQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long unitId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private Integer status;
    private String keyword;
}
