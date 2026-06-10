package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.StatQueryDTO;
import com.gov.doc.engine.service.StatService;
import com.gov.doc.engine.vo.StatDocStatusVO;
import com.gov.doc.engine.vo.StatDocTypeVO;
import com.gov.doc.engine.vo.StatOverviewVO;
import com.gov.doc.engine.vo.StatProcessVO;
import com.gov.doc.engine.vo.StatTrendVO;
import com.gov.doc.engine.vo.StatUnitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/overview")
    public Result<StatOverviewVO> getOverview(StatQueryDTO queryDTO) {
        StatOverviewVO vo = statService.getOverview(queryDTO);
        return Result.success(vo);
    }

    @GetMapping("/doc-type")
    public Result<List<StatDocTypeVO>> getDocTypeStats(StatQueryDTO queryDTO) {
        List<StatDocTypeVO> list = statService.getDocTypeStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/doc-status")
    public Result<List<StatDocStatusVO>> getDocStatusStats(StatQueryDTO queryDTO) {
        List<StatDocStatusVO> list = statService.getDocStatusStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/process")
    public Result<List<StatProcessVO>> getProcessStats(StatQueryDTO queryDTO) {
        List<StatProcessVO> list = statService.getProcessStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/trend")
    public Result<List<StatTrendVO>> getTrendStats(StatQueryDTO queryDTO) {
        List<StatTrendVO> list = statService.getTrendStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/unit")
    public Result<List<StatUnitVO>> getUnitStats(StatQueryDTO queryDTO) {
        List<StatUnitVO> list = statService.getUnitStats(queryDTO);
        return Result.success(list);
    }
}
