package com.pms.rttm.enums;

public enum StageName {
    RECEIVED, // Trade/message received by service
    VALIDATED, // Validation completed
    ENRICHED, // Reference / enrichment completed
    COMMITTED, // Persisted / state committed
    ANALYZED // Final processing / downstream analysis
}
