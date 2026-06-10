package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysPostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long unitId;
    private String postCode;
    private String postName;
    private String postType;
    private Integer postLevel;
    private Long deptId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private String unitName;
    private String deptName;
}
