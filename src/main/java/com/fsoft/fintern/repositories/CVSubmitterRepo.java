package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.CVSubmitter;

import com.fsoft.fintern.models.EmbedableID.CVSubmitterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CVSubmitterRepo extends JpaRepository<CVSubmitter, CVSubmitterId> {
}
