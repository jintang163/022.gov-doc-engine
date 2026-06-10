package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.SysPostQueryDTO;
import com.gov.doc.engine.dto.SysPostSaveDTO;
import com.gov.doc.engine.entity.SysPost;
import com.gov.doc.engine.vo.SysPostVO;

import java.util.List;

public interface SysPostService extends IService<SysPost> {

    PageResult<SysPostVO> pageList(Integer pageNum, Integer pageSize, SysPostQueryDTO queryDTO);

    SysPostVO getPostDetail(Long id);

    Long savePost(SysPostSaveDTO saveDTO);

    void updatePost(SysPostSaveDTO saveDTO);

    void deletePost(Long id);

    List<SysPostVO> listAll(Long unitId);
}
