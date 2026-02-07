package com.healthcare.repository;

import com.healthcare.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime);
    List<AppointmentEntity> findByPatientId(UUID patientId);
}
