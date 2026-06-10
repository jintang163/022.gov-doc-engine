package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.SysUnitSaveDTO;
import com.gov.doc.engine.entity.SysUnit;
import com.gov.doc.engine.vo.SysUnitVO;

import java.util.List;

public interface SysUnitService extends IService<SysUnit> {

    List<SysUnitVO> getUnitTree();

    SysUnitVO getUnitDetail(Long id);

    Long saveUnit(SysUnitSaveDTO saveDTO);

    void updateUnit(SysUnitSaveDTO saveDTO);

    void deleteUnit(Long id);

    List<SysUnitVO> getChildren(Long parentId);
}
