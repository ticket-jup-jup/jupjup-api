package org.example.jubjubapi.performancewatch.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;


//userId는 요청으로 받지 않고 인증된 사용자의 ID 사용
@Getter
@NoArgsConstructor
public class PerformanceWatchCreateRequest {
    @NotNull(message = "회차 ID는 필수입니다.")
    @Positive(message = "회차 ID는 1 이상이어야 합니다.")
    private Long performanceId;

    public PerformanceWatchCreateRequest(Long performanceId) {
        this.performanceId = performanceId;
    }
}
