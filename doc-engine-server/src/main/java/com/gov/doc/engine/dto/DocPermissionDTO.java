package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DocPermissionDTO {

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    private String securityLevel;
    private String allowedRoles;
    private String allowedUsers;
    private String allowedDepts;
    private String description;
    private Integer enabled;
    private Integer sortOrder;
}
