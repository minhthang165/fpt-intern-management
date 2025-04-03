package com.fsoft.fintern.dtos;

public class RoomDTO {
    private Integer id;
    private String roomName;
    private Integer location;

    public RoomDTO() {
    }

    public RoomDTO(Integer id, String roomName, Integer location) {
        this.id = id;
        this.roomName = roomName;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getLocation() {
        return location;
    }

    public void setLocation(Integer location) {
        this.location = location;
    }
}