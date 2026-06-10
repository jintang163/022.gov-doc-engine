package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends BaseEntity {

    private Long unitId;
    private String postCode;
    private String postName;
    private String postType;
    private Integer postLevel;
    private Long deptId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
