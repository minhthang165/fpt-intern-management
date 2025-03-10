package com.fsoft.fintern.services;

import com.fsoft.fintern.models.CVSubmitter;

import com.fsoft.fintern.models.EmbedableID.CVSubmitterId;
import com.fsoft.fintern.repositories.CVSubmitterRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CVSubmitterService {
    private final CVSubmitterRepo cvSubmitterRepo;

    public CVSubmitterService(CVSubmitterRepo cvSubmitterRepo) {
        this.cvSubmitterRepo = cvSubmitterRepo;
    }

    @Transactional
    public CVSubmitter create(Integer recruitmentId, Integer fileId) {
        CVSubmitterId id = new CVSubmitterId(recruitmentId, fileId);
        if (cvSubmitterRepo.existsById(id)) {
            throw new RuntimeException("CV đã tồn tại!");
        }
        CVSubmitter cvSubmitter = new CVSubmitter(id);
        return cvSubmitterRepo.save(cvSubmitter);
    }
}
