package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DeptRecommendDTO;
import com.gov.doc.engine.service.DeptRecommendService;
import com.gov.doc.engine.vo.DeptRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doc/recommend")
public class DeptRecommendController {

    @Autowired
    private DeptRecommendService deptRecommendService;

    @PostMapping("/dept")
    public Result<List<DeptRecommendVO>> recommendDepts(@RequestBody DeptRecommendDTO dto) {
        List<DeptRecommendVO> list = deptRecommendService.recommendDepts(dto);
        return Result.success(list);
    }

    @GetMapping("/dept")
    public Result<List<DeptRecommendVO>> recommendDeptsGet(
            @RequestParam(required = false) String docTitle,
            @RequestParam(required = false) String docType,
            @RequestParam(required = false) String keyword) {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle(docTitle);
        dto.setDocType(docType);
        dto.setKeyword(keyword);
        List<DeptRecommendVO> list = deptRecommendService.recommendDepts(dto);
        return Result.success(list);
    }
}
