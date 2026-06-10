package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.SysDeptSaveDTO;
import com.gov.doc.engine.entity.SysDept;
import com.gov.doc.engine.vo.SysDeptVO;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<SysDeptVO> getDeptTree(Long unitId);

    SysDeptVO getDeptDetail(Long id);

    Long saveDept(SysDeptSaveDTO saveDTO);

    void updateDept(SysDeptSaveDTO saveDTO);

    void deleteDept(Long id);

    List<SysDeptVO> getChildren(Long unitId, Long parentId);
}
