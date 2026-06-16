package com.classroomscheduler.model;

public class Room {
    private int roomId;
    private String roomName;
    private int blockId;
    private String building;
    private int capacity;
    private String roomType;
    private boolean hasProjector;
    private boolean hasAc;
    private boolean hasWhiteboard;
    private String availabilityStatus;

    // UI helper
    private String blockName;

    public Room() {}

    public Room(int roomId, String roomName, String building, int capacity, String roomType) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }

    public boolean isHasAc() { return hasAc; }
    public void setHasAc(boolean hasAc) { this.hasAc = hasAc; }

    public boolean isHasWhiteboard() { return hasWhiteboard; }
    public void setHasWhiteboard(boolean hasWhiteboard) { this.hasWhiteboard = hasWhiteboard; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    @Override
    public String toString() {
        return roomName + " (" + building + " - Cap: " + capacity + ")";
    }
}
