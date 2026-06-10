package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.SysPostQueryDTO;
import com.gov.doc.engine.dto.SysPostSaveDTO;
import com.gov.doc.engine.entity.SysDept;
import com.gov.doc.engine.entity.SysPost;
import com.gov.doc.engine.entity.SysUnit;
import com.gov.doc.engine.mapper.SysDeptMapper;
import com.gov.doc.engine.mapper.SysPostMapper;
import com.gov.doc.engine.mapper.SysUnitMapper;
import com.gov.doc.engine.service.SysPostService;
import com.gov.doc.engine.vo.SysPostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    @Autowired
    private SysPostMapper sysPostMapper;

    @Autowired
    private SysUnitMapper sysUnitMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public PageResult<SysPostVO> pageList(Integer pageNum, Integer pageSize, SysPostQueryDTO queryDTO) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<SysPost> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByAsc(SysPost::getSortOrder);
        List<SysPost> list = sysPostMapper.selectList(queryWrapper);
        PageInfo<SysPost> pageInfo = new PageInfo<>(list);
        List<SysPostVO> voList = pageInfo.getList().stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(pageInfo.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public SysPostVO getPostDetail(Long id) {
        SysPost post = sysPostMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("岗位不存在");
        }
        SysPostVO vo = convertToVO(post);
        if (post.getUnitId() != null) {
            SysUnit unit = sysUnitMapper.selectById(post.getUnitId());
            if (unit != null) {
                vo.setUnitName(unit.getUnitName());
            }
        }
        if (post.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(post.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }

    @Override
    public Long savePost(SysPostSaveDTO saveDTO) {
        SysPost post = new SysPost();
        BeanUtils.copyProperties(saveDTO, post);
        sysPostMapper.insert(post);
        return post.getId();
    }

    @Override
    public void updatePost(SysPostSaveDTO saveDTO) {
        SysPost post = new SysPost();
        BeanUtils.copyProperties(saveDTO, post);
        sysPostMapper.updateById(post);
    }

    @Override
    public void deletePost(Long id) {
        sysPostMapper.deleteById(id);
    }

    @Override
    public List<SysPostVO> listAll(Long unitId) {
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPost::getUnitId, unitId).eq(SysPost::getStatus, 1).orderByAsc(SysPost::getSortOrder);
        List<SysPost> list = sysPostMapper.selectList(queryWrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<SysPost> buildQueryWrapper(SysPostQueryDTO queryDTO) {
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return queryWrapper;
        }
        if (queryDTO.getUnitId() != null) {
            queryWrapper.eq(SysPost::getUnitId, queryDTO.getUnitId());
        }
        if (queryDTO.getDeptId() != null) {
            queryWrapper.eq(SysPost::getDeptId, queryDTO.getDeptId());
        }
        if (StringUtils.hasText(queryDTO.getPostCode())) {
            queryWrapper.like(SysPost::getPostCode, queryDTO.getPostCode());
        }
        if (StringUtils.hasText(queryDTO.getPostName())) {
            queryWrapper.like(SysPost::getPostName, queryDTO.getPostName());
        }
        if (StringUtils.hasText(queryDTO.getPostType())) {
            queryWrapper.eq(SysPost::getPostType, queryDTO.getPostType());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysPost::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(SysPost::getPostCode, keyword)
                    .or()
                    .like(SysPost::getPostName, keyword));
        }
        return queryWrapper;
    }

    private SysPostVO convertToVO(SysPost post) {
        SysPostVO vo = new SysPostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
