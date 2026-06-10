package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.StatQueryDTO;
import com.gov.doc.engine.entity.DocDocument;
import com.gov.doc.engine.vo.StatDocStatusVO;
import com.gov.doc.engine.vo.StatDocTypeVO;
import com.gov.doc.engine.vo.StatOverviewVO;
import com.gov.doc.engine.vo.StatProcessVO;
import com.gov.doc.engine.vo.StatTrendVO;
import com.gov.doc.engine.vo.StatUnitVO;

import java.util.List;

public interface StatService extends IService<DocDocument> {

    StatOverviewVO getOverview(StatQueryDTO queryDTO);

    List<StatDocTypeVO> getDocTypeStats(StatQueryDTO queryDTO);

    List<StatDocStatusVO> getDocStatusStats(StatQueryDTO queryDTO);

    List<StatProcessVO> getProcessStats(StatQueryDTO queryDTO);

    List<StatTrendVO> getTrendStats(StatQueryDTO queryDTO);

    List<StatUnitVO> getUnitStats(StatQueryDTO queryDTO);
}
