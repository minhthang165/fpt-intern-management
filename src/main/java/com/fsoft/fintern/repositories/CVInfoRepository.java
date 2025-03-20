package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVInfoRepository extends JpaRepository<CVInfo, CVInfoId> {
    List<CVInfo> findByIsActiveTrue();
    @Modifying
    @Query("DELETE FROM CVInfo cv WHERE cv.gpa < (SELECT r.minGPA FROM Recruitment r WHERE r.id = cv.id.recruitmentId)")
    void deleteCVsBelowMinGPA();
    @Query("SELECT cv FROM CVInfo cv WHERE cv.id.fileId = :fileId AND cv.id.recruitmentId = :recruitmentId")
    CVInfo findByFileIdAndRecruitmentId(Integer fileId, Integer recruitmentId);
}
