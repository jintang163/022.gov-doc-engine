package com.gov.doc.engine.vo;

import lombok.Data;

@Data
public class DocPermissionVO {

    private Long id;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String permissionTypeName;
    private String securityLevel;
    private String allowedRoles;
    private String allowedUsers;
    private String allowedDepts;
    private String description;
    private Integer enabled;
    private Integer sortOrder;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
}
