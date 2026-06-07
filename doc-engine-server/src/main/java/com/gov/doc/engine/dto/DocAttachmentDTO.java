package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DocAttachmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "公文ID不能为空")
    private Long documentId;

    @NotBlank(message = "附件名称不能为空")
    private String attachmentName;

    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String fileExt;
    private Integer sortOrder;
    private String remark;
}
