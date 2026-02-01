package com.pms.rttm.service;

import com.pms.rttm.entity.RttmStageLatencyEntity;
import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.enums.EventStage;
import com.pms.rttm.enums.EventType;
import com.pms.rttm.repository.RttmStageLatencyRepository;
import com.pms.rttm.repository.RttmTradeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StageLatencyComputationServiceTest {

    @Mock
    private RttmTradeEventRepository tradeEventRepository;

    @Mock
    private RttmStageLatencyRepository stageLatencyRepository;

    @InjectMocks
    private StageLatencyComputationService service;

    private UUID tradeId;
    private List<RttmTradeEventEntity> sampleEvents;

    @BeforeEach
    void setUp() {
        tradeId = UUID.randomUUID();
        sampleEvents = createSampleEvents();
    }

    @Test
    void computeAndSaveLatencies_withCompleteStages_calculatesCorrectly() {
        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId))
                .thenReturn(new ArrayList<>(sampleEvents));

        int result = service.computeAndSaveLatencies(tradeId);

        assertThat(result).isEqualTo(3);

        ArgumentCaptor<List<RttmStageLatencyEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(stageLatencyRepository).saveAll(captor.capture());

        List<RttmStageLatencyEntity> saved = captor.getValue();
        assertThat(saved).hasSize(3);

        assertThat(saved.get(0).getStageName()).isEqualTo(EventStage.VALIDATED);
        assertThat(saved.get(0).getLatencyMs()).isEqualTo(120000L);

        assertThat(saved.get(1).getStageName()).isEqualTo(EventStage.ENRICHED);
        assertThat(saved.get(1).getLatencyMs()).isEqualTo(240000L);

        assertThat(saved.get(2).getStageName()).isEqualTo(EventStage.COMMITTED);
        assertThat(saved.get(2).getLatencyMs()).isEqualTo(180000L);
    }

    @Test
    void computeAndSaveLatencies_withNoEvents_returnsZero() {
        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId))
                .thenReturn(Collections.emptyList());

        int result = service.computeAndSaveLatencies(tradeId);

        assertThat(result).isZero();
        verify(stageLatencyRepository, never()).saveAll(any());
    }

    @Test
    void computeAndSaveLatencies_withPartialStages_calculatesAvailable() {
        List<RttmTradeEventEntity> partialEvents = Arrays.asList(
                createEvent(EventStage.RECEIVED, Instant.parse("2026-01-15T10:00:00Z")),
                createEvent(EventStage.VALIDATED, Instant.parse("2026-01-15T10:02:00Z")));

        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId))
                .thenReturn(new ArrayList<>(partialEvents));

        int result = service.computeAndSaveLatencies(tradeId);

        assertThat(result).isEqualTo(1);

        ArgumentCaptor<List<RttmStageLatencyEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(stageLatencyRepository).saveAll(captor.capture());

        List<RttmStageLatencyEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStageName()).isEqualTo(EventStage.VALIDATED);
        assertThat(saved.get(0).getLatencyMs()).isEqualTo(120000L);
    }

    @Test
    void computeAndSaveLatenciesBatch_processesMultipleTrades() {
        UUID tradeId1 = UUID.randomUUID();
        UUID tradeId2 = UUID.randomUUID();

        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId1))
                .thenReturn(new ArrayList<>(createSampleEvents()));
        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId2))
                .thenReturn(new ArrayList<>(createSampleEvents()));

        int result = service.computeAndSaveLatenciesBatch(Arrays.asList(tradeId1, tradeId2));

        assertThat(result).isEqualTo(6);
        verify(stageLatencyRepository, times(2)).saveAll(any());
    }

    @Test
    void computeAndSaveLatenciesBatch_handlesExceptions() {
        UUID tradeId1 = UUID.randomUUID();
        UUID tradeId2 = UUID.randomUUID();

        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId1))
                .thenThrow(new RuntimeException("DB error"));
        when(tradeEventRepository.findByTradeIdOrderByEventTimeDesc(tradeId2))
                .thenReturn(new ArrayList<>(createSampleEvents()));

        int result = service.computeAndSaveLatenciesBatch(Arrays.asList(tradeId1, tradeId2));

        assertThat(result).isEqualTo(3);
        verify(stageLatencyRepository, times(1)).saveAll(any());
    }

    private List<RttmTradeEventEntity> createSampleEvents() {
        Instant baseTime = Instant.parse("2026-01-15T10:00:00Z");

        return Arrays.asList(
                createEvent(EventStage.COMMITTED, baseTime.plusSeconds(540)),
                createEvent(EventStage.ENRICHED, baseTime.plusSeconds(360)),
                createEvent(EventStage.VALIDATED, baseTime.plusSeconds(120)),
                createEvent(EventStage.RECEIVED, baseTime));
    }

    private RttmTradeEventEntity createEvent(EventStage stage, Instant time) {
        return RttmTradeEventEntity.builder()
                .id(1L)
                .tradeId(tradeId)
                .serviceName("test-service")
                .eventType(EventType.TRADE_RECEIVED)
                .eventStage(stage)
                .eventStatus("OK")
                .eventTime(time)
                .build();
    }
}
