package com.gov.doc.engine.service.impl;

import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.service.DocPreviewService;
import com.gov.doc.engine.vo.DocTemplateVO;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DocPreviewServiceImpl implements DocPreviewService {

    @Autowired
    private Configuration freeMarkerConfiguration;

    @Override
    public String generateTemplatePreviewHtml(DocTemplateVO templateVO) {
        if (templateVO == null || templateVO.getHeader() == null) {
            throw new RuntimeException("模板或红头样式不能为空");
        }

        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("header", templateVO.getHeader());
            dataModel.put("fields", templateVO.getFields());
            dataModel.put("template", templateVO);

            Template template = freeMarkerConfiguration.getTemplate("doc_preview.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);
        } catch (Exception e) {
            log.error("生成模板预览HTML失败", e);
            throw new RuntimeException("生成模板预览HTML失败：" + e.getMessage());
        }
    }

    @Override
    public String generateDocumentPreviewHtml(DocTemplateVO templateVO, DocDocumentCreateDTO documentDTO) {
        if (templateVO == null || templateVO.getHeader() == null) {
            throw new RuntimeException("模板或红头样式不能为空");
        }

        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("header", templateVO.getHeader());
            dataModel.put("fields", templateVO.getFields());
            dataModel.put("template", templateVO);
            dataModel.put("document", documentDTO);

            Template template = freeMarkerConfiguration.getTemplate("doc_preview.ftl");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);
        } catch (Exception e) {
            log.error("生成公文预览HTML失败", e);
            throw new RuntimeException("生成公文预览HTML失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] generateTemplatePreviewPdf(DocTemplateVO templateVO) {
        String html = generateTemplatePreviewHtml(templateVO);
        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] generateDocumentPreviewPdf(DocTemplateVO templateVO, DocDocumentCreateDTO documentDTO) {
        String html = generateDocumentPreviewHtml(templateVO, documentDTO);
        return html.getBytes(StandardCharsets.UTF_8);
    }
}
