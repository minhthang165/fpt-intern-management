package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.repositories.CVInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVInfoService {
    @Autowired
    private final CVInfoRepository cvInfoRepository;

    public CVInfoService(CVInfoRepository cvInfoRepository) {
        this.cvInfoRepository = cvInfoRepository;
    }

    @Transactional
    public CVInfo create(CVInfoId id, Double gpa, String skill, String education) {
        if (cvInfoRepository.existsById(id)) {
            throw new RuntimeException("CV đã tồn tại!");
        }
        CVInfo cvInfo = new CVInfo();
        cvInfo.setId(id);
        cvInfo.setGpa(gpa);
        cvInfo.setSkill(skill);
        cvInfo.setEducation(education);
        return cvInfoRepository.save(cvInfo);
    }

    public List<CVInfo> getAllActiveCVs() {
        return cvInfoRepository.findByIsActiveTrue();
    }

    @Transactional
    public void filterCVsByMinGPA() {
        cvInfoRepository.deleteCVsBelowMinGPA();
    }

    public CVInfo getCVInfoByFileIdAndRecruitmentId(Integer fileId, Integer recruitmentId) {
        return cvInfoRepository.findByFileIdAndRecruitmentId(fileId, recruitmentId);
    }

}
