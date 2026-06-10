package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysDeptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long unitId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private String deptType;
    private String leader;
    private String phone;
    private String email;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private String unitName;
    private String parentName;
    private List<SysDeptVO> children;
    private Integer userCount;
}
