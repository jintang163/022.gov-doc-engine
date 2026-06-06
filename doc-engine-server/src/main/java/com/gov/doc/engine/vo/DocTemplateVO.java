package com.gov.doc.engine.vo;

import com.gov.doc.engine.dto.DocTemplateFieldDTO;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateType;
    private String templateCategory;
    private Integer version;
    private Integer isCurrentVersion;
    private Long parentTemplateId;
    private Integer status;
    private String statusName;
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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private DocTemplateHeaderDTO header;
    private List<DocTemplateFieldDTO> fields;
}
