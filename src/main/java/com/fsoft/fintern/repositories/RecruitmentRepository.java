package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Integer>  {
    @Query("SELECT r FROM Recruitment r WHERE r.id = :id AND r.isActive = true")
    Recruitment findByIdAndIsActiveTrue(@Param("id") int id);

    @Query(value = "SELECT COUNT(c.file_id) FROM CV_Info c WHERE c.recruitment_id = :recruitmentId AND c.is_active = 1", nativeQuery = true)
    Integer countByRecruitmentIdAndIsActiveTrue(@Param("recruitmentId") Integer recruitmentId);

    Recruitment findByClassId(Integer classId);
}
