package com.fsoft.fintern.services;

import com.fsoft.fintern.dtos.RoomDTO;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.repositories.RoomRepository;
import com.fsoft.fintern.utils.BeanUtilsHelper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public ResponseEntity<List<Room>> findAll() {
        List<Room> rooms = this.roomRepository.findAll();
        if (rooms.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        }
    }

    public ResponseEntity<Page<Room>> findAllWithPagination(Pageable pageable) throws BadRequestException {
        Page<Room> rooms = this.roomRepository.findAll(pageable);
        if (rooms.isEmpty()) {
            throw new BadRequestException("No rooms found");
        } else {
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        }
    }

    public ResponseEntity<Room> findById(int id) throws BadRequestException {
        Optional<Room> room = this.roomRepository.findById(id);
        if (room.isPresent()) {
            return new ResponseEntity<>(room.get(), HttpStatus.OK);
        } else {
            throw new BadRequestException("Room not found with id: " + id);
        }
    }

    public ResponseEntity<Room> createRoom(RoomDTO roomDTO) throws BadRequestException {
        // Check if room with same name already exists
        Optional<Room> existingRoom = this.roomRepository.findByRoomName(roomDTO.getRoomName());
        if (existingRoom.isPresent()) {
            throw new BadRequestException("Room with name '" + roomDTO.getRoomName() + "' already exists");
        }

        Room newRoom = new Room();
        newRoom.setActive(true);
        newRoom.setRoomName(roomDTO.getRoomName());
        newRoom.setLocation(roomDTO.getLocation());

        Room savedRoom = this.roomRepository.save(newRoom);
        return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
    }

    public ResponseEntity<Room> updateRoom(int id, RoomDTO roomDTO) throws BadRequestException {
        Room room = this.roomRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Room not found with id: " + id)
        );
        if (!roomDTO.getRoomName().equals(room.getRoomName())) {
            Optional<Room> roomWithSameName = this.roomRepository.findByRoomName(roomDTO.getRoomName());
            if (roomWithSameName.isPresent()) {
                throw new BadRequestException("Room with name '" + roomDTO.getRoomName() + "' already exists");
            }
        }

        BeanUtils.copyProperties(roomDTO, room, BeanUtilsHelper.getNullPropertyNames(roomDTO));
        this.roomRepository.save(room);
        return new ResponseEntity<>(room, HttpStatus.OK);
    }

    public ResponseEntity<Room> deleteRoom(int id) throws BadRequestException {
        Room room = this.roomRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Room not found with id: " + id)
        );
        room.setActive(false);
        room.setDeletedAt(Timestamp.from(Instant.now().plus(Duration.ofHours(7))));
        
        this.roomRepository.save(room);
        return new ResponseEntity<>(room, HttpStatus.OK);
    }
    
    public ResponseEntity<Room> restoreRoom(int id) throws BadRequestException {
        Room room = this.roomRepository.findById(id).orElseThrow(
            () -> new BadRequestException("Room not found with id: " + id)
        );
        
        if (room.getActive()) {
            throw new BadRequestException("Room is already active");
        }
        
        room.setActive(true);
        room.setDeletedAt(null);
        
        this.roomRepository.save(room);
        return new ResponseEntity<>(room, HttpStatus.OK);
    }
}