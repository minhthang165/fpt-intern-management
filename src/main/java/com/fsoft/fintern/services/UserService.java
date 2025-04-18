package com.fsoft.fintern.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoft.fintern.constraints.ErrorDictionaryConstraints;
import com.fsoft.fintern.dtos.CreateUserDTO;
import com.fsoft.fintern.dtos.UpdateUserDTO;
import com.fsoft.fintern.enums.Role;
import com.fsoft.fintern.models.Classroom;
import com.fsoft.fintern.models.Task;
import com.fsoft.fintern.models.User;
import com.fsoft.fintern.repositories.ClassroomRepository;
import com.fsoft.fintern.repositories.UserRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserRepository userRepository;
    private final ClassroomRepository classRepository;
    private final String BAN_PREFIX = "banned_user:";

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

        User existedUser = getByEmail(createUserDTO.getEmail()).getBody();
        if (existedUser != null) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_ALREADY_EXISTS.getMessage());
        }

        User user = new User();
        user.setEmail(createUserDTO.getEmail());
        user.setClassroom(existedClass);
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
            createUserDTO.setRole(Role.EMPLOYEE);
        }

        if ("INTERN".equalsIgnoreCase(createUserDTO.getRole().name())) {
            throw new BadRequestException(ErrorDictionaryConstraints.CREATED_FOR_EMPLOYEE_OR_GUEST_ONLY.getMessage());
        }

        User existedUser = getByEmail(createUserDTO.getEmail()).getBody();
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

    public ResponseEntity<Page<User>> findAll(Pageable pageable) throws BadRequestException {
        Page<User> users = userRepository.findAll(pageable);
        if (users.isEmpty()) {
            throw new BadRequestException(ErrorDictionaryConstraints.USERS_IS_EMPTY.getMessage());
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    public ResponseEntity<Page<User>> findUserByRole(Role role, Pageable pageable) throws BadRequestException {
        Page<User> users = userRepository.findByRole(role, pageable);
        for (User user : users.getContent()) {
            String key = BAN_PREFIX + user.getId();
            String banInfoJson = redisTemplate.opsForValue().get(key);
            user.setActive(banInfoJson == null);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
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

    //Search for users that their email contain input string
    public ResponseEntity<List<User>> searchUsersByEmail(String email) {
        List<User> users = null;
        users = this.userRepository.findByEmailContainingIgnoreCase(email).orElse(null);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    public String banUser(int userId, Long durationInSeconds, String reason) throws JsonProcessingException {
        Map<String, Object> banInfo = new HashMap<>();
        banInfo.put("duration", durationInSeconds);
        banInfo.put("reason", reason);

        String key = BAN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(banInfo), durationInSeconds, TimeUnit.SECONDS);

        return "User " + userId + " has been banned for " + durationInSeconds + " days.";
    }

    public Map<String, Object> getBanStatus(int userId) throws JsonProcessingException {
        String key = BAN_PREFIX + userId;
        String banInfoJson = redisTemplate.opsForValue().get(key);

        if (banInfoJson == null) {
            return Map.of("userId", userId, "banned", false);
        }

        Map<String, Object> banInfo = objectMapper.readValue(banInfoJson, Map.class);
        Long remainingTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        banInfo.put("userId", userId);
        banInfo.put("remainingDuration", remainingTime);
        return banInfo;
    }

    public String unbanUser(int userId) {
        String key = BAN_PREFIX + userId;
        redisTemplate.delete(key);
        return "User " + userId + " has been unbanned.";
    }

    public ResponseEntity<List<User>> findUserByClassroom_Id(Integer classId) {
        List<User> users = null;
        users = this.userRepository.findUserByClassroom_Id(classId).orElse(null);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    }