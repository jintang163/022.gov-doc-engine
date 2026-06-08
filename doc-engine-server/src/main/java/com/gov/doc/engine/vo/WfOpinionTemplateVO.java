package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class WfOpinionTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateType;
    private String templateTypeName;
    private String templateContent;
    private Integer sortOrder;
    private Integer isPreset;
    private String isPresetName;
    private String userId;
    private String userName;
    private Integer status;
    private String statusName;
    private String remark;
    private LocalDateTime createTime;
}
