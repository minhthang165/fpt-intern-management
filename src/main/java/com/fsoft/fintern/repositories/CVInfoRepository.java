package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CVInfoId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CVInfoRepository extends JpaRepository<CVInfo, CVInfoId> {
  
    @Query("SELECT c FROM CVInfo c JOIN File f ON c.id.fileId = f.id " +
            "JOIN User u ON f.submitter = u " +
            "WHERE c.id.recruitmentId = :recruitmentId AND c.isActive = true AND u.role = com.fsoft.fintern.enums.Role.GUEST")
    List<CVInfo> findAllByRecruitmentIdAndUserRoleGuest(@Param("recruitmentId") Integer recruitmentId);
    
}
