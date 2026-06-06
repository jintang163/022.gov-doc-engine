package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_template_field")
public class DocTemplateField extends BaseEntity {

    private Long templateId;
    private String fieldKey;
    private String fieldName;
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
