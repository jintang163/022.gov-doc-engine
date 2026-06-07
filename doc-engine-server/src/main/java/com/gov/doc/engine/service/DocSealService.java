package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.dto.DocSealCreateDTO;
import com.gov.doc.engine.dto.DocSealGrantDTO;
import com.gov.doc.engine.dto.DocSealQueryDTO;
import com.gov.doc.engine.dto.DocSealUpdateDTO;
import com.gov.doc.engine.entity.DocSeal;
import com.gov.doc.engine.vo.DocSealGrantVO;
import com.gov.doc.engine.vo.DocSealVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocSealService extends IService<DocSeal> {

    PageResult<DocSealVO> pageList(DocSealQueryDTO queryDTO);

    DocSealVO getDetail(Long id);

    Long createSeal(DocSealCreateDTO createDTO);

    void updateSeal(DocSealUpdateDTO updateDTO);

    void deleteSeal(Long id);

    String uploadSealImage(Long sealId, MultipartFile file);

    byte[] getSealImage(Long sealId);

    void grantSeal(DocSealGrantDTO grantDTO);

    void revokeGrant(Long grantId);

    List<DocSealGrantVO> getSealGrants(Long sealId);

    List<DocSealVO> listAvailableSeals();
}
