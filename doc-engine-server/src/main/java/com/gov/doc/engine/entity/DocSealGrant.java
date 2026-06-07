package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_seal_grant")
public class DocSealGrant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long sealId;

    private String grantType;

    private Long grantTargetId;

    private String grantTargetName;

    private LocalDateTime grantStartTime;

    private LocalDateTime grantEndTime;

    private Integer signLimit;

    private Integer signCount;

    private Integer status;

    private String remark;
}
