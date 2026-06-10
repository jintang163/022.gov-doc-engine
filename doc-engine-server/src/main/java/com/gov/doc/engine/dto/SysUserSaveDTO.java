package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SysUserSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "用户编码不能为空")
    private String userCode;

    @NotBlank(message = "用户名称不能为空")
    private String userName;

    private String loginName;
    private String password;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;

    @NotNull(message = "所属单位不能为空")
    private Long unitId;

    private Long deptId;
    private Long postId;
    private String postName;
    private Integer sortOrder;
    private String remark;
}
