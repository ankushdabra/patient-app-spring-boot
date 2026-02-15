package com.healthcare.repository;

import com.healthcare.entity.AppointmentEntity;
import com.healthcare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.patient.id = :patientId AND (a.appointmentDate > :currentDate OR (a.appointmentDate = :currentDate AND a.appointmentTime >= :currentTime)) AND a.status = :status ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<AppointmentEntity> findUpcomingAppointmentsForPatient(@Param("patientId") UUID patientId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.doctor.id = :doctorId AND (a.appointmentDate > :currentDate OR (a.appointmentDate = :currentDate AND a.appointmentTime >= :currentTime)) AND a.status = :status ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<AppointmentEntity> findUpcomingAppointmentsForDoctor(@Param("doctorId") UUID doctorId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :currentDate AND a.appointmentTime >= :currentTime AND a.status = :status ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<AppointmentEntity> findTodayAppointmentsForDoctor(@Param("doctorId") UUID doctorId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime, @Param("status") AppointmentStatus status);

    @Query("SELECT SUM(a.doctor.consultationFee) FROM AppointmentEntity a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status = :status")
    BigDecimal calculateTotalEarningsByDoctorAndDateAndStatus(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);
}
