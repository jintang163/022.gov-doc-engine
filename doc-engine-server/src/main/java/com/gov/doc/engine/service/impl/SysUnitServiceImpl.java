package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.dto.SysUnitSaveDTO;
import com.gov.doc.engine.entity.SysDept;
import com.gov.doc.engine.entity.SysUnit;
import com.gov.doc.engine.entity.SysUser;
import com.gov.doc.engine.mapper.SysDeptMapper;
import com.gov.doc.engine.mapper.SysUnitMapper;
import com.gov.doc.engine.mapper.SysUserMapper;
import com.gov.doc.engine.service.SysUnitService;
import com.gov.doc.engine.vo.SysUnitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysUnitServiceImpl extends ServiceImpl<SysUnitMapper, SysUnit> implements SysUnitService {

    @Autowired
    private SysUnitMapper sysUnitMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysUnitVO> getUnitTree() {
        LambdaQueryWrapper<SysUnit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysUnit::getSortOrder);
        List<SysUnit> allUnits = sysUnitMapper.selectList(queryWrapper);
        List<SysUnitVO> voList = allUnits.stream().map(this::convertToVO).collect(Collectors.toList());
        for (SysUnitVO vo : voList) {
            vo.setDeptCount(Math.toIntExact(sysDeptMapper.selectCount(
                    new LambdaQueryWrapper<SysDept>().eq(SysDept::getUnitId, vo.getId()))));
            vo.setUserCount(Math.toIntExact(sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUnitId, vo.getId()))));
        }
        return buildTree(voList);
    }

    @Override
    public SysUnitVO getUnitDetail(Long id) {
        SysUnit unit = sysUnitMapper.selectById(id);
        if (unit == null) {
            throw new RuntimeException("单位不存在");
        }
        SysUnitVO vo = convertToVO(unit);
        if (unit.getParentId() != null) {
            SysUnit parent = sysUnitMapper.selectById(unit.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getUnitName());
            }
        }
        vo.setDeptCount(Math.toIntExact(sysDeptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getUnitId, id))));
        vo.setUserCount(Math.toIntExact(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUnitId, id))));
        return vo;
    }

    @Override
    public Long saveUnit(SysUnitSaveDTO saveDTO) {
        SysUnit unit = new SysUnit();
        BeanUtils.copyProperties(saveDTO, unit);
        sysUnitMapper.insert(unit);
        return unit.getId();
    }

    @Override
    public void updateUnit(SysUnitSaveDTO saveDTO) {
        SysUnit unit = new SysUnit();
        BeanUtils.copyProperties(saveDTO, unit);
        sysUnitMapper.updateById(unit);
    }

    @Override
    public void deleteUnit(Long id) {
        Long childCount = sysUnitMapper.selectCount(
                new LambdaQueryWrapper<SysUnit>().eq(SysUnit::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("该单位下存在子单位，无法删除");
        }
        Long deptCount = sysDeptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getUnitId, id));
        if (deptCount > 0) {
            throw new RuntimeException("该单位下存在部门，无法删除");
        }
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUnitId, id));
        if (userCount > 0) {
            throw new RuntimeException("该单位下存在用户，无法删除");
        }
        sysUnitMapper.deleteById(id);
    }

    @Override
    public List<SysUnitVO> getChildren(Long parentId) {
        List<SysUnit> list = sysUnitMapper.selectList(
                new LambdaQueryWrapper<SysUnit>().eq(SysUnit::getParentId, parentId).orderByAsc(SysUnit::getSortOrder));
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private SysUnitVO convertToVO(SysUnit unit) {
        SysUnitVO vo = new SysUnitVO();
        BeanUtils.copyProperties(unit, vo);
        return vo;
    }

    private List<SysUnitVO> buildTree(List<SysUnitVO> allNodes) {
        List<SysUnitVO> roots = new ArrayList<>();
        for (SysUnitVO node : allNodes) {
            if (node.getParentId() == null) {
                roots.add(node);
            }
        }
        for (SysUnitVO root : roots) {
            setChildren(root, allNodes);
        }
        return roots;
    }

    private void setChildren(SysUnitVO parent, List<SysUnitVO> allNodes) {
        List<SysUnitVO> children = new ArrayList<>();
        for (SysUnitVO node : allNodes) {
            if (parent.getId().equals(node.getParentId())) {
                children.add(node);
            }
        }
        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (SysUnitVO child : children) {
                setChildren(child, allNodes);
            }
        }
    }
}
