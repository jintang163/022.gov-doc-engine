package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

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
}
