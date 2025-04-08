package com.asyncflow.log.controller;

import com.asyncflow.log.model.entity.LogIndex;
import com.asyncflow.log.service.LogIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log-indexes")
public class LogIndexController {

    @Autowired
    private LogIndexService logIndexService;

    @PostMapping
    public ResponseEntity<LogIndex> createLogIndex(@RequestBody LogIndex logIndex) {
        LogIndex created = logIndexService.createLogIndex(logIndex);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogIndex> getLogIndexById(@PathVariable Long id) {
        LogIndex logIndex = logIndexService.getLogIndexById(id);
        return ResponseEntity.ok(logIndex);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<LogIndex>> getLogIndexesByFileId(@PathVariable Long fileId) {
        List<LogIndex> logIndexes = logIndexService.getLogIndexesByFileId(fileId);
        return ResponseEntity.ok(logIndexes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LogIndex>> searchLogIndexes(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String keyword) {
        
        if (level != null && startTime != null && endTime != null) {
            List<LogIndex> logIndexes = logIndexService.getLogIndexesByLevelAndTimeRange(level, startTime, endTime);
            return ResponseEntity.ok(logIndexes);
        }
        
        if (keyword != null) {
            List<LogIndex> logIndexes = logIndexService.getLogIndexesByKeyword(keyword);
            return ResponseEntity.ok(logIndexes);
        }
        
        return ResponseEntity.badRequest().build();
    }
} 