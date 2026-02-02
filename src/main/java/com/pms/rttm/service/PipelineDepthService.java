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
        private final RttmErrorEventRepository errorRepo;
        private final RttmDlqEventRepository dlqRepo;

        public PipelineStageMetrics stageMetrics(EventStage stage) {

                long total = tradeRepo.countByStage(stage);
                long errors = errorRepo.countByStage(stage);
                long dlq = dlqRepo.countGroupedByStage()
                                .getOrDefault(stage, 0L);

                double success = total == 0 ? 100.0 : ((double) (total - errors - dlq) / total) * 100;

                // Use last 24 hours latency data
                Instant since = Instant.now().minusSeconds(WINDOW_24_HOURS);
                return new PipelineStageMetrics(
                                total,
                                latencyRepo.avgLatency(stage, WINDOW_24_HOURS, since),
                                success);
        }

        public Map<EventStage, PipelineStageMetrics> fullPipeline() {
                return Arrays.stream(EventStage.values())
                                .collect(Collectors.toMap(
                                                Function.identity(),
                                                this::stageMetrics));
        }
}
