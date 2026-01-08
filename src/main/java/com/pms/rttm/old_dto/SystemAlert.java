package com.pms.rttm.old_dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemAlert {
    private String severity;
    private String timeAgo;
    private String message;
}