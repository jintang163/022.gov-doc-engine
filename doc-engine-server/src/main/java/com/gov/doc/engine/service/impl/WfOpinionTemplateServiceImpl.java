package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.WfOpinionTemplateQueryDTO;
import com.gov.doc.engine.dto.WfOpinionTemplateSaveDTO;
import com.gov.doc.engine.entity.WfOpinionTemplate;
import com.gov.doc.engine.mapper.WfOpinionTemplateMapper;
import com.gov.doc.engine.service.WfOpinionTemplateService;
import com.gov.doc.engine.vo.WfOpinionTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WfOpinionTemplateServiceImpl extends ServiceImpl<WfOpinionTemplateMapper, WfOpinionTemplate> implements WfOpinionTemplateService {

    @Autowired
    private WfOpinionTemplateMapper opinionTemplateMapper;

    @Override
    public List<WfOpinionTemplateVO> list(WfOpinionTemplateQueryDTO queryDTO) {
        LambdaQueryWrapper<WfOpinionTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getTemplateType())) {
            wrapper.eq(WfOpinionTemplate::getTemplateType, queryDTO.getTemplateType());
        }
        if (StringUtils.hasText(queryDTO.getUserId())) {
            wrapper.eq(WfOpinionTemplate::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getIsPreset() != null) {
            wrapper.eq(WfOpinionTemplate::getIsPreset, queryDTO.getIsPreset());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(WfOpinionTemplate::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(WfOpinionTemplate::getTemplateContent, queryDTO.getKeyword());
        }

        wrapper.orderByAsc(WfOpinionTemplate::getSortOrder)
                .orderByDesc(WfOpinionTemplate::getCreateTime);

        List<WfOpinionTemplate> list = opinionTemplateMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public WfOpinionTemplateVO getDetail(Long id) {
        WfOpinionTemplate template = this.getById(id);
        return template != null ? convertToVO(template) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTemplate(WfOpinionTemplateSaveDTO saveDTO) {
        UserContext currentUser = UserContext.getCurrentUser();

        WfOpinionTemplate template = new WfOpinionTemplate();
        BeanUtils.copyProperties(saveDTO, template);

        template.setUserId(String.valueOf(currentUser.getUserId()));
        template.setUserName(currentUser.getRealName());
        template.setIsPreset(0);
        template.setStatus(1);
        if (template.getSortOrder() == null) {
            template.setSortOrder(0);
        }

        this.save(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(WfOpinionTemplateSaveDTO saveDTO) {
        if (saveDTO.getId() == null) {
            throw new RuntimeException("常用语ID不能为空");
        }
        WfOpinionTemplate template = this.getById(saveDTO.getId());
        if (template == null) {
            throw new RuntimeException("常用语不存在");
        }

        UserContext currentUser = UserContext.getCurrentUser();
        if (!template.getUserId().equals(String.valueOf(currentUser.getUserId())) && template.getIsPreset() != 1) {
            throw new RuntimeException("您无权修改此常用语");
        }

        BeanUtils.copyProperties(saveDTO, template);
        this.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        WfOpinionTemplate template = this.getById(id);
        if (template == null) {
            throw new RuntimeException("常用语不存在");
        }

        UserContext currentUser = UserContext.getCurrentUser();
        if (!template.getUserId().equals(String.valueOf(currentUser.getUserId())) && template.getIsPreset() != 1) {
            throw new RuntimeException("您无权删除此常用语");
        }

        this.removeById(id);
    }

    @Override
    public WfOpinionTemplateVO convertToVO(WfOpinionTemplate template) {
        WfOpinionTemplateVO vo = new WfOpinionTemplateVO();
        BeanUtils.copyProperties(template, vo);

        vo.setTemplateTypeName(getTemplateTypeName(template.getTemplateType()));
        vo.setIsPresetName(template.getIsPreset() != null && template.getIsPreset() == 1 ? "是" : "否");
        vo.setStatusName(template.getStatus() != null && template.getStatus() == 1 ? "启用" : "禁用");

        return vo;
    }

    private String getTemplateTypeName(String type) {
        if ("agree".equals(type)) {
            return "同意";
        } else if ("reject".equals(type)) {
            return "驳回";
        } else if ("return".equals(type)) {
            return "退回修改";
        } else if ("delegate".equals(type)) {
            return "转办";
        } else if ("addSign".equals(type)) {
            return "加签";
        } else if ("countersign".equals(type)) {
            return "会签";
        } else if ("terminate".equals(type)) {
            return "终止";
        } else if ("abstain".equals(type)) {
            return "弃权";
        } else {
            return "通用";
        }
    }
}
