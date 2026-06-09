package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_archive")
public class DocArchive extends BaseEntity {

    private Long docId;
    private String archiveNo;
    private Integer archiveYear;
    private String archiveType;
    private String archiveDeptId;
    private String archiveDeptName;
    private String securityLevel;
    private String archiveMethod;
    private String archiveLocation;
    private String retentionPeriod;
    private LocalDate archiveDate;
    private Integer isLocked;
    private String status;
    private String docTitle;
    private String docNumber;
    private String docType;
    private String docContentSnapshot;
    private String unitCode;
    private String unitName;
    private String remark;
}
