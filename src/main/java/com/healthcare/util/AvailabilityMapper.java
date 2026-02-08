package com.healthcare.util;

import com.healthcare.dto.TimeSlotDto;
import com.healthcare.entity.DoctorAvailabilityEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AvailabilityMapper {

    public Map<String, List<TimeSlotDto>> map(List<DoctorAvailabilityEntity> availabilityEntities) {
        return availabilityEntities.stream()
                .sorted(Comparator.comparing(DoctorAvailabilityEntity::getDay))
                .collect(Collectors.groupingBy(
                        entity -> entity.getDay().name(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                entity -> TimeSlotDto.builder()
                                        .startTime(String.valueOf(entity.getStartTime()))
                                        .endTime(String.valueOf(entity.getEndTime()))
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }
}
