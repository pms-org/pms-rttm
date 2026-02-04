package com.pms.rttm.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pms.rttm.dto.TradeTrackingDto;
import com.pms.rttm.service.TradeTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/rttm")
@RequiredArgsConstructor
public class TradeTrackingController {

    @Autowired
    TradeTrackingService tradeTrackingService;

    @GetMapping("/track-trade")
    public ResponseEntity<List<TradeTrackingDto>> getTrackTradeByTradeIde(
            @RequestParam(name = "tradeId") UUID tradeId) {
        return ResponseEntity.ok(tradeTrackingService.getTrackTradeByTradeId(tradeId));
    }

}
