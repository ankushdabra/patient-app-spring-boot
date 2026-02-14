package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDto {
    @JsonFormat(pattern = "hh:mm a")
    private String startTime;
    @JsonFormat(pattern = "hh:mm a")
    private String endTime;
}
