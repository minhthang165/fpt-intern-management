package com.fsoft.fintern.repositories;

import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    Optional<List<User>> findByEmailContainingIgnoreCase(String email);

    Optional<List<User>> findUserByClassroom_Id(Integer classId);
}
