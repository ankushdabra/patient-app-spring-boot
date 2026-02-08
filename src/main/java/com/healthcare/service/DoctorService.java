package com.healthcare.service;

import com.healthcare.dto.DoctorAvailabilityDto;
import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorService(DoctorRepository doctorRepository, DoctorAvailabilityRepository availabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
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
                        .build())
                .toList();
    }

    public DoctorDetailResponseDto getDoctorDetail(UUID doctorId) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<DoctorAvailabilityEntity> availabilityEntities = availabilityRepository.findByDoctorId(doctorId);
        List<DoctorAvailabilityDto> availabilityDtos = availabilityEntities.stream()
                .map(this::mapAvailability)
                .toList();
        return getDoctorDetailResponseDto(doctor, availabilityDtos);
    }

    private DoctorDetailResponseDto getDoctorDetailResponseDto(DoctorEntity doctor, List<DoctorAvailabilityDto> availabilityDtos) {
        return DoctorDetailResponseDto.builder().id(doctor.getId())
                .name(doctor.getUser().getName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .rating(doctor.getRating())
                .consultationFee(doctor.getConsultationFee())
                .about(doctor.getAbout())
                .clinicAddress(doctor.getClinicAddress())
                .profileImage(doctor.getProfileImage())
                .availability(availabilityDtos)
                .build();
    }

    private DoctorAvailabilityDto mapAvailability(DoctorAvailabilityEntity entity) {
        return DoctorAvailabilityDto.builder().day(String.valueOf(entity.getDay()))
                .startTime(String.valueOf(entity.getStartTime()))
                .endTime(String.valueOf(entity.getEndTime()))
                .build();
    }
}
