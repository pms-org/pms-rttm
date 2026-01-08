package com.pms.rttm.old_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class DlqStats {
    private Long totalCount;
    private String lastError;
    private Map<String, Long> errorsByStage;
}