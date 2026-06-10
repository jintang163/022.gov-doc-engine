package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String userCode;
    private String userName;
    private String loginName;
    private String password;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private Long unitId;
    private Long deptId;
    private Long postId;
    private String postName;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
