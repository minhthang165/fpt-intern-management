package com.fsoft.fintern.repositories;

import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.models.Schedule;
import com.fsoft.fintern.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByClassFieldId(Integer classId);
    List<Schedule> findBySubjectId(Integer subjectId);
    List<Schedule> findByRoomId(Integer roomId);

} 