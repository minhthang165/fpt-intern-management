package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CV_InfoId;
import com.fsoft.fintern.repositories.CVInfoRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CVInfoService {
    private final CVInfoRepo cvInfoRepo;

    public CVInfoService(CVInfoRepo cvInfoRepo) {
        this.cvInfoRepo = cvInfoRepo;
    }

    @Transactional
    public CVInfo create(CV_InfoId id, Double gpa, String skill, String education) {
        if (cvInfoRepo.existsById(id)) {
            throw new RuntimeException("CV đã tồn tại!");
        }
        CVInfo cvInfo = new CVInfo();
        cvInfo.setId(id);
        cvInfo.setGpa(gpa);
        cvInfo.setSkill(skill);
        cvInfo.setEducation(education);
        return cvInfoRepo.save(cvInfo);
    }
}
