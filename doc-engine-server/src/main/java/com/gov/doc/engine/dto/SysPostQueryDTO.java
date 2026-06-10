package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysPostQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long unitId;
    private Long deptId;
    private String postCode;
    private String postName;
    private String postType;
    private Integer status;
    private String keyword;
}
