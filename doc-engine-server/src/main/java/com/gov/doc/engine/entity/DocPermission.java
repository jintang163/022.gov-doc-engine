package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_permission")
public class DocPermission extends BaseEntity {

    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String securityLevel;
    private String allowedRoles;
    private String allowedUsers;
    private String allowedDepts;
    private String description;
    private Integer enabled;
    private Integer sortOrder;
}
