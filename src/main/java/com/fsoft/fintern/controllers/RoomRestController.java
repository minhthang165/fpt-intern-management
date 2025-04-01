package com.fsoft.fintern.controllers;

import com.fsoft.fintern.dtos.RoomDTO;
import com.fsoft.fintern.models.Room;
import com.fsoft.fintern.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/rooms")
public class RoomRestController {
    private final RoomService roomService;

    @Autowired
    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("")
    @Operation(description = "Get all rooms with pagination")
    public ResponseEntity<Page<Room>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return this.roomService.findAllWithPagination(pageable);
    }

    @GetMapping("/all")
    @Operation(description = "Get all rooms without pagination")
    public ResponseEntity<List<Room>> getAllRoomsNoPagination() {
        return this.roomService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(description = "Get room by id")
    public ResponseEntity<Room> getRoomById(@PathVariable int id) throws BadRequestException {
        return this.roomService.findById(id);
    }

    @PostMapping("/create")
    @Operation(description = "Create a new room")
    public ResponseEntity<Room> createRoom(@RequestBody RoomDTO roomDTO) throws BadRequestException {
        return this.roomService.createRoom(roomDTO);
    }

    @PatchMapping("/update/{id}")
    @Operation(description = "Update room by id")
    public ResponseEntity<Room> updateRoom(@RequestBody RoomDTO roomDTO, @PathVariable int id) throws BadRequestException {
        return this.roomService.updateRoom(id, roomDTO);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(description = "Delete room by id (soft delete)")
    public ResponseEntity<Room> deleteRoom(@PathVariable int id) throws BadRequestException {
        return this.roomService.deleteRoom(id);
    }

    @PatchMapping("/restore/{id}")
    @Operation(description = "Restore a deleted room")
    public ResponseEntity<Room> restoreRoom(@PathVariable int id) throws BadRequestException {
        return this.roomService.restoreRoom(id);
    }
}