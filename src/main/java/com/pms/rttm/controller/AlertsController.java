package com.pms.rttm.controller;

import com.pms.rttm.dto.Alert;
import com.pms.rttm.service.AlertsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rttm")
@RequiredArgsConstructor
public class AlertsController {

    private final AlertsService alertsService;

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> alerts(@RequestParam(name = "status", defaultValue = "ACTIVE") String status,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<Alert> res = alertsService.latestByStatus(status, limit);
        return ResponseEntity.ok(res);
    }
}
