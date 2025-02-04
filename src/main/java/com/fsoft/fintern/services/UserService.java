package com.fsoft.fintern.services;

import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.UserDTO;
import com.fsoft.fintern.models.User.User;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService {
    private final UserRepository user_repository;

    public UserService(final UserRepository user_repository) {
        this.user_repository = user_repository;
    }

    public ResponseEntity<User> createIntern(UserDTO userDTO) throws BadRequestException {
        if (!"INTERN".equalsIgnoreCase(userDTO.getRole().name())) {
            throw new BadRequestException(ErrorDictionaryConstraints.CREATED_FOR_INTERN_ONLY.getMessage());
        }
        User user = new User();
        user.setFirst_name(userDTO.getFirst_name());
        user.setLast_name(userDTO.getLast_name());
        user.setEmail(userDTO.getEmail());
        user.setPhone_number(userDTO.getPhone_number());
        user.setClass_id(userDTO.getClass_id());
        user.setGender(userDTO.getGender());
        user.setRole(userDTO.getRole());

        User savedUser = user_repository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    public ResponseEntity<User> createEmployeeOrGuest(UserDTO userDTO) throws BadRequestException {
        if ("INTERN".equalsIgnoreCase(userDTO.getRole().name())) {
            throw new BadRequestException(ErrorDictionaryConstraints.CREATED_FOR_EMPLOYEE_OR_GUEST_ONLY.getMessage());
        }

        User user = new User();
        user.setFirst_name(userDTO.getFirst_name());
        user.setLast_name(userDTO.getLast_name());
        user.setEmail(userDTO.getEmail());
        user.setPhone_number(userDTO.getPhone_number());
        user.setGender(userDTO.getGender());
        user.setRole(userDTO.getRole());

        User savedUser = user_repository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    public ResponseEntity<List<User>> findAll() throws BadRequestException {
        List<User> users = this.user_repository.findAll();
        if (users.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_IS_EMPTY.getMessage());
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    public ResponseEntity<User> getById(int id) throws BadRequestException {
        User user =  this.user_repository.findById(id).orElse(null);
        if (user == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    public ResponseEntity<User> update(int id, UserDTO userDTO) throws BadRequestException {
        User user = this.user_repository.findById(id).orElseThrow(()
                -> new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage()));

        BeanUtils.copyProperties(userDTO, user, BeanUtilsHelper.getNullPropertyNames(userDTO));
        this.user_repository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<User> delete(int id) throws BadRequestException {
        User user = this.user_repository.findById(id).orElse(null);
        if (user == null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USER_NOT_FOUND.getMessage());
        }
        user.setActive(false);
        user.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        user_repository.save(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
