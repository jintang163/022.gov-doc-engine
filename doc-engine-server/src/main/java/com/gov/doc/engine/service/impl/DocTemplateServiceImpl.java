package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.util.WordTemplateUtil;
import com.gov.doc.engine.dto.DocDocumentCreateDTO;
import com.gov.doc.engine.dto.DocTemplateFieldDTO;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import com.gov.doc.engine.dto.DocTemplateQueryDTO;
import com.gov.doc.engine.dto.DocTemplateSaveDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.entity.DocTemplate;
import com.gov.doc.engine.entity.DocTemplateField;
import com.gov.doc.engine.entity.DocTemplateHeader;
import com.gov.doc.engine.mapper.DocDocumentMapper;
import com.gov.doc.engine.mapper.DocTemplateFieldMapper;
import com.gov.doc.engine.mapper.DocTemplateHeaderMapper;
import com.gov.doc.engine.mapper.DocTemplateMapper;
import com.gov.doc.engine.service.DocTemplateService;
import com.gov.doc.engine.vo.DocTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocTemplateServiceImpl extends ServiceImpl<DocTemplateMapper, DocTemplate> implements DocTemplateService {

    @Autowired
    private DocTemplateMapper docTemplateMapper;

    @Autowired
    private DocTemplateHeaderMapper docTemplateHeaderMapper;

    @Autowired
    private DocTemplateFieldMapper docTemplateFieldMapper;

    @Autowired
    private DocDocumentMapper docDocumentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<DocTemplateVO> pageList(DocTemplateQueryDTO queryDTO) {
        LambdaQueryWrapper<DocTemplate> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getTemplateCode())) {
            queryWrapper.like(DocTemplate::getTemplateCode, queryDTO.getTemplateCode());
        }
        if (StringUtils.hasText(queryDTO.getTemplateName())) {
            queryWrapper.like(DocTemplate::getTemplateName, queryDTO.getTemplateName());
        }
        if (StringUtils.hasText(queryDTO.getTemplateType())) {
            queryWrapper.eq(DocTemplate::getTemplateType, queryDTO.getTemplateType());
        }
        if (StringUtils.hasText(queryDTO.getTemplateCategory())) {
            queryWrapper.eq(DocTemplate::getTemplateCategory, queryDTO.getTemplateCategory());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(DocTemplate::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getIsCurrentVersion() != null) {
            queryWrapper.eq(DocTemplate::getIsCurrentVersion, queryDTO.getIsCurrentVersion());
        }
        if (StringUtils.hasText(queryDTO.getUnitCode())) {
            queryWrapper.eq(DocTemplate::getUnitCode, queryDTO.getUnitCode());
        }
        queryWrapper.orderByDesc(DocTemplate::getCreateTime);

        Page<DocTemplate> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<DocTemplate> resultPage = docTemplateMapper.selectPage(page, queryWrapper);

        List<DocTemplateVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public DocTemplateVO getDetail(Long id) {
        DocTemplate template = docTemplateMapper.selectById(id);
        if (template == null) {
            return null;
        }

        DocTemplateVO vo = convertToVO(template);

        if (template.getHeaderId() != null) {
            DocTemplateHeader header = docTemplateHeaderMapper.selectById(template.getHeaderId());
            if (header != null) {
                DocTemplateHeaderDTO headerDTO = new DocTemplateHeaderDTO();
                BeanUtils.copyProperties(header, headerDTO);
                vo.setHeader(headerDTO);
            }
        }

        LambdaQueryWrapper<DocTemplateField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(DocTemplateField::getTemplateId, id);
        fieldWrapper.orderByAsc(DocTemplateField::getSortOrder);
        List<DocTemplateField> fields = docTemplateFieldMapper.selectList(fieldWrapper);
        if (!CollectionUtils.isEmpty(fields)) {
            List<DocTemplateFieldDTO> fieldDTOList = fields.stream()
                    .map(field -> {
                        DocTemplateFieldDTO fieldDTO = new DocTemplateFieldDTO();
                        BeanUtils.copyProperties(field, fieldDTO);
                        return fieldDTO;
                    })
                    .collect(Collectors.toList());
            vo.setFields(fieldDTOList);
        }

        return vo;
    }

    @Override
    public Long saveTemplate(DocTemplateSaveDTO saveDTO) {
        LambdaQueryWrapper<DocTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocTemplate::getTemplateCode, saveDTO.getTemplateCode());
        queryWrapper.eq(DocTemplate::getVersion, saveDTO.getVersion() != null ? saveDTO.getVersion() : 1);
        Long count = docTemplateMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("模板编码" + saveDTO.getTemplateCode() + "的版本" + (saveDTO.getVersion() != null ? saveDTO.getVersion() : 1) + "已存在");
        }

        DocTemplate template = new DocTemplate();
        BeanUtils.copyProperties(saveDTO, template);
        if (template.getVersion() == null) {
            template.setVersion(1);
        }
        if (template.getStatus() == null) {
            template.setStatus(0);
        }
        if (template.getIsCurrentVersion() == null) {
            template.setIsCurrentVersion(1);
        }
        docTemplateMapper.insert(template);

        if (!CollectionUtils.isEmpty(saveDTO.getFields())) {
            for (DocTemplateFieldDTO fieldDTO : saveDTO.getFields()) {
                DocTemplateField field = new DocTemplateField();
                BeanUtils.copyProperties(fieldDTO, field);
                field.setTemplateId(template.getId());
                docTemplateFieldMapper.insert(field);
            }
        }

        return template.getId();
    }

    @Override
    public void updateTemplate(DocTemplateSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new RuntimeException("模板ID不能为空");
        }

        DocTemplate existingTemplate = docTemplateMapper.selectById(saveDTO.getId());
        if (existingTemplate == null) {
            throw new RuntimeException("模板不存在");
        }
        if (existingTemplate.getStatus() != 0) {
            throw new RuntimeException("只能更新草稿状态的模板");
        }

        DocTemplate template = new DocTemplate();
        BeanUtils.copyProperties(saveDTO, template);
        template.setId(saveDTO.getId());
        docTemplateMapper.updateById(template);

        LambdaQueryWrapper<DocTemplateField> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DocTemplateField::getTemplateId, saveDTO.getId());
        docTemplateFieldMapper.delete(deleteWrapper);

        if (!CollectionUtils.isEmpty(saveDTO.getFields())) {
            for (DocTemplateFieldDTO fieldDTO : saveDTO.getFields()) {
                DocTemplateField field = new DocTemplateField();
                BeanUtils.copyProperties(fieldDTO, field);
                field.setTemplateId(saveDTO.getId());
                docTemplateFieldMapper.insert(field);
            }
        }
    }

    @Override
    public void deleteTemplate(Long id) {
        DocTemplate template = docTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 0) {
            throw new RuntimeException("只能删除草稿状态的模板");
        }
        docTemplateMapper.deleteById(id);
    }

    @Override
    public Long createNewVersion(Long id) {
        DocTemplate oldTemplate = docTemplateMapper.selectById(id);
        if (oldTemplate == null) {
            throw new RuntimeException("模板不存在");
        }

        DocTemplate newTemplate = new DocTemplate();
        BeanUtils.copyProperties(oldTemplate, newTemplate);
        newTemplate.setId(null);
        newTemplate.setVersion(oldTemplate.getVersion() + 1);
        newTemplate.setIsCurrentVersion(1);
        newTemplate.setParentTemplateId(oldTemplate.getId());
        newTemplate.setStatus(0);
        docTemplateMapper.insert(newTemplate);

        LambdaQueryWrapper<DocTemplate> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(DocTemplate::getTemplateCode, oldTemplate.getTemplateCode());
        updateWrapper.ne(DocTemplate::getId, newTemplate.getId());
        DocTemplate updateEntity = new DocTemplate();
        updateEntity.setIsCurrentVersion(0);
        docTemplateMapper.update(updateEntity, updateWrapper);

        LambdaQueryWrapper<DocTemplateField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(DocTemplateField::getTemplateId, id);
        List<DocTemplateField> oldFields = docTemplateFieldMapper.selectList(fieldWrapper);
        if (!CollectionUtils.isEmpty(oldFields)) {
            for (DocTemplateField oldField : oldFields) {
                DocTemplateField newField = new DocTemplateField();
                BeanUtils.copyProperties(oldField, newField);
                newField.setId(null);
                newField.setTemplateId(newTemplate.getId());
                docTemplateFieldMapper.insert(newField);
            }
        }

        return newTemplate.getId();
    }

    @Override
    public void publish(Long id) {
        DocTemplate template = docTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 0) {
            throw new RuntimeException("只能发布草稿状态的模板");
        }

        DocTemplate updateEntity = new DocTemplate();
        updateEntity.setId(id);
        updateEntity.setStatus(1);
        docTemplateMapper.updateById(updateEntity);
    }

    @Override
    public void disable(Long id) {
        DocTemplate template = docTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 1) {
            throw new RuntimeException("只能停用已发布状态的模板");
        }

        DocTemplate updateEntity = new DocTemplate();
        updateEntity.setId(id);
        updateEntity.setStatus(2);
        docTemplateMapper.updateById(updateEntity);
    }

    @Override
    public List<DocTemplateVO> listAvailable() {
        UserContext currentUser = UserContext.getCurrentUser();
        
        LambdaQueryWrapper<DocTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocTemplate::getStatus, 1);
        queryWrapper.eq(DocTemplate::getIsCurrentVersion, 1);
        queryWrapper.orderByDesc(DocTemplate::getCreateTime);

        List<DocTemplate> templates = docTemplateMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(templates)) {
            return new ArrayList<>();
        }

        return templates.stream()
                .filter(template -> currentUser.hasPermission(
                        template.getPermissionRoles(),
                        template.getPermissionUsers(),
                        template.getPermissionDepts()))
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DocDocument createDocumentFromTemplate(DocDocumentCreateDTO createDTO) {
        DocTemplate template = docTemplateMapper.selectById(createDTO.getTemplateId());
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 1 || template.getIsCurrentVersion() != 1) {
            throw new RuntimeException("模板未发布或不是当前版本");
        }

        UserContext currentUser = UserContext.getCurrentUser();
        if (!currentUser.hasPermission(
                template.getPermissionRoles(),
                template.getPermissionUsers(),
                template.getPermissionDepts())) {
            throw new RuntimeException("您没有使用该模板的权限");
        }

        DocDocument document = new DocDocument();
        BeanUtils.copyProperties(createDTO, document);
        document.setTemplateId(createDTO.getTemplateId());
        document.setDocType(template.getTemplateType());
        if (!StringUtils.hasText(document.getSecurityLevel())) {
            document.setSecurityLevel(template.getSecurityLevel());
        }
        if (!StringUtils.hasText(document.getUrgencyLevel())) {
            document.setUrgencyLevel(template.getUrgencyLevel());
        }
        if (!StringUtils.hasText(document.getUnitCode())) {
            document.setUnitCode(template.getUnitCode());
        }
        if (!StringUtils.hasText(document.getUnitName())) {
            document.setUnitName(template.getUnitName());
        }
        if (!StringUtils.hasText(document.getStatus())) {
            document.setStatus("0");
        }

        if (createDTO.getFieldData() != null) {
            try {
                document.setFieldData(objectMapper.writeValueAsString(createDTO.getFieldData()));
            } catch (Exception e) {
                throw new RuntimeException("字段数据序列化失败", e);
            }
        }

        docDocumentMapper.insert(document);
        return document;
    }

    private DocTemplateVO convertToVO(DocTemplate template) {
        DocTemplateVO vo = new DocTemplateVO();
        BeanUtils.copyProperties(template, vo);
        if (template.getStatus() != null) {
            switch (template.getStatus()) {
                case 0:
                    vo.setStatusName("草稿");
                    break;
                case 1:
                    vo.setStatusName("已发布");
                    break;
                case 2:
                    vo.setStatusName("已停用");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }
        return vo;
    }

    @Override
    public String uploadWordTemplate(Long templateId, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的Word文件");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || (!originalFileName.endsWith(".docx") && !originalFileName.endsWith(".doc"))) {
            throw new RuntimeException("只支持.docx和.doc格式的Word文件");
        }

        DocTemplate template = docTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 0) {
            throw new RuntimeException("只能修改草稿状态的模板");
        }

        try {
            String oldFilePath = template.getTemplateFilePath();
            String filePath = WordTemplateUtil.saveTemplateFile(file, templateId);
            List<String> variables = WordTemplateUtil.extractVariables(file);
            
            template.setTemplateFilePath(filePath);
            template.setTemplateFileName(originalFileName);
            template.setTemplateVariables(objectMapper.writeValueAsString(variables));
            docTemplateMapper.updateById(template);

            if (oldFilePath != null && !oldFilePath.isEmpty()) {
                WordTemplateUtil.deleteTemplateFile(oldFilePath);
            }

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Word模板上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> extractVariablesFromWord(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择Word文件");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || (!originalFileName.endsWith(".docx") && !originalFileName.endsWith(".doc"))) {
            throw new RuntimeException("只支持.docx和.doc格式的Word文件");
        }

        try {
            return WordTemplateUtil.extractVariables(file);
        } catch (Exception e) {
            throw new RuntimeException("解析Word模板变量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateWordDocument(Long templateId, DocDocumentCreateDTO createDTO) {
        DocTemplate template = docTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getStatus() != 1 || template.getIsCurrentVersion() != 1) {
            throw new RuntimeException("模板未发布或不是当前版本");
        }
        if (template.getTemplateFilePath() == null || template.getTemplateFilePath().isEmpty()) {
            throw new RuntimeException("该模板未上传Word模板文件");
        }

        UserContext currentUser = UserContext.getCurrentUser();
        if (!currentUser.hasPermission(
                template.getPermissionRoles(),
                template.getPermissionUsers(),
                template.getPermissionDepts())) {
            throw new RuntimeException("您没有使用该模板的权限");
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            
            variables.put("docTitle", createDTO.getDocTitle());
            variables.put("docNumber", createDTO.getDocNumber());
            variables.put("docType", createDTO.getDocType());
            variables.put("securityLevel", createDTO.getSecurityLevel());
            variables.put("urgencyLevel", createDTO.getUrgencyLevel());
            variables.put("mainSendDept", createDTO.getMainSendDept());
            variables.put("copySendDept", createDTO.getCopySendDept());
            variables.put("signer", createDTO.getSigner());
            variables.put("signDate", createDTO.getSignDate());
            variables.put("writtenDate", createDTO.getWrittenDate());
            variables.put("docContent", createDTO.getDocContent());
            variables.put("attachmentInfo", createDTO.getAttachmentInfo());
            variables.put("remark", createDTO.getRemark());
            
            if (createDTO.getFieldData() != null) {
                variables.putAll(createDTO.getFieldData());
            }

            return WordTemplateUtil.generateDocument(template.getTemplateFilePath(), variables);
        } catch (Exception e) {
            throw new RuntimeException("生成Word文档失败: " + e.getMessage(), e);
        }
    }
}
