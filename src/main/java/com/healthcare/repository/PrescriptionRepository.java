package com.healthcare.repository;

import com.healthcare.entity.PrescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, UUID> {
    @Query("SELECT p FROM PrescriptionEntity p JOIN FETCH p.appointment a JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH a.patient pt JOIN FETCH pt.user WHERE p.id = :prescriptionId")
    Optional<PrescriptionEntity> findByIdWithDetails(@Param("prescriptionId") UUID prescriptionId);

    @Query("SELECT p FROM PrescriptionEntity p JOIN FETCH p.appointment a JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH a.patient pt JOIN FETCH pt.user WHERE p.patient = :patientId")
    List<PrescriptionEntity> findByPatientWithDetails(@Param("patientId") UUID patientId);

    @Query("SELECT p FROM PrescriptionEntity p JOIN FETCH p.appointment a JOIN FETCH a.doctor d JOIN FETCH d.user JOIN FETCH a.patient pt JOIN FETCH pt.user WHERE p.doctor = :doctorId")
    List<PrescriptionEntity> findByDoctorWithDetails(@Param("doctorId") UUID doctorId);
}
