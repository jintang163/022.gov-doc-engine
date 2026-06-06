package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_template")
public class DocTemplate extends BaseEntity {

    private String templateCode;
    private String templateName;
    private String templateType;
    private String templateCategory;
    private Integer version;
    private Integer isCurrentVersion;
    private Long parentTemplateId;
    private Integer status;
    private String description;
    private String unitCode;
    private String unitName;
    private String securityLevel;
    private String urgencyLevel;
    private String permissionRoles;
    private String permissionUsers;
    private String permissionDepts;
    private String templateFilePath;
    private String templateFileName;
    private String templateVariables;
    private Long headerId;
    private String remark;
}
