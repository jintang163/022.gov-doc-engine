package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUnitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String unitCode;
    private String unitName;
    private String unitType;
    private Long parentId;
    private String shortName;
    private String leader;
    private String phone;
    private String address;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private String parentName;
    private List<SysUnitVO> children;
    private Integer userCount;
    private Integer deptCount;
}
