package com.asyncflow.log.controller;

import com.asyncflow.log.model.entity.LogArchive;
import com.asyncflow.log.service.LogArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log-archives")
public class LogArchiveController {

    @Autowired
    private LogArchiveService logArchiveService;

    /**
     * 创建日志归档记录
     */
    @PostMapping
    public ResponseEntity<LogArchive> createLogArchive(@RequestBody LogArchive logArchive) {
        LogArchive created = logArchiveService.createLogArchive(logArchive);
        return ResponseEntity.ok(created);
    }

    /**
     * 根据ID获取日志归档记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<LogArchive> getLogArchiveById(@PathVariable Long id) {
        LogArchive logArchive = logArchiveService.getLogArchiveById(id);
        if (logArchive == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logArchive);
    }

    /**
     * 根据文件ID获取日志归档记录
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<LogArchive> getLogArchiveByFileId(@PathVariable Long fileId) {
        LogArchive logArchive = logArchiveService.getLogArchiveByFileId(fileId);
        if (logArchive == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logArchive);
    }

    /**
     * 根据时间范围查询日志归档记录
     */
    @GetMapping("/search")
    public ResponseEntity<List<LogArchive>> searchLogArchives(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<LogArchive> logArchives = logArchiveService.getLogArchivesByTimeRange(startTime, endTime);
        return ResponseEntity.ok(logArchives);
    }

    /**
     * 归档日志文件
     */
    @PostMapping("/archive-file")
    public ResponseEntity<LogArchive> archiveLogFile(
            @RequestParam Long fileId,
            @RequestParam String archivePath,
            @RequestParam(required = false) String archiveReason) {
        LogArchive logArchive = logArchiveService.archiveLogFile(fileId, archivePath, archiveReason);
        return ResponseEntity.ok(logArchive);
    }
} 