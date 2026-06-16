package com.classroomscheduler.model;

public class Timetable {
    private int scheduleId;
    private String courseCode;
    private String courseName;
    private String lecturer;
    private int roomId;
    private String day;
    private String startTime;
    private String endTime;
    private String programme;
    private String level;
    private String semester;

    // UI helper field
    private String roomName;

    public Timetable() {}

    public Timetable(int scheduleId, String courseCode, String courseName, String lecturer,
                     int roomId, String day, String startTime, String endTime) {
        this.scheduleId = scheduleId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturer = lecturer;
        this.roomId = roomId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}
