package com.pms.rttm.controller;

import com.pms.rttm.dto.StageStats;
import com.pms.rttm.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public List<StageStats> getStats() {
        return statsService.getStageStats();
    }
}