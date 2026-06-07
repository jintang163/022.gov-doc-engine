package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_attachment")
public class DocAttachment extends BaseEntity {

    private Long documentId;
    private String attachmentName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String fileExt;
    private Integer sortOrder;
    private String remark;
}
