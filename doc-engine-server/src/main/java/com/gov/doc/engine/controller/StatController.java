package com.gov.doc.engine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.EfficiencyRankQueryDTO;
import com.gov.doc.engine.dto.StatQueryDTO;
import com.gov.doc.engine.service.StatService;
import com.gov.doc.engine.vo.StatCountersignCycleVO;
import com.gov.doc.engine.vo.StatDeptDraftVO;
import com.gov.doc.engine.vo.StatDocStatusVO;
import com.gov.doc.engine.vo.StatDocTypeVO;
import com.gov.doc.engine.vo.StatEfficiencyVO;
import com.gov.doc.engine.vo.StatNodeDwellVO;
import com.gov.doc.engine.vo.StatOverviewVO;
import com.gov.doc.engine.vo.StatProcessVO;
import com.gov.doc.engine.vo.StatRejectionOverviewVO;
import com.gov.doc.engine.vo.StatRejectionReasonVO;
import com.gov.doc.engine.vo.StatRejectionWordVO;
import com.gov.doc.engine.vo.StatTimelinessTrendVO;
import com.gov.doc.engine.vo.StatTrendVO;
import com.gov.doc.engine.vo.StatUnitVO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @GetMapping("/dept-draft")
    public Result<List<StatDeptDraftVO>> getDeptDraftStats(StatQueryDTO queryDTO) {
        List<StatDeptDraftVO> list = statService.getDeptDraftStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/node-dwell")
    public Result<List<StatNodeDwellVO>> getNodeDwellStats(StatQueryDTO queryDTO) {
        List<StatNodeDwellVO> list = statService.getNodeDwellStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/countersign-cycle")
    public Result<List<StatCountersignCycleVO>> getCountersignCycleStats(StatQueryDTO queryDTO) {
        List<StatCountersignCycleVO> list = statService.getCountersignCycleStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/timeliness-trend")
    public Result<List<StatTimelinessTrendVO>> getTimelinessTrend(StatQueryDTO queryDTO) {
        List<StatTimelinessTrendVO> list = statService.getTimelinessTrend(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/rejection-overview")
    public Result<StatRejectionOverviewVO> getRejectionOverview(StatQueryDTO queryDTO) {
        StatRejectionOverviewVO vo = statService.getRejectionOverview(queryDTO);
        return Result.success(vo);
    }

    @GetMapping("/rejection-words")
    public Result<List<StatRejectionWordVO>> getRejectionWordStats(StatQueryDTO queryDTO) {
        List<StatRejectionWordVO> list = statService.getRejectionWordStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/rejection-reasons")
    public Result<List<StatRejectionReasonVO>> getRejectionReasonStats(StatQueryDTO queryDTO) {
        List<StatRejectionReasonVO> list = statService.getRejectionReasonStats(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/efficiency/dept-rank")
    public Result<Page<StatEfficiencyVO>> getDeptEfficiencyRank(EfficiencyRankQueryDTO queryDTO) {
        Page<StatEfficiencyVO> page = statService.getDeptEfficiencyRank(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/efficiency/person-rank")
    public Result<Page<StatEfficiencyVO>> getPersonEfficiencyRank(EfficiencyRankQueryDTO queryDTO) {
        Page<StatEfficiencyVO> page = statService.getPersonEfficiencyRank(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/efficiency/export")
    public void exportEfficiencyRank(EfficiencyRankQueryDTO queryDTO,
                                     @RequestParam(defaultValue = "dept") String rankType,
                                     HttpServletResponse response) throws Exception {
        String fileName = (rankType.equals("person") ? "个人" : "部门") + "效能排行_"
                + (queryDTO.getStatMonth() == null ? "本月" : queryDTO.getStatMonth()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20"));
        try (OutputStream os = response.getOutputStream()) {
            statService.exportEfficiencyRank(queryDTO, rankType, os);
        }
    }

    @PostMapping("/efficiency/calculate")
    public Result<Boolean> calculateEfficiency(@RequestBody EfficiencyRankQueryDTO queryDTO) {
        boolean ok = statService.calculateEfficiencyForMonth(queryDTO.getStatMonth());
        return Result.success(ok);
    }
}
