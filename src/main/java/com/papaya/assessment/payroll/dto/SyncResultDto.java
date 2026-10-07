package com.papaya.assessment.payroll.dto;

import java.util.List;

public record SyncResultDto(
        int fetched,
        int persisted,
        int skippedDuplicate,
        int skippedInvalid,
        int failed,
        List<String> invalidRecordSummaries
) {
}
