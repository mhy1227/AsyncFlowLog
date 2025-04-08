package com.asyncflow.log.controller;

import com.asyncflow.log.model.entity.LogFile;
import com.asyncflow.log.service.LogFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log-files")
public class LogFileController {

    @Autowired
    private LogFileService logFileService;

    @PostMapping
    public ResponseEntity<LogFile> createLogFile(@RequestBody LogFile logFile) {
        LogFile created = logFileService.createLogFile(logFile);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogFile> getLogFileById(@PathVariable Long id) {
        LogFile logFile = logFileService.getLogFileById(id);
        return ResponseEntity.ok(logFile);
    }

    @GetMapping("/path/{filePath}")
    public ResponseEntity<LogFile> getLogFileByPath(@PathVariable String filePath) {
        LogFile logFile = logFileService.getLogFileByPath(filePath);
        return ResponseEntity.ok(logFile);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LogFile>> getLogFilesByStatus(@PathVariable String status) {
        List<LogFile> logFiles = logFileService.getLogFilesByStatus(status);
        return ResponseEntity.ok(logFiles);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateLogFileStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        boolean result = logFileService.updateLogFileStatus(id, status);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/info")
    public ResponseEntity<Boolean> updateLogFileInfo(
            @PathVariable Long id,
            @RequestParam Long fileSize,
            @RequestParam String endTime) {
        boolean result = logFileService.updateLogFileInfo(id, fileSize, endTime);
        return ResponseEntity.ok(result);
    }
} 