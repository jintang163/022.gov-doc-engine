<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>公文预览</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: "仿宋", "SimSun", serif;
            background-color: #f0f0f0;
            padding: 20px;
        }

        .document-container {
            width: ${header.pageWidth}px;
            min-height: ${header.pageHeight}px;
            margin: 0 auto;
            background-color: #ffffff;
            padding: ${header.pageMarginTop}px ${header.pageMarginRight}px ${header.pageMarginBottom}px ${header.pageMarginLeft}px;
            position: relative;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        .document-header {
            text-align: center;
            margin-bottom: 20px;
        }

        .unit-name {
            font-family: "${header.unitNameFont}", "SimSun", serif;
            font-size: ${header.unitNameFontSize}px;
            color: ${header.unitNameFontColor};
            <#if header.unitNameFontBold == 1>font-weight: bold;</#if>
            text-align: ${header.unitNameTextAlign};
            margin-top: ${header.unitNameMarginTop}px;
            margin-bottom: ${header.unitNameMarginBottom}px;
            letter-spacing: 2px;
            line-height: 1.2;
        }

        <#if header.showDocumentNumber == 1>
        .doc-number {
            font-family: "${header.documentNumberFont}", "FangSong", serif;
            font-size: ${header.documentNumberFontSize}px;
            color: ${header.documentNumberFontColor};
            text-align: ${header.documentNumberTextAlign};
            margin-top: ${header.documentNumberMarginTop}px;
            margin-bottom: ${header.documentNumberMarginBottom}px;
        }
        </#if>

        <#if header.showRedLine == 1>
        .red-line {
            width: ${header.redLineWidth}%;
            height: ${header.redLineHeight}px;
            background-color: ${header.redLineColor};
            margin: ${header.redLineMarginTop}px auto ${header.redLineMarginBottom}px auto;
            position: relative;
        }
            <#if header.showStar == 1>
            .red-line::after {
                content: "";
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: ${header.starSize}px;
                height: ${header.starSize}px;
                background-color: ${header.starColor};
                clip-path: polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%);
            }
            </#if>
        </#if>

        <#if header.showDocumentNumber == 1 && document.docNumber??>
        .doc-number-display {
            font-family: "${header.documentNumberFont}", "FangSong", serif;
            font-size: ${header.documentNumberFontSize}px;
            color: ${header.documentNumberFontColor};
            text-align: ${header.documentNumberTextAlign};
            margin-bottom: 20px;
        }
        </#if>

        .document-body {
            min-height: 400px;
        }

        <#if fields??>
            <#list fields as field>
                <#if field.fieldGroup == "main" || field.fieldGroup == "header" || field.fieldGroup == "footer">
                .field-${field.fieldKey} {
                    <#if field.fontFamily??>font-family: "${field.fontFamily}", "FangSong", serif;</#if>
                    <#if field.fontSize??>font-size: ${field.fontSize}px;</#if>
                    <#if field.fontColor??>color: ${field.fontColor};</#if>
                    <#if field.fontBold == 1>font-weight: bold;</#if>
                    <#if field.textAlign??>text-align: ${field.textAlign};</#if>
                    <#if field.marginTop??>margin-top: ${field.marginTop}px;</#if>
                    <#if field.marginBottom??>margin-bottom: ${field.marginBottom}px;</#if>
                    <#if field.marginLeft??>margin-left: ${field.marginLeft}px;</#if>
                    <#if field.marginRight??>margin-right: ${field.marginRight}px;</#if>
                    <#if field.lineHeight??>line-height: ${field.lineHeight};</#if>
                    <#if field.textIndent?? && field.textIndent > 0>text-indent: ${field.textIndent}em;</#if>
                }
                </#if>
            </#list>
        </#if>

        .field-label {
            display: inline-block;
            margin-right: 10px;
        }

        .field-value {
            display: inline;
        }

        .doc-title {
            font-size: 22px;
            font-weight: bold;
            text-align: center;
            margin: 20px 0;
            font-family: "黑体", "SimHei", sans-serif;
        }

        .doc-content {
            font-size: 16px;
            line-height: 1.5;
            text-indent: 2em;
            margin: 10px 0;
        }

        .doc-content p {
            margin-bottom: 10px;
        }

        .written-date {
            text-align: right;
            margin-top: 40px;
            font-size: 16px;
        }

        <#if header.customCss?? && header.customCss != "">
        ${header.customCss}
        </#if>
    </style>
</head>
<body>
    <div class="document-container">
        <div class="document-header">
            <div class="unit-name">${header.unitName}</div>

            <#if header.showDocumentNumber == 1>
                <#if document?? && document.docNumber?? && document.docNumber != "">
                    <div class="doc-number-display">${document.docNumber}</div>
                <#else>
                    <div class="doc-number">${header.documentNumberPrefix!""}${header.documentNumberYear!""}X号</div>
                </#if>
            </#if>

            <#if header.showRedLine == 1>
                <div class="red-line"></div>
            </#if>
        </div>

        <div class="document-body">
            <#if document??>
                <#-- 标题字段 -->
                <#if document.docTitle??>
                    <div class="doc-title">${document.docTitle}</div>
                </#if>

                <#-- 主送机关 -->
                <#if document.mainSendDept?? && document.mainSendDept != "">
                    <div style="font-size: 16px; margin: 10px 0;"><span style="font-weight: bold;">主送机关：</span>${document.mainSendDept}</div>
                </#if>

                <#-- 签发人（上行文） -->
                <#if document.signer?? && document.signer != "">
                    <div style="font-size: 16px; text-align: right; margin: 10px 0;">签发人：${document.signer}</div>
                </#if>

                <#-- 正文 -->
                <#if document.docContent?? && document.docContent != "">
                    <div class="doc-content">${document.docContent}</div>
                <#else>
                    <div class="doc-content" style="color: #999;">
                        <p>此处为正文内容区域，请在此处输入公文正文...</p>
                        <p>公文正文应使用3号仿宋字，一般每面排22行，每行排28个字。</p>
                    </div>
                </#if>

                <#-- 附件 -->
                <#if document.attachmentInfo?? && document.attachmentInfo != "">
                    <div style="font-size: 16px; margin: 20px 0;">
                        <span style="font-weight: bold;">附件：</span>${document.attachmentInfo}
                    </div>
                </#if>

                <#-- 成文日期 -->
                <div class="written-date">
                    <#if document.writtenDate??>${document.writtenDate}<#else>XXXX年XX月XX日</#if>
                </div>

                <#-- 抄送机关 -->
                <#if document.copySendDept?? && document.copySendDept != "">
                    <div style="font-size: 14px; margin-top: 20px; border-top: 1px solid #ddd; padding-top: 10px;">
                        <span style="font-weight: bold;">抄送：</span>${document.copySendDept}
                    </div>
                </#if>

                <#-- 自定义字段数据展示 -->
                <#if document.fieldData??>
                    <#list fields as field>
                        <#if field.fieldKey != "docNumber" && field.fieldKey != "title" && field.fieldKey != "mainSendDept" && field.fieldKey != "signer" && field.fieldKey != "content" && field.fieldKey != "attachment" && field.fieldKey != "writtenDate" && field.fieldKey != "copySendDept">
                            <div class="field-${field.fieldKey}" style="margin: 10px 0;">
                                <span class="field-label" style="font-weight: bold;">${field.fieldName}：</span>
                                <span class="field-value">[${field.fieldName}]</span>
                            </div>
                        </#if>
                    </#list>
                </#if>

            <#else>
                <#-- 模板预览模式：展示字段占位符 -->
                <#if fields??>
                    <#list fields as field>
                        <#if field.fieldGroup == "main">
                            <div class="field-${field.fieldKey}" style="margin: 15px 0;">
                                <#if field.fieldKey == "title">
                                    <div class="doc-title">[${field.fieldName}]</div>
                                <#elseif field.fieldKey == "content">
                                    <div class="doc-content" style="color: #999;">
                                        <p>[${field.fieldName}]：此处为正文内容区域...</p>
                                    </div>
                                <#else>
                                    <span class="field-label" style="font-weight: bold;">${field.fieldName}：</span>
                                    <span class="field-value" style="color: #999;">[请输入${field.fieldName}]</span>
                                </#if>
                            </div>
                        </#if>
                    </#list>
                </#if>

                <div class="written-date" style="color: #999;">[成文日期]</div>
            </#if>
        </div>
    </div>
</body>
</html>
