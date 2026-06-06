package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.dto.DocTemplateHeaderDTO;
import com.gov.doc.engine.entity.DocTemplate;
import com.gov.doc.engine.entity.DocTemplateHeader;
import com.gov.doc.engine.mapper.DocTemplateHeaderMapper;
import com.gov.doc.engine.mapper.DocTemplateMapper;
import com.gov.doc.engine.service.DocTemplateHeaderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocTemplateHeaderServiceImpl extends ServiceImpl<DocTemplateHeaderMapper, DocTemplateHeader> implements DocTemplateHeaderService {

    @Autowired
    private DocTemplateHeaderMapper docTemplateHeaderMapper;

    @Autowired
    private DocTemplateMapper docTemplateMapper;

    @Override
    public List<DocTemplateHeaderDTO> listAll() {
        LambdaQueryWrapper<DocTemplateHeader> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DocTemplateHeader::getCreateTime);
        List<DocTemplateHeader> list = docTemplateHeaderMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return java.util.Collections.emptyList();
        }
        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocTemplateHeaderDTO getDetail(Long id) {
        DocTemplateHeader header = docTemplateHeaderMapper.selectById(id);
        if (header == null) {
            return null;
        }
        return convertToDTO(header);
    }

    @Override
    public Long saveHeader(DocTemplateHeaderDTO dto) {
        LambdaQueryWrapper<DocTemplateHeader> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocTemplateHeader::getHeaderName, dto.getHeaderName());
        Long count = docTemplateHeaderMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("红头配置名称已存在：" + dto.getHeaderName());
        }

        DocTemplateHeader header = new DocTemplateHeader();
        BeanUtils.copyProperties(dto, header);
        docTemplateHeaderMapper.insert(header);
        return header.getId();
    }

    @Override
    public void updateHeader(DocTemplateHeaderDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("红头配置ID不能为空");
        }

        DocTemplateHeader existing = docTemplateHeaderMapper.selectById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("红头配置不存在");
        }

        if (!existing.getHeaderName().equals(dto.getHeaderName())) {
            LambdaQueryWrapper<DocTemplateHeader> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocTemplateHeader::getHeaderName, dto.getHeaderName());
            queryWrapper.ne(DocTemplateHeader::getId, dto.getId());
            Long count = docTemplateHeaderMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new RuntimeException("红头配置名称已存在：" + dto.getHeaderName());
            }
        }

        DocTemplateHeader header = new DocTemplateHeader();
        BeanUtils.copyProperties(dto, header);
        header.setId(dto.getId());
        docTemplateHeaderMapper.updateById(header);
    }

    @Override
    public void deleteHeader(Long id) {
        DocTemplateHeader header = docTemplateHeaderMapper.selectById(id);
        if (header == null) {
            throw new RuntimeException("红头配置不存在");
        }

        LambdaQueryWrapper<DocTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.eq(DocTemplate::getHeaderId, id);
        Long templateCount = docTemplateMapper.selectCount(templateWrapper);
        if (templateCount > 0) {
            throw new RuntimeException("该红头配置已被" + templateCount + "个模板引用，无法删除");
        }

        docTemplateHeaderMapper.deleteById(id);
    }

    private DocTemplateHeaderDTO convertToDTO(DocTemplateHeader header) {
        DocTemplateHeaderDTO dto = new DocTemplateHeaderDTO();
        BeanUtils.copyProperties(header, dto);
        return dto;
    }
}
