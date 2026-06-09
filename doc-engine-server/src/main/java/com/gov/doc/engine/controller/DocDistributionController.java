package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.dto.DocDistributionCreateDTO;
import com.gov.doc.engine.entity.DocDistribution;
import com.gov.doc.engine.service.DocDistributionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "公文分发管理")
@RestController
@RequestMapping("/api/doc/distribution")
public class DocDistributionController {

    @Autowired
    private DocDistributionService docDistributionService;

    @ApiOperation("分发列表")
    @GetMapping("/page")
    public Result<PageResult<DocDistribution>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long docId,
            @RequestParam(required = false) String status) {
        return Result.success(docDistributionService.pageList(pageNum, pageSize, docId, status));
    }

    @ApiOperation("分发详情")
    @GetMapping("/{id}")
    public Result<DocDistribution> detail(@PathVariable Long id) {
        return Result.success(docDistributionService.getDetail(id));
    }

    @ApiOperation("根据公文ID查询分发记录")
    @GetMapping("/doc/{docId}")
    public Result<List<DocDistribution>> getByDocId(@PathVariable Long docId) {
        return Result.success(docDistributionService.getByDocId(docId));
    }

    @ApiOperation("分发公文")
    @PostMapping("/distribute")
    public Result<List<DocDistribution>> distribute(@RequestBody DocDistributionCreateDTO dto) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        return Result.success(docDistributionService.distribute(dto, operatorId, operatorName));
    }

    @ApiOperation("确认接收")
    @PostMapping("/confirm-receive/{id}")
    public Result<Void> confirmReceive(@PathVariable Long id,
                                       @RequestParam(required = false) String remark,
                                       HttpServletRequest request) {
        String receiverId = "current_user";
        String receiverName = "当前用户";
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        docDistributionService.confirmReceive(id, receiverId, receiverName, remark, ip, ua);
        return Result.success();
    }

    @ApiOperation("标记已打印")
    @PostMapping("/mark-printed/{id}")
    public Result<Void> markPrinted(@PathVariable Long id) {
        String operatorId = "current_user";
        String operatorName = "当前用户";
        docDistributionService.markPrinted(id, operatorId, operatorName);
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
