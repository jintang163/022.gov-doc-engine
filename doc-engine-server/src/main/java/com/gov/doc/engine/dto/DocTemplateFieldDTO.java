package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DocTemplateFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long templateId;

    @NotBlank(message = "字段键名不能为空")
    private String fieldKey;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private String fieldGroup;

    private Integer isRequired;

    private Integer isPreset;

    private String defaultValue;

    private String placeholder;

    private String fieldOptions;

    private Integer sortOrder;

    private String validationRule;

    private String fontFamily;

    private Integer fontSize;

    private String fontColor;

    private Integer fontBold;

    private String textAlign;

    private Integer marginTop;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private BigDecimal lineHeight;

    private Integer textIndent;

    private String remark;
}
