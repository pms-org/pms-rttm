package com.pms.rttm.service;

import com.pms.rttm.entity.RttmStageLatencyEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmStageLatencyRepository;
import com.pms.rttm.repository.RttmTradeEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StageLatencyComputationService {

    private final RttmTradeEventRepository tradeEventRepository;
    private final RttmStageLatencyRepository stageLatencyRepository;

    private static final List<EventStage> STAGE_ORDER = Arrays.asList(
            EventStage.RECEIVED,
            EventStage.VALIDATED,
            EventStage.ENRICHED,
            EventStage.COMMITTED,
            EventStage.ANALYZED);

    @Transactional
    public int computeAndSaveLatencies(UUID tradeId) {
        List<RttmTradeEventEntity> events = tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId);
        if (events.isEmpty()) {
            log.debug("No events found for trade {}", tradeId);
            return 0;
        }

        Collections.reverse(events);

        Map<EventStage, RttmTradeEventEntity> stageMap = new LinkedHashMap<>();
        for (RttmTradeEventEntity event : events) {
            stageMap.putIfAbsent(event.getEventStage(), event);
        }

        log.debug("Trade {} has stages: {}", tradeId, stageMap.keySet());

        List<RttmStageLatencyEntity> latencies = new ArrayList<>();

        for (int i = 0; i < STAGE_ORDER.size() - 1; i++) {
            EventStage currentStage = STAGE_ORDER.get(i);
            EventStage nextStage = STAGE_ORDER.get(i + 1);

            RttmTradeEventEntity currentEvent = stageMap.get(currentStage);
            RttmTradeEventEntity nextEvent = stageMap.get(nextStage);

            if (currentEvent != null && nextEvent != null) {
                long latencyMs = Duration.between(
                        currentEvent.getEventTime(),
                        nextEvent.getEventTime()).toMillis();

                latencies.add(RttmStageLatencyEntity.builder()
                        .tradeId(tradeId)
                        .serviceName(nextEvent.getServiceName())
                        .stageName(nextStage)
                        .latencyMs(Math.max(0, latencyMs))
                        .eventTime(nextEvent.getEventTime())
                        .build());
            }
        }

        if (!latencies.isEmpty()) {
            stageLatencyRepository.saveAll(latencies);
            log.debug("Computed {} latencies for trade {}", latencies.size(), tradeId);
        } else {
            log.warn("No latencies computed for trade {} - stages found: {} (need consecutive stages)",
                    tradeId, stageMap.keySet());
        }

        return latencies.size();
    }

    @Transactional
    public int computeAndSaveLatenciesBatch(Collection<UUID> tradeIds) {
        log.info("Computing latencies for batch of {} trades", tradeIds.size());
        int total = 0;
        for (UUID tradeId : tradeIds) {
            try {
                total += computeAndSaveLatencies(tradeId);
            } catch (Exception e) {
                log.error("Failed to compute latencies for trade {}", tradeId, e);
            }
        }
        if (total > 0) {
            log.info("Computed {} total latencies for {} trades", total, tradeIds.size());
        } else {
            log.warn("No latencies computed for {} trades - check if events have different stages", tradeIds.size());
        }
        return total;
    }
}
