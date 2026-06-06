package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DocTemplateHeaderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String headerType;

    @NotBlank(message = "红头配置名称不能为空")
    private String headerName;

    @NotBlank(message = "发文单位名称不能为空")
    private String unitName;

    private String unitNameFont;

    @NotNull(message = "单位名称字号不能为空")
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
