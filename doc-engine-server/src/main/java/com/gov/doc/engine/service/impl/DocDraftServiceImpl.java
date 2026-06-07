package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocDraftSaveDTO;
import com.gov.doc.engine.entity.DocDraft;
import com.gov.doc.engine.entity.DocTemplate;
import com.gov.doc.engine.mapper.DocDraftMapper;
import com.gov.doc.engine.mapper.DocTemplateMapper;
import com.gov.doc.engine.service.DocDraftService;
import com.gov.doc.engine.vo.DocDraftVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocDraftServiceImpl extends ServiceImpl<DocDraftMapper, DocDraft> implements DocDraftService {

    @Autowired
    private DocDraftMapper docDraftMapper;

    @Autowired
    private DocTemplateMapper docTemplateMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<DocDraftVO> pageList(Integer pageNum, Integer pageSize) {
        UserContext currentUser = UserContext.getCurrentUser();

        LambdaQueryWrapper<DocDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDraft::getCreateBy, currentUser.getUsername());
        queryWrapper.orderByDesc(DocDraft::getCreateTime);

        Page<DocDraft> page = new Page<>(pageNum, pageSize);
        Page<DocDraft> resultPage = docDraftMapper.selectPage(page, queryWrapper);

        List<DocDraftVO> voList = convertToVOList(resultPage.getRecords());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<DocDraftVO> listByTemplateId(Long templateId) {
        UserContext currentUser = UserContext.getCurrentUser();

        LambdaQueryWrapper<DocDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDraft::getTemplateId, templateId);
        queryWrapper.eq(DocDraft::getCreateBy, currentUser.getUsername());
        queryWrapper.orderByDesc(DocDraft::getCreateTime);

        List<DocDraft> drafts = docDraftMapper.selectList(queryWrapper);
        return convertToVOList(drafts);
    }

    @Override
    public DocDraftVO getDetail(Long id) {
        DocDraft draft = docDraftMapper.selectById(id);
        if (draft == null) {
            return null;
        }
        return convertToVO(draft);
    }

    @Override
    public DocDraftVO getLatestByTemplateId(Long templateId) {
        UserContext currentUser = UserContext.getCurrentUser();

        LambdaQueryWrapper<DocDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocDraft::getTemplateId, templateId);
        queryWrapper.eq(DocDraft::getCreateBy, currentUser.getUsername());
        queryWrapper.orderByDesc(DocDraft::getLastSaveTime);
        queryWrapper.last("LIMIT 1");

        DocDraft draft = docDraftMapper.selectOne(queryWrapper);
        if (draft == null) {
            return null;
        }
        return convertToVO(draft);
    }

    @Override
    public Long saveDraft(DocDraftSaveDTO saveDTO) {
        DocDraft draft = convertToEntity(saveDTO);
        draft.setAutoSave(0);
        draft.setLastSaveTime(LocalDateTime.now());

        if (draft.getId() == null) {
            docDraftMapper.insert(draft);
        } else {
            docDraftMapper.updateById(draft);
        }

        return draft.getId();
    }

    @Override
    public Long autoSaveDraft(DocDraftSaveDTO saveDTO) {
        DocDraft draft = convertToEntity(saveDTO);
        draft.setAutoSave(1);
        draft.setLastSaveTime(LocalDateTime.now());

        if (draft.getId() == null) {
            docDraftMapper.insert(draft);
        } else {
            docDraftMapper.updateById(draft);
        }

        return draft.getId();
    }

    @Override
    public void deleteDraft(Long id) {
        DocDraft draft = docDraftMapper.selectById(id);
        if (draft == null) {
            throw new RuntimeException("草稿不存在");
        }
        docDraftMapper.deleteById(id);
    }

    @Override
    public DocDraft convertToEntity(DocDraftSaveDTO saveDTO) {
        DocDraft draft = new DocDraft();
        BeanUtils.copyProperties(saveDTO, draft);

        if (saveDTO.getFieldData() != null) {
            try {
                draft.setFieldData(objectMapper.writeValueAsString(saveDTO.getFieldData()));
            } catch (Exception e) {
                throw new RuntimeException("字段数据序列化失败", e);
            }
        }

        return draft;
    }

    @Override
    public DocDraftVO convertToVO(DocDraft draft) {
        DocDraftVO vo = new DocDraftVO();
        BeanUtils.copyProperties(draft, vo);

        if (draft.getFieldData() != null && !draft.getFieldData().isEmpty()) {
            try {
                objectMapper.readTree(draft.getFieldData());
            } catch (Exception e) {
                throw new RuntimeException("字段数据格式错误，不是有效的JSON", e);
            }
        }

        if (draft.getTemplateId() != null) {
            DocTemplate template = docTemplateMapper.selectById(draft.getTemplateId());
            if (template != null) {
                vo.setTemplateName(template.getTemplateName());
            }
        }

        return vo;
    }

    private List<DocDraftVO> convertToVOList(List<DocDraft> drafts) {
        if (CollectionUtils.isEmpty(drafts)) {
            return new ArrayList<>();
        }

        List<Long> templateIds = drafts.stream()
                .map(DocDraft::getTemplateId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> templateNameMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(templateIds)) {
            LambdaQueryWrapper<DocTemplate> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.in(DocTemplate::getId, templateIds);
            List<DocTemplate> templates = docTemplateMapper.selectList(templateWrapper);
            for (DocTemplate template : templates) {
                templateNameMap.put(template.getId(), template.getTemplateName());
            }
        }

        return drafts.stream()
                .map(draft -> {
                    DocDraftVO vo = new DocDraftVO();
                    BeanUtils.copyProperties(draft, vo);

                    if (draft.getFieldData() != null && !draft.getFieldData().isEmpty()) {
                        try {
                            objectMapper.readTree(draft.getFieldData());
                        } catch (Exception e) {
                            throw new RuntimeException("字段数据格式错误，不是有效的JSON", e);
                        }
                    }

                    vo.setTemplateName(templateNameMap.get(draft.getTemplateId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
