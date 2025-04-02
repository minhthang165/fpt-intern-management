package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import com.fsoft.fintern.models.File;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CVInfoRepository extends JpaRepository<CVInfo, CVInfoId> {
    @Query("SELECT c FROM CVInfo c JOIN File f ON c.id.fileId = f.id " +
            "JOIN User u ON f.submitter = u " +
            "WHERE c.id.recruitmentId = :recruitmentId AND c.isActive = true AND u.role = com.fsoft.fintern.enums.Role.GUEST")
    List<CVInfo> findAllByRecruitmentIdAndUserRoleGuest(@Param("recruitmentId") Integer recruitmentId);
    @Transactional
    @Modifying
    @Query("DELETE FROM CVInfo c " +
            "WHERE c.id.recruitmentId = :recruitmentId " +
            "AND c.gpa <= (SELECT r.minGPA FROM Recruitment r WHERE r.id = :recruitmentId) " +
            "AND c.isActive = true " +
            "AND EXISTS (SELECT r FROM Recruitment r WHERE r.id = :recruitmentId " +
            "AND (r.endTime < :currentTime OR " +
            "(r.totalSlot - (SELECT COUNT(cv) FROM CVInfo cv WHERE cv.id.recruitmentId = r.id AND cv.isActive = true)) = 0))")
    int deleteCVInfosAboveMinGpaWhenTimeEndsOrSlotsFull(@Param("recruitmentId") Integer recruitmentId,
                                                        @Param("currentTime") Timestamp currentTime);

    @Query("SELECT COUNT(c) FROM CVInfo c WHERE c.id.recruitmentId = :recruitmentId AND c.isActive = true")
    int countByRecruitmentIdAndIsActiveTrue(@Param("recruitmentId") Integer recruitmentId);
    boolean existsByIdFileIdAndIdRecruitmentId(Integer fileId, Integer recruitmentId);

}
