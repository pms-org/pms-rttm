package com.pms.rttm.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pms.rttm.dto.PipelineStageMetrics;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.repository.RttmDlqEventRepository;
import com.pms.rttm.repository.RttmErrorEventRepository;
import com.pms.rttm.repository.RttmStageLatencyRepository;
import com.pms.rttm.repository.RttmTradeEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PipelineDepthService {

        // Last 24 hours window in seconds
        private static final Long WINDOW_24_HOURS = 86400L;

        private final RttmTradeEventRepository tradeRepo;
        private final RttmStageLatencyRepository latencyRepo;

        public PipelineStageMetrics stageMetrics(EventStage stage) {

                // Use last 24 hours for all metrics
                Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);

                long currentCount = tradeRepo.countByStageSince(stage, since);

                // Calculate success rate as: (current stage count / previous stage count) * 100
                double success;
                if (stage == EventStage.RECEIVED) {
                        // First stage has no previous stage, so success rate doesn't apply
                        success = 0.0;
                } else {
                        EventStage previousStage = getPreviousStage(stage);
                        long previousCount = tradeRepo.countByStageSince(previousStage, since);
                        double rawSuccess = previousCount == 0 ? 0.0 : ((double) currentCount / previousCount) * 100;
                        success = Math.round(rawSuccess * 100.0) / 100.0;
                }

                return new PipelineStageMetrics(
                                currentCount,
                                latencyRepo.avgLatency(stage, WINDOW_24_HOURS, since),
                                success);
        }

        private EventStage getPreviousStage(EventStage stage) {
                return switch (stage) {
                        case VALIDATED -> EventStage.RECEIVED;
                        case ENRICHED -> EventStage.VALIDATED;
                        case COMMITTED -> EventStage.ENRICHED;
                        default -> null;
                };
        }

        public Map<EventStage, PipelineStageMetrics> fullPipeline() {
                return Arrays.stream(EventStage.values())
                                .collect(Collectors.toMap(
                                                Function.identity(),
                                                this::stageMetrics));
        }
}
