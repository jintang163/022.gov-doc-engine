package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userCode;
    private String userName;
    private String loginName;
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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private String unitName;
    private String deptName;
}
