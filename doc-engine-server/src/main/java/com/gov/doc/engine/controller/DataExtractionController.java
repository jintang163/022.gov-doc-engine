package com.gov.doc.engine.controller;

import com.gov.doc.engine.common.Result;
import com.gov.doc.engine.entity.DocAnalysis;
import com.gov.doc.engine.entity.RejectReason;
import com.gov.doc.engine.entity.ReminderLog;
import com.gov.doc.engine.service.DataExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data-extraction")
public class DataExtractionController {

    @Autowired
    private DataExtractionService dataExtractionService;

    @PostMapping("/extract/all")
    public Result<Map<String, Integer>> extractAll(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        if (since == null) {
            dataExtractionService.extractAll();
        } else {
            int p = dataExtractionService.extractProcessHistory(since);
            int u = dataExtractionService.extractUrgeLogs(since);
            int r = dataExtractionService.extractRejectReasons(since);
            Map<String, Integer> result = new HashMap<>();
            result.put("processHistory", p);
            result.put("urgeLogs", u);
            result.put("rejectReasons", r);
            return Result.success(result);
        }
        return Result.success(null);
    }

    @PostMapping("/extract/process-history")
    public Result<Integer> extractProcessHistory(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        int count = dataExtractionService.extractProcessHistory(since);
        return Result.success(count);
    }

    @PostMapping("/extract/urge-logs")
    public Result<Integer> extractUrgeLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        int count = dataExtractionService.extractUrgeLogs(since);
        return Result.success(count);
    }

    @PostMapping("/extract/reject-reasons")
    public Result<Integer> extractRejectReasons(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        int count = dataExtractionService.extractRejectReasons(since);
        return Result.success(count);
    }

    @GetMapping("/query/doc-analysis")
    public Result<List<DocAnalysis>> queryDocAnalysis(
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String sourceType,
            @RequestParam(defaultValue = "100") int limit) {
        List<DocAnalysis> list = dataExtractionService.queryDocAnalysis(businessKey, sourceType, limit);
        return Result.success(list);
    }

    @GetMapping("/query/reminder-log")
    public Result<List<ReminderLog>> queryReminderLog(
            @RequestParam(required = false) Long incomingId,
            @RequestParam(required = false) String urgeType,
            @RequestParam(defaultValue = "100") int limit) {
        List<ReminderLog> list = dataExtractionService.queryReminderLog(incomingId, urgeType, limit);
        return Result.success(list);
    }

    @GetMapping("/query/reject-reason")
    public Result<List<RejectReason>> queryRejectReason(
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String approvalType,
            @RequestParam(defaultValue = "100") int limit) {
        List<RejectReason> list = dataExtractionService.queryRejectReason(businessKey, approvalType, limit);
        return Result.success(list);
    }
}
