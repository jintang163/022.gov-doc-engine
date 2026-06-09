package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocPermissionDTO;
import com.gov.doc.engine.entity.DocPermission;
import com.gov.doc.engine.vo.DocPermissionVO;

import java.util.List;

public interface DocPermissionService extends IService<DocPermission> {

    PageResult<DocPermissionVO> pageList(Integer pageNum, Integer pageSize, String permissionType, String permissionCode);

    List<DocPermissionVO> listAll();

    DocPermissionVO getByCode(String permissionCode);

    DocPermissionVO create(DocPermissionDTO dto);

    DocPermissionVO update(Long id, DocPermissionDTO dto);

    void delete(Long id);

    boolean checkPermission(String permissionCode);

    boolean checkSecurityLevelAccess(String userId, String securityLevel);
}
