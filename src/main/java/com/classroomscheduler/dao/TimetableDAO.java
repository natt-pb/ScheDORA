package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.Timetable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public List<Timetable> getTimetables() {
        List<Timetable> schedules = new ArrayList<>();
        String sql = "SELECT t.*, r.room_name FROM timetable t JOIN rooms r ON t.room_id = r.room_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(extractTimetableFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error getting timetables: " + e.getMessage());
        }
        return schedules;
    }

    public boolean addTimetable(Timetable timetable) {
        String sql = "INSERT INTO timetable (course_code, course_name, lecturer, room_id, day, start_time, end_time, programme, level, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, timetable.getCourseCode());
            pstmt.setString(2, timetable.getCourseName());
            pstmt.setString(3, timetable.getLecturer());
            pstmt.setInt(4, timetable.getRoomId());
            pstmt.setString(5, timetable.getDay());
            pstmt.setString(6, timetable.getStartTime());
            pstmt.setString(7, timetable.getEndTime());
            pstmt.setString(8, timetable.getProgramme());
            pstmt.setString(9, timetable.getLevel());
            pstmt.setString(10, timetable.getSemester());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        timetable.setScheduleId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error adding timetable slot: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteTimetable(int scheduleId) {
        String sql = "DELETE FROM timetable WHERE schedule_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error deleting timetable: " + e.getMessage());
        }
        return false;
    }

    public List<Timetable> getTimetablesByRoom(int roomId) {
        List<Timetable> schedules = new ArrayList<>();
        String sql = "SELECT t.*, r.room_name FROM timetable t JOIN rooms r ON t.room_id = r.room_id WHERE t.room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(extractTimetableFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error getting timetables by room: " + e.getMessage());
        }
        return schedules;
    }

    public List<Timetable> getTimetableByProgramme(String programme, String level, String semester) {
        List<Timetable> schedules = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.*, r.room_name FROM timetable t JOIN rooms r ON t.room_id = r.room_id WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (programme != null && !programme.isEmpty()) {
            sql.append(" AND t.programme = ?");
            params.add(programme);
        }
        if (level != null && !level.isEmpty()) {
            sql.append(" AND t.level = ?");
            params.add(level);
        }
        if (semester != null && !semester.isEmpty()) {
            sql.append(" AND t.semester = ?");
            params.add(semester);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(extractTimetableFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error getting timetable by programme: " + e.getMessage());
        }
        return schedules;
    }

    public List<Timetable> filterTimetable(String dept, String level, String building) {
        List<Timetable> schedules = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.*, r.room_name FROM timetable t JOIN rooms r ON t.room_id = r.room_id WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Filter by department/programme
        if (dept != null && !dept.isEmpty() && !dept.equalsIgnoreCase("All Departments") && !dept.equalsIgnoreCase("All Programmes")) {
            sql.append(" AND (t.programme = ? OR t.course_code LIKE ?)");
            params.add(dept);
            // Also match by course code prefix
            if (dept.contains("Computer Science")) {
                params.add("CSC%");
            } else if (dept.contains("Psychology")) {
                params.add("PSY%");
            } else if (dept.contains("Business")) {
                params.add("BUS%");
            } else {
                params.add(dept.substring(0, Math.min(3, dept.length())).toUpperCase() + "%");
            }
        }

        // Filter by Level
        if (level != null && !level.isEmpty() && !level.equalsIgnoreCase("All Levels")) {
            sql.append(" AND t.level = ?");
            params.add(level);
        }

        // Filter by building/block
        if (building != null && !building.isEmpty() && !building.equalsIgnoreCase("All Blocks")) {
            sql.append(" AND (r.building LIKE ? OR r.block_id IN (SELECT block_id FROM blocks WHERE block_name LIKE ?))");
            params.add("%" + building + "%");
            params.add("%" + building + "%");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(extractTimetableFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error filtering timetable: " + e.getMessage());
        }
        return schedules;
    }

    public void clearAllTimetable() {
        String sql = "DELETE FROM timetable";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("TimetableDAO: Error clearing timetable: " + e.getMessage());
        }
    }

    private Timetable extractTimetableFromResultSet(ResultSet rs) throws SQLException {
        Timetable t = new Timetable();
        t.setScheduleId(rs.getInt("schedule_id"));
        t.setCourseCode(rs.getString("course_code"));
        t.setCourseName(rs.getString("course_name"));
        t.setLecturer(rs.getString("lecturer"));
        t.setRoomId(rs.getInt("room_id"));
        t.setDay(rs.getString("day"));
        t.setStartTime(rs.getString("start_time"));
        t.setEndTime(rs.getString("end_time"));
        t.setProgramme(rs.getString("programme"));
        t.setLevel(rs.getString("level"));
        t.setSemester(rs.getString("semester"));
        t.setRoomName(rs.getString("room_name"));
        return t;
    }
}
