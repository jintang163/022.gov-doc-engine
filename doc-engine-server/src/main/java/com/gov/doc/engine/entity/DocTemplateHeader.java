package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_template_header")
public class DocTemplateHeader extends BaseEntity {

    private Long templateId;
    private String headerType;
    private String unitName;
    private String unitNameFont;
    private Integer unitNameFontSize;
    private String unitNameFontColor;
    private Integer unitNameFontBold;
    private String unitNameTextAlign;
    private Integer unitNameMarginTop;
    private Integer unitNameMarginBottom;
    private Integer showDocumentNumber;
    private String documentNumberPrefix;
    private String documentNumberYear;
    private String documentNumberFont;
    private Integer documentNumberFontSize;
    private String documentNumberFontColor;
    private String documentNumberTextAlign;
    private Integer documentNumberMarginTop;
    private Integer documentNumberMarginBottom;
    private Integer showRedLine;
    private Integer redLineWidth;
    private Integer redLineHeight;
    private String redLineColor;
    private Integer redLineMarginTop;
    private Integer redLineMarginBottom;
    private Integer showStar;
    private Integer starSize;
    private String starColor;
    private Integer pageWidth;
    private Integer pageHeight;
    private Integer pageMarginTop;
    private Integer pageMarginBottom;
    private Integer pageMarginLeft;
    private Integer pageMarginRight;
    private String customCss;
    private String remark;
}
