package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSealGrantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sealId;
    private String sealName;
    private String grantType;
    private String grantTypeName;
    private Long grantTargetId;
    private String grantTargetName;
    private LocalDateTime grantStartTime;
    private LocalDateTime grantEndTime;
    private Integer signLimit;
    private Integer signCount;
    private Integer status;
    private String statusName;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
