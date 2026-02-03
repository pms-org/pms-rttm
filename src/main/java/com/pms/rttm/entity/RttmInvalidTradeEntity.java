package com.pms.rttm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rttm_invalid_trades", indexes = {
        @Index(name = "idx_invalid_time", columnList = "event_time"),
        @Index(name = "idx_invalid_trade_id", columnList = "trade_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RttmInvalidTradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "symbol", nullable = false, length = 16)
    private String symbol;

    @Column(name = "side", nullable = false, length = 8)
    private String side;

    @Column(name = "price_per_stock")
    private BigDecimal pricePerStock;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "trade_timestamp", nullable = false)
    private Instant tradeTimestamp;

    @Column(name = "validation_errors", nullable = false, columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
