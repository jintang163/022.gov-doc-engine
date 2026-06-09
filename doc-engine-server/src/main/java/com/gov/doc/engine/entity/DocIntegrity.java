package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_integrity")
public class DocIntegrity extends BaseEntity {

    private Long docId;
    private String contentHash;
    private String signatureHash;
    private String hashAlgorithm;
    private Integer version;
    private String verifyStatus;
    private LocalDateTime verifyTime;
    private String verifiedBy;
    private String verifiedByName;
    private String contentSnapshot;
    private String remark;
}
