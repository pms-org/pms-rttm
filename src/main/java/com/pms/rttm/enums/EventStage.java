package com.pms.rttm.enums;

public enum EventStage {

    RECEIVED, // trade accepted by ingress / API
    VALIDATED, // schema + business validation passed
    ENRICHED, // reference data / pricing enrichment done
    COMMITTED, // persisted to core system / ledger
    ANALYZED // downstream analytics / risk / reporting
}
