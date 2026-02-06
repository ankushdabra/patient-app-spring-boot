package com.healthcare.repository;

import com.healthcare.entity.DoctorAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilityEntity, UUID> {
    List<DoctorAvailabilityEntity> findByDoctorId(UUID doctorId);
}
