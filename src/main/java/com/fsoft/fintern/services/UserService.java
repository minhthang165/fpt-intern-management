package com.fsoft.fintern.services;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.UpdateUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class    UserService {
    private final UserRepository userRepository;
    private final ClassroomRepository classRepository;

    public UserService(final UserRepository userRepository, final ClassroomRepository classRepository) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
    }

    public ResponseEntity<User> createIntern(CreateUserDTO createUserDTO) throws BadRequestException {
        if (!"INTERN".equalsIgnoreCase(createUserDTO.getRole().name())) {
            throw new BadRequestException(ErrorDictionaryConstraints.CREATED_FOR_INTERN_ONLY.getMessage());
        }

        Classroom existedClass = this.classRepository.findById(createUserDTO.getClassId()).orElse(null);
        if (existedClass == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.CLASS_NOT_EXISTS_ID.getMessage());
        }

        User existedUser = findUserByEmail(createUserDTO.getEmail());
        if (existedUser != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_ALREADY_EXISTS.getMessage());
        }

        User user = new User();
        user.setEmail(createUserDTO.getEmail());;
        user.setFirst_name(createUserDTO.getFirst_name());
        user.setLast_name(createUserDTO.getLast_name());
        user.setPhone_number(createUserDTO.getPhone_number());
        user.setGender(createUserDTO.getGender());
        user.setAvatar_path(createUserDTO.getPicture());

        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    public ResponseEntity<User> createEmployeeOrGuest(CreateUserDTO createUserDTO) throws BadRequestException {

        if (createUserDTO.getRole() == null) {
            createUserDTO.setRole(Role.GUEST);
        }

        if ("INTERN".equalsIgnoreCase(createUserDTO.getRole().name())) {
            throw new BadRequestException(ErrorDictionaryConstraints.CREATED_FOR_EMPLOYEE_OR_GUEST_ONLY.getMessage());
        }

        User existedUser = findUserByEmail(createUserDTO.getEmail());
        if (existedUser != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_ALREADY_EXISTS.getMessage());
        }

        User user = new User();
        user.setLast_name(createUserDTO.getLast_name());
        user.setFirst_name(createUserDTO.getFirst_name());
        user.setAvatar_path(createUserDTO.getPicture());
        user.setGender(createUserDTO.getGender());
        user.setPhone_number(createUserDTO.getPhone_number());
        user.setEmail(createUserDTO.getEmail());
        user.setRole(createUserDTO.getRole());

        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    public ResponseEntity<List<User>> findAll() throws BadRequestException {
        List<User> users = this.userRepository.findAll();
        if (users.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_IS_EMPTY.getMessage());
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    public ResponseEntity<User> getByEmail(String email) throws BadRequestException {
        User user = this.userRepository.findByEmail(email).orElse(null);
            return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<User> getById(int id) throws BadRequestException {
        User user =  this.userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    public ResponseEntity<User> update(int id, UpdateUserDTO updateUserDTO) throws BadRequestException {
        User user = this.userRepository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage()));

        BeanUtils.copyProperties(updateUserDTO, user, BeanUtilsHelper.getNullPropertyNames(updateUserDTO));
        this.userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<User> delete(int id) throws BadRequestException {
        User user = this.userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        user.setActive(false);
        user.setDeletedAt(Timestamp.from(Instant.now().plus(Duration.ofHours(7))));
        userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<User> setIsActiveTrue(int id) throws BadRequestException {
        User existedUser = this.userRepository.findById(id).orElse(null);
        if (existedUser == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        if (existedUser.isActive()) {
            throw new BadRequestException(ErrorDictionaryConstraints.IS_ACTIVE_TRUE.getMessage());
        }

        existedUser.setActive(true);
        userRepository.save(existedUser);
        return new ResponseEntity<>(existedUser, HttpStatus.OK);
    }

    private User findUserByEmail(String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }
}