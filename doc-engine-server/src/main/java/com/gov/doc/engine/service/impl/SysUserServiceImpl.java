package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.SysUserQueryDTO;
import com.gov.doc.engine.dto.SysUserSaveDTO;
import com.gov.doc.engine.entity.SysDept;
import com.gov.doc.engine.entity.SysPost;
import com.gov.doc.engine.entity.SysUnit;
import com.gov.doc.engine.entity.SysUser;
import com.gov.doc.engine.mapper.SysDeptMapper;
import com.gov.doc.engine.mapper.SysPostMapper;
import com.gov.doc.engine.mapper.SysUnitMapper;
import com.gov.doc.engine.mapper.SysUserMapper;
import com.gov.doc.engine.service.SysUserService;
import com.gov.doc.engine.vo.SysUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUnitMapper sysUnitMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private SysPostMapper sysPostMapper;

    @Override
    public PageResult<SysUserVO> pageList(Integer pageNum, Integer pageSize, SysUserQueryDTO queryDTO) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByAsc(SysUser::getSortOrder);
        List<SysUser> list = sysUserMapper.selectList(queryWrapper);
        PageInfo<SysUser> pageInfo = new PageInfo<>(list);
        List<SysUserVO> voList = pageInfo.getList().stream().map(this::enrichVO).collect(Collectors.toList());
        return PageResult.of(pageInfo.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public SysUserVO getUserDetail(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return enrichVO(user);
    }

    @Override
    public Long saveUser(SysUserSaveDTO saveDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(saveDTO, user);
        sysUserMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(SysUserSaveDTO saveDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(saveDTO, user);
        sysUserMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }

    @Override
    public List<SysUserVO> listByDept(Long deptId) {
        List<SysUser> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId).orderByAsc(SysUser::getSortOrder));
        return list.stream().map(this::enrichVO).collect(Collectors.toList());
    }

    @Override
    public List<SysUserVO> listByPost(Long postId) {
        List<SysUser> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPostId, postId).orderByAsc(SysUser::getSortOrder));
        return list.stream().map(this::enrichVO).collect(Collectors.toList());
    }

    @Override
    public List<SysUserVO> listByUnit(Long unitId) {
        List<SysUser> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUnitId, unitId).orderByAsc(SysUser::getSortOrder));
        return list.stream().map(this::enrichVO).collect(Collectors.toList());
    }

    @Override
    public List<SysUserVO> searchUsers(String keyword) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(SysUser::getUserCode, keyword)
                .or().like(SysUser::getUserName, keyword)
                .or().like(SysUser::getLoginName, keyword);
        queryWrapper.orderByAsc(SysUser::getSortOrder);
        List<SysUser> list = sysUserMapper.selectList(queryWrapper);
        return list.stream().map(this::enrichVO).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<SysUser> buildQueryWrapper(SysUserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return queryWrapper;
        }
        if (StringUtils.hasText(queryDTO.getUserCode())) {
            queryWrapper.like(SysUser::getUserCode, queryDTO.getUserCode());
        }
        if (StringUtils.hasText(queryDTO.getUserName())) {
            queryWrapper.like(SysUser::getUserName, queryDTO.getUserName());
        }
        if (queryDTO.getUnitId() != null) {
            queryWrapper.eq(SysUser::getUnitId, queryDTO.getUnitId());
        }
        if (queryDTO.getDeptId() != null) {
            queryWrapper.eq(SysUser::getDeptId, queryDTO.getDeptId());
        }
        if (queryDTO.getPostId() != null) {
            queryWrapper.eq(SysUser::getPostId, queryDTO.getPostId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(SysUser::getUserCode, keyword)
                    .or()
                    .like(SysUser::getUserName, keyword)
                    .or()
                    .like(SysUser::getLoginName, keyword));
        }
        return queryWrapper;
    }

    private SysUserVO convertToVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private SysUserVO enrichVO(SysUser user) {
        SysUserVO vo = convertToVO(user);
        if (user.getUnitId() != null) {
            SysUnit unit = sysUnitMapper.selectById(user.getUnitId());
            if (unit != null) {
                vo.setUnitName(unit.getUnitName());
            }
        }
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
