package com.gov.doc.engine.service;

import com.gov.doc.engine.dto.DeptRecommendDTO;
import com.gov.doc.engine.vo.DeptRecommendVO;

import java.util.List;

public interface DeptRecommendService {

    List<DeptRecommendVO> recommendDepts(DeptRecommendDTO dto);
}
