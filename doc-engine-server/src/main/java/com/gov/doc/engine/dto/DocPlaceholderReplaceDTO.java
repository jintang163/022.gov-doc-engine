package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

@Data
public class DocPlaceholderReplaceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "正文内容不能为空")
    private String content;

    private Map<String, String> placeholders;
}
