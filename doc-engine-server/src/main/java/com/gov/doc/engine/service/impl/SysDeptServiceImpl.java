package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.dto.SysDeptSaveDTO;
import com.gov.doc.engine.entity.SysDept;
import com.gov.doc.engine.entity.SysUnit;
import com.gov.doc.engine.entity.SysUser;
import com.gov.doc.engine.mapper.SysDeptMapper;
import com.gov.doc.engine.mapper.SysUnitMapper;
import com.gov.doc.engine.mapper.SysUserMapper;
import com.gov.doc.engine.service.SysDeptService;
import com.gov.doc.engine.vo.SysDeptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUnitMapper sysUnitMapper;

    @Override
    public List<SysDeptVO> getDeptTree(Long unitId) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getUnitId, unitId).orderByAsc(SysDept::getSortOrder);
        List<SysDept> allDepts = sysDeptMapper.selectList(queryWrapper);
        List<SysDeptVO> voList = allDepts.stream().map(this::convertToVO).collect(Collectors.toList());
        for (SysDeptVO vo : voList) {
            vo.setUserCount(Math.toIntExact(sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, vo.getId()))));
        }
        return buildTree(voList);
    }

    @Override
    public SysDeptVO getDeptDetail(Long id) {
        SysDept dept = sysDeptMapper.selectById(id);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        SysDeptVO vo = convertToVO(dept);
        if (dept.getUnitId() != null) {
            SysUnit unit = sysUnitMapper.selectById(dept.getUnitId());
            if (unit != null) {
                vo.setUnitName(unit.getUnitName());
            }
        }
        if (dept.getParentId() != null) {
            SysDept parent = sysDeptMapper.selectById(dept.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getDeptName());
            }
        }
        vo.setUserCount(Math.toIntExact(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id))));
        return vo;
    }

    @Override
    public Long saveDept(SysDeptSaveDTO saveDTO) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(saveDTO, dept);
        sysDeptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    public void updateDept(SysDeptSaveDTO saveDTO) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(saveDTO, dept);
        sysDeptMapper.updateById(dept);
    }

    @Override
    public void deleteDept(Long id) {
        Long childCount = sysDeptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("该部门下存在子部门，无法删除");
        }
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id));
        if (userCount > 0) {
            throw new RuntimeException("该部门下存在用户，无法删除");
        }
        sysDeptMapper.deleteById(id);
    }

    @Override
    public List<SysDeptVO> getChildren(Long unitId, Long parentId) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getUnitId, unitId).eq(SysDept::getParentId, parentId).orderByAsc(SysDept::getSortOrder);
        List<SysDept> list = sysDeptMapper.selectList(queryWrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private SysDeptVO convertToVO(SysDept dept) {
        SysDeptVO vo = new SysDeptVO();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }

    private List<SysDeptVO> buildTree(List<SysDeptVO> allNodes) {
        List<SysDeptVO> roots = new ArrayList<>();
        for (SysDeptVO node : allNodes) {
            if (node.getParentId() == null) {
                roots.add(node);
            }
        }
        for (SysDeptVO root : roots) {
            setChildren(root, allNodes);
        }
        return roots;
    }

    private void setChildren(SysDeptVO parent, List<SysDeptVO> allNodes) {
        List<SysDeptVO> children = new ArrayList<>();
        for (SysDeptVO node : allNodes) {
            if (parent.getId().equals(node.getParentId())) {
                children.add(node);
            }
        }
        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (SysDeptVO child : children) {
                setChildren(child, allNodes);
            }
        }
    }
}
