package com.pms.rttm.service;

import com.pms.rttm.entity.RttmTradeEventEntity;
import com.pms.rttm.proto.RttmTradeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { BatchQueueService.class })
@TestPropertySource(properties = { "rttm.batch.size=10", "rttm.batch.poll-ms=10000" })
public class BatchQueueServiceTest {

    @Autowired
    private BatchQueueService batchQueueService;

    @MockBean
    private RttmIngestService ingestService;

    @Test
    public void testTradeBatchSaved() throws Exception {
        // enqueue 3 trade proto messages
        long now = System.currentTimeMillis();
        String uuid1 = java.util.UUID.randomUUID().toString();
        String uuid2 = java.util.UUID.randomUUID().toString();
        String uuid3 = java.util.UUID.randomUUID().toString();

        RttmTradeEvent t1 = RttmTradeEvent.newBuilder()
                .setTradeId(uuid1)
                .setServiceName("svc")
                .setEventType("TRADE_RECEIVED")
                .setEventStage("RECEIVED")
                .setEventStatus("OK")
                .setEventTime(now)
                .build();

        RttmTradeEvent t2 = RttmTradeEvent.newBuilder()
                .setTradeId(uuid2)
                .setServiceName("svc")
                .setEventType("TRADE_RECEIVED")
                .setEventStage("RECEIVED")
                .setEventStatus("OK")
                .setEventTime(now)
                .build();

        RttmTradeEvent t3 = RttmTradeEvent.newBuilder()
                .setTradeId(uuid3)
                .setServiceName("svc")
                .setEventType("TRADE_RECEIVED")
                .setEventStage("RECEIVED")
                .setEventStatus("OK")
                .setEventTime(now)
                .build();

        boolean o1 = batchQueueService.enqueueTrade(t1);
        boolean o2 = batchQueueService.enqueueTrade(t2);
        boolean o3 = batchQueueService.enqueueTrade(t3);

        assertThat(o1).isTrue();
        assertThat(o2).isTrue();
        assertThat(o3).isTrue();

        // call the batch processor directly
        batchQueueService.processTradeBatch();

        // capture and assert ingestService.ingestBatch was called with 3 entities
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<RttmTradeEventEntity>> captor = ArgumentCaptor.forClass((Class) Iterable.class);

        verify(ingestService, times(1)).ingestBatch(captor.capture());

        Iterable<RttmTradeEventEntity> captured = captor.getValue();
        long count = StreamSupport.stream(captured.spliterator(), false).count();
        assertThat(count).isEqualTo(3);
    }
}
