package com.fsoft.fintern.services;
import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.RecruitmentDTO;
import com.fsoft.fintern.models.Recruitment;
import com.fsoft.fintern.repositories.RecruitmentRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;

    public RecruitmentService(RecruitmentRepository recruitRepository) {
        this.recruitmentRepository = recruitRepository;
    }
    public ResponseEntity<Recruitment> findById(int id) throws BadRequestException {
        Optional<Recruitment> recruitment = this.recruitmentRepository.findById(id);
        if (recruitment.isPresent()) {
            return new ResponseEntity<>(recruitment.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException();
        }
    }

    public ResponseEntity<List<Recruitment>> findAll() {
        List<Recruitment> recruitments = this.recruitmentRepository.findAll();
        if (recruitments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(recruitments, HttpStatus.OK);
        }
    }

    public ResponseEntity<Recruitment> update(int id, RecruitmentDTO recruitmentDTO) throws BadRequestException {
        Recruitment recruitment = this.recruitmentRepository.findById(id).orElseThrow(()
                -> new BadRequestException());

        BeanUtils.copyProperties(recruitmentDTO, recruitment, BeanUtilsHelper.getNullPropertyNames(recruitmentDTO));

        this.recruitmentRepository.save(recruitment);
        return new ResponseEntity<>(recruitment, HttpStatus.OK);
    }

    public ResponseEntity<Recruitment> delete(int id) throws BadRequestException {
        Recruitment recruitment = this.recruitmentRepository.findById(id).orElse(null);
        if (recruitment == null) {
            throw new BadRequestException();
        }
        recruitment.setActive(false);
        recruitment.setDeletedAt(Timestamp.from(Instant.now().plus((Duration.ofHours(6)))));
        this.recruitmentRepository.save(recruitment);
        return new ResponseEntity<>(recruitment, HttpStatus.OK);
    }

    public ResponseEntity<Recruitment> create(RecruitmentDTO recruitmentDTO) throws BadRequestException {
        Recruitment newRecruitment = new Recruitment();

        newRecruitment.setName(recruitmentDTO.getName());
        newRecruitment.setPosition(recruitmentDTO.getPosition());
        newRecruitment.setExperienceRequirement(recruitmentDTO.getExperience());
        newRecruitment.setLanguage(recruitmentDTO.getLanguage());
        newRecruitment.setTotalSlot(recruitmentDTO.getTotalSlot());
        newRecruitment.setDescription(recruitmentDTO.getDescription());
        newRecruitment.setEndTime(recruitmentDTO.getEndTime());


        if (recruitmentDTO.getMinGPA() == null || recruitmentDTO.getMinGPA() < 0.0) {
            newRecruitment.setMinGPA(0.0f);
        } else {
            newRecruitment.setMinGPA(recruitmentDTO.getMinGPA());
        }


        Recruitment savedRecruitment = this.recruitmentRepository.save(newRecruitment);


        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecruitment);
    }



}
