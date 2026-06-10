package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_unit")
public class SysUnit extends BaseEntity {

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
}
