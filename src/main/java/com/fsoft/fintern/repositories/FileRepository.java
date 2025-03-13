package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.File;
import com.fsoft.fintern.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository  extends JpaRepository<File, Integer> {

    Optional<File> findBySubmitterIdAndFileType(Integer userId, String cv);
}
