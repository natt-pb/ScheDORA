package com.classroomscheduler.model;

public class Booking {
    private int bookingId;
    private int userId;
    private int roomId;
    private String course;
    private String lecturer;
    private String representative;
    private String bookingDate;
    private String startTime;
    private String endTime;
    private String bookingType; // Lecture, Quiz, Lab
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED, RELEASED
    private String rejectionReason;
    private int alternativeRoomId;
    private int assignedManagerId;

    // UI helper fields
    private String roomName;
    private String requesterName;
    private String alternativeRoomName;

    public Booking() {}

    public Booking(int bookingId, int userId, int roomId, String course, String lecturer, String representative,
                   String bookingDate, String startTime, String endTime, String bookingType, String reason, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.roomId = roomId;
        this.course = course;
        this.lecturer = lecturer;
        this.representative = representative;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingType = bookingType;
        this.reason = reason;
        this.status = status;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }

    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public int getAlternativeRoomId() { return alternativeRoomId; }
    public void setAlternativeRoomId(int alternativeRoomId) { this.alternativeRoomId = alternativeRoomId; }

    public int getAssignedManagerId() { return assignedManagerId; }
    public void setAssignedManagerId(int assignedManagerId) { this.assignedManagerId = assignedManagerId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getAlternativeRoomName() { return alternativeRoomName; }
    public void setAlternativeRoomName(String alternativeRoomName) { this.alternativeRoomName = alternativeRoomName; }
}
