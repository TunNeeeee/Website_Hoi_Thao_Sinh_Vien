package com.hutech.hoithao.service;

import com.hutech.hoithao.models.AcademicYear;
import com.hutech.hoithao.repository.AcademicYearRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    public AcademicYear findById(Integer id) {
        return academicYearRepository.findById(id).orElse(null);
    }
    public List<AcademicYear> findAll() {
        return academicYearRepository.findAll();
    }
    public AcademicYear createAcademicYear(AcademicYear academicYear) {
        return academicYearRepository.save(academicYear);
    }

    public AcademicYear updateAcademicYear(Integer id, AcademicYear academicYear) {
        AcademicYear existingYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));
        existingYear.setNameAcademicYear(academicYear.getNameAcademicYear());
        existingYear.setEvent(academicYear.getEvent());
        return academicYearRepository.save(existingYear);
    }


    public void deleteAcademicYear(Integer id) {
        academicYearRepository.deleteById(id);
    }


    public AcademicYear getAcademicYearById(Integer id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic Year not found"));
    }


    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAll();
    }
}