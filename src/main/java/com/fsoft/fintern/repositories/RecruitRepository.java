package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruitRepository extends JpaRepository<Recruitment, Integer>  {
}
