package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.dto.DocDocumentUpdateDTO;
import com.gov.doc.engine.dto.DocDocumentValidationDTO;
import com.gov.doc.engine.dto.DocPlaceholderReplaceDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocTemplate;
import com.gov.doc.engine.entity.DocTemplateField;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.DocTemplateFieldMapper;
import com.gov.doc.engine.mapper.DocTemplateMapper;
import com.gov.doc.engine.service.DocDocumentService;
import com.gov.doc.engine.vo.DocPlaceholderResultVO;
import com.gov.doc.engine.vo.DocValidationResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocDocumentServiceImpl extends ServiceImpl<DocDocumentMapper, DocDocument> implements DocDocumentService {

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private DocTemplateMapper docTemplateMapper;

    @Autowired
    private DocTemplateFieldMapper docTemplateFieldMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

    @Override
    public PageResult<DocDocument> pageList(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<DocDocument> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(DocDocument::getDocTitle, keyword)
                    .or()
                    .like(DocDocument::getDocNumber, keyword)
                    .or()
                    .like(DocDocument::getDocContent, keyword));
        }
        queryWrapper.orderByDesc(DocDocument::getCreateTime);

        Page<DocDocument> page = new Page<>(pageNum, pageSize);
        Page<DocDocument> resultPage = docDocumentMapper.selectPage(page, queryWrapper);

        return PageResult.of(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    public DocDocument getDetail(Long id) {
        return docDocumentMapper.selectById(id);
    }

    @Override
    public void updateDocument(DocDocumentUpdateDTO updateDTO) {
        if (updateDTO.getId() == null) {
            throw new RuntimeException("公文ID不能为空");
        }

        DocDocument existingDocument = docDocumentMapper.selectById(updateDTO.getId());
        if (existingDocument == null) {
            throw new RuntimeException("公文不存在");
        }

        DocDocument document = new DocDocument();
        BeanUtils.copyProperties(updateDTO, document);
        document.setId(updateDTO.getId());

        if (updateDTO.getFieldData() != null) {
            try {
                document.setFieldData(objectMapper.writeValueAsString(updateDTO.getFieldData()));
            } catch (Exception e) {
                throw new RuntimeException("字段数据序列化失败", e);
            }
        }

        docDocumentMapper.updateById(document);
    }

    @Override
    public DocValidationResultVO validateDocument(DocDocumentValidationDTO validationDTO) {
        DocValidationResultVO result = new DocValidationResultVO();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String docTitle = validationDTO.getDocTitle();
        String docContent = validationDTO.getDocContent();
        Long templateId = validationDTO.getTemplateId();
        DocDocumentCreateDTO documentData = validationDTO.getDocumentData();

        if (!StringUtils.hasText(docTitle)) {
            errors.add("公文标题不能为空");
        }

        if (!StringUtils.hasText(docContent) || docContent.trim().length() == 0) {
            errors.add("正文内容不能为空");
        } else if (docContent.trim().length() < 10) {
            warnings.add("正文内容较短，建议补充完整");
        }

        if (templateId != null) {
            DocTemplate template = docTemplateMapper.selectById(templateId);
            if (template == null) {
                errors.add("模板不存在");
            } else {
                LambdaQueryWrapper<DocTemplateField> fieldWrapper = new LambdaQueryWrapper<>();
                fieldWrapper.eq(DocTemplateField::getTemplateId, templateId);
                fieldWrapper.eq(DocTemplateField::getIsRequired, 1);
                List<DocTemplateField> requiredFields = docTemplateFieldMapper.selectList(fieldWrapper);

                if (!CollectionUtils.isEmpty(requiredFields) && documentData != null) {
                    Map<String, Object> fieldData = documentData.getFieldData();
                    for (DocTemplateField field : requiredFields) {
                        String fieldKey = field.getFieldKey();
                        String fieldName = field.getFieldName();
                        boolean hasValue = false;

                        if (fieldData != null && fieldData.containsKey(fieldKey)) {
                            Object value = fieldData.get(fieldKey);
                            if (value != null && StringUtils.hasText(value.toString())) {
                                hasValue = true;
                            }
                        }

                        if (!hasValue) {
                            try {
                                java.lang.reflect.Field dtoField = DocDocumentCreateDTO.class.getDeclaredField(fieldKey);
                                dtoField.setAccessible(true);
                                Object value = dtoField.get(documentData);
                                if (value != null && StringUtils.hasText(value.toString())) {
                                    hasValue = true;
                                }
                            } catch (Exception e) {
                                // ignore
                            }
                        }

                        if (!hasValue) {
                            errors.add("必填字段【" + fieldName + "】不能为空");
                        }
                    }
                }

                if (template.getIsCurrentVersion() == null || template.getIsCurrentVersion() != 1) {
                    warnings.add("当前模板不是最新版本，建议使用最新版本");
                }
                if (template.getStatus() == null || template.getStatus() != 1) {
                    warnings.add("当前模板未发布，使用时可能存在问题");
                }
            }
        }

        if (documentData != null) {
            if (!StringUtils.hasText(documentData.getDocNumber())) {
                warnings.add("建议填写公文文号");
            }
            if (!StringUtils.hasText(documentData.getMainSendDept())) {
                warnings.add("建议填写主送机关");
            }
        }

        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        result.setWarnings(warnings);

        return result;
    }

    @Override
    public DocPlaceholderResultVO replacePlaceholders(DocPlaceholderReplaceDTO replaceDTO) {
        DocPlaceholderResultVO result = new DocPlaceholderResultVO();
        String content = replaceDTO.getContent();
        Map<String, String> customPlaceholders = replaceDTO.getPlaceholders();

        Map<String, String> replacementMap = new HashMap<>();
        Map<String, String> presetPlaceholders = getPresetPlaceholders();
        replacementMap.putAll(presetPlaceholders);
        if (customPlaceholders != null) {
            replacementMap.putAll(customPlaceholders);
        }

        List<String> foundPlaceholders = new ArrayList<>();
        List<String> replacedPlaceholders = new ArrayList<>();
        List<String> missingPlaceholders = new ArrayList<>();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        while (matcher.find()) {
            String placeholderKey = matcher.group(1);
            if (!foundPlaceholders.contains(placeholderKey)) {
                foundPlaceholders.add(placeholderKey);
            }
        }

        String processedContent = content;
        for (String placeholderKey : foundPlaceholders) {
            String placeholder = "{{" + placeholderKey + "}}";
            if (replacementMap.containsKey(placeholderKey)) {
                String replacement = replacementMap.get(placeholderKey);
                processedContent = processedContent.replace(placeholder, replacement);
                if (!replacedPlaceholders.contains(placeholderKey)) {
                    replacedPlaceholders.add(placeholderKey);
                }
            } else {
                if (!missingPlaceholders.contains(placeholderKey)) {
                    missingPlaceholders.add(placeholderKey);
                }
            }
        }

        result.setContent(processedContent);
        result.setFoundPlaceholders(foundPlaceholders);
        result.setReplacedPlaceholders(replacedPlaceholders);
        result.setMissingPlaceholders(missingPlaceholders);
        result.setReplacementMap(replacementMap);

        return result;
    }

    @Override
    public Map<String, String> getPresetPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        UserContext currentUser = UserContext.getCurrentUser();
        LocalDate today = LocalDate.now();

        placeholders.put("部门", currentUser.getDeptName());
        placeholders.put("日期", today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        placeholders.put("年份", String.valueOf(today.getYear()));
        placeholders.put("用户", currentUser.getRealName());
        placeholders.put("发文单位", currentUser.getUnitName());
        placeholders.put("发文部门", currentUser.getDeptName());
        placeholders.put("用户名", currentUser.getUsername());
        placeholders.put("单位编码", currentUser.getUnitCode());
        placeholders.put("部门编码", currentUser.getDeptCode());
        placeholders.put("年月日", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        placeholders.put("中文日期", today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));

        return placeholders;
    }

    @Override
    public String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }

        final long KB = 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;
        final long TB = GB * 1024;

        if (fileSize < KB) {
            return fileSize + " B";
        } else if (fileSize < MB) {
            return String.format("%.2f KB", (double) fileSize / KB);
        } else if (fileSize < GB) {
            return String.format("%.2f MB", (double) fileSize / MB);
        } else if (fileSize < TB) {
            return String.format("%.2f GB", (double) fileSize / GB);
        } else {
            return String.format("%.2f TB", (double) fileSize / TB);
        }
    }
}
