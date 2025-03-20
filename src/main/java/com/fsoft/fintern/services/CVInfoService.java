package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CV_InfoId;
import com.fsoft.fintern.repositories.CVInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CVInfoService {
    private final CVInfoRepository cvInfoRepository;

    public CVInfoService(CVInfoRepository cvInfoRepository) {
        this.cvInfoRepository = cvInfoRepository;
    }

    @Transactional
    public CVInfo create(CV_InfoId id, Double gpa, String skill, String education) {
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
}
