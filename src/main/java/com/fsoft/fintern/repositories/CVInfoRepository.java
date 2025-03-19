package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CVInfo;
import com.fsoft.fintern.models.EmbedableID.CV_InfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CVInfoRepository extends JpaRepository<CVInfo, CV_InfoId> {
}