package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocPermissionDTO;
import com.gov.doc.engine.entity.DocPermission;
import com.gov.doc.engine.mapper.DocPermissionMapper;
import com.gov.doc.engine.service.DocPermissionService;
import com.gov.doc.engine.vo.DocPermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocPermissionServiceImpl extends ServiceImpl<DocPermissionMapper, DocPermission> implements DocPermissionService {

    @Autowired
    private DocPermissionMapper docPermissionMapper;

    private static final Map<String, String> TYPE_NAMES = new HashMap<>();

    static {
        TYPE_NAMES.put("action", "操作权限");
        TYPE_NAMES.put("data", "数据权限");
    }

    @Override
    public PageResult<DocPermissionVO> pageList(Integer pageNum, Integer pageSize, String permissionType, String permissionCode) {
        LambdaQueryWrapper<DocPermission> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(permissionType)) {
            queryWrapper.eq(DocPermission::getPermissionType, permissionType);
        }
        if (StringUtils.hasText(permissionCode)) {
            queryWrapper.like(DocPermission::getPermissionCode, permissionCode);
        }
        queryWrapper.orderByAsc(DocPermission::getSortOrder);

        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 20;
        Page<DocPermission> page = new Page<>(pn, ps);
        Page<DocPermission> resultPage = docPermissionMapper.selectPage(page, queryWrapper);

        java.util.List<DocPermissionVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pn, ps);
    }

    @Override
    public java.util.List<DocPermissionVO> listAll() {
        LambdaQueryWrapper<DocPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocPermission::getEnabled, 1)
                .orderByAsc(DocPermission::getSortOrder);
        return docPermissionMapper.selectList(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DocPermissionVO getByCode(String permissionCode) {
        LambdaQueryWrapper<DocPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocPermission::getPermissionCode, permissionCode);
        DocPermission permission = docPermissionMapper.selectOne(queryWrapper);
        return permission != null ? convertToVO(permission) : null;
    }

    @Override
    public DocPermissionVO create(DocPermissionDTO dto) {
        DocPermission permission = new DocPermission();
        BeanUtils.copyProperties(dto, permission);
        if (permission.getEnabled() == null) {
            permission.setEnabled(1);
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        docPermissionMapper.insert(permission);
        return convertToVO(permission);
    }

    @Override
    public DocPermissionVO update(Long id, DocPermissionDTO dto) {
        DocPermission permission = docPermissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        if (StringUtils.hasText(dto.getPermissionName())) {
            permission.setPermissionName(dto.getPermissionName());
        }
        if (StringUtils.hasText(dto.getPermissionType())) {
            permission.setPermissionType(dto.getPermissionType());
        }
        if (dto.getSecurityLevel() != null) {
            permission.setSecurityLevel(dto.getSecurityLevel());
        }
        permission.setAllowedRoles(dto.getAllowedRoles());
        permission.setAllowedUsers(dto.getAllowedUsers());
        permission.setAllowedDepts(dto.getAllowedDepts());
        permission.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
            permission.setEnabled(dto.getEnabled());
        }
        if (dto.getSortOrder() != null) {
            permission.setSortOrder(dto.getSortOrder());
        }
        docPermissionMapper.updateById(permission);
        return convertToVO(permission);
    }

    @Override
    public void delete(Long id) {
        docPermissionMapper.deleteById(id);
    }

    @Override
    public boolean checkPermission(String permissionCode) {
        DocPermissionVO permission = getByCode(permissionCode);
        if (permission == null || permission.getEnabled() != 1) {
            return true;
        }

        UserContext currentUser = UserContext.getCurrentUser();
        return currentUser.hasPermission(
                permission.getAllowedRoles(),
                permission.getAllowedUsers(),
                permission.getAllowedDepts()
        );
    }

    @Override
    public boolean checkSecurityLevelAccess(String userId, String securityLevel) {
        if (securityLevel == null || securityLevel.isEmpty() || "普通".equals(securityLevel)) {
            return true;
        }

        String requiredPermission;
        switch (securityLevel) {
            case "秘密":
                requiredPermission = "doc:view:secret";
                break;
            case "机密":
                requiredPermission = "doc:view:confidential";
                break;
            case "绝密":
                requiredPermission = "doc:view:top_secret";
                break;
            default:
                return true;
        }

        return checkPermission(requiredPermission);
    }

    private DocPermissionVO convertToVO(DocPermission permission) {
        DocPermissionVO vo = new DocPermissionVO();
        BeanUtils.copyProperties(permission, vo);
        vo.setPermissionTypeName(TYPE_NAMES.getOrDefault(permission.getPermissionType(), permission.getPermissionType()));
        return vo;
    }
}
