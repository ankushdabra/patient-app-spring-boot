package com.healthcare.service;

import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.dto.TimeSlotDto;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.util.AvailabilityMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AvailabilityMapper availabilityMapper;

    public DoctorService(DoctorRepository doctorRepository, DoctorAvailabilityRepository availabilityRepository, AvailabilityMapper availabilityMapper) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
    }

    public List<DoctorResponseDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(doctor -> DoctorResponseDto.builder()
                        .id(doctor.getId())
                        .name(doctor.getUser().getName())
                        .specialization(doctor.getSpecialization())
                        .experience(doctor.getExperience())
                        .consultationFee(doctor.getConsultationFee())
                        .rating(doctor.getRating())
                        .profileImage(doctor.getProfileImage())
                        .build())
                .toList();
    }

    public DoctorDetailResponseDto getDoctorDetail(UUID doctorId) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<DoctorAvailabilityEntity> availabilityEntities = availabilityRepository.findByDoctorId(doctorId);

        Map<String, List<TimeSlotDto>> availabilityMap = availabilityMapper.map(availabilityEntities);

        return DoctorDetailResponseDto.builder()
                .id(doctor.getId())
                .name(doctor.getUser().getName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .rating(doctor.getRating())
                .consultationFee(doctor.getConsultationFee())
                .about(doctor.getAbout())
                .clinicAddress(doctor.getClinicAddress())
                .profileImage(doctor.getProfileImage())
                .availability(availabilityMap)
                .build();
    }
}
