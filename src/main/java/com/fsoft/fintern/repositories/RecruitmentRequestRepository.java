package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.RecruitmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRequestRepository extends JpaRepository<RecruitmentRequest, Integer> {
    
    @Query("SELECT rr.classId FROM RecruitmentRequest rr WHERE rr.recruitmentId = :recruitmentId AND rr.isActive = true")
    Integer findClassIdByRecruitmentId(@Param("recruitmentId") Integer recruitmentId);
} 