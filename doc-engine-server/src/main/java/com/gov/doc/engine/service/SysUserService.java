package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.SysUserQueryDTO;
import com.gov.doc.engine.dto.SysUserSaveDTO;
import com.gov.doc.engine.entity.SysUser;
import com.gov.doc.engine.vo.SysUserVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    PageResult<SysUserVO> pageList(Integer pageNum, Integer pageSize, SysUserQueryDTO queryDTO);

    SysUserVO getUserDetail(Long id);

    Long saveUser(SysUserSaveDTO saveDTO);

    void updateUser(SysUserSaveDTO saveDTO);

    void deleteUser(Long id);

    List<SysUserVO> listByDept(Long deptId);

    List<SysUserVO> listByPost(Long postId);

    List<SysUserVO> listByUnit(Long unitId);

    List<SysUserVO> searchUsers(String keyword);
}
