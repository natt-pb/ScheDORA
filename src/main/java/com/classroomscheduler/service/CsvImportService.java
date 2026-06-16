package com.classroomscheduler.service;

import com.classroomscheduler.dao.UserDAO;
import com.classroomscheduler.dao.TimetableDAO;
import com.classroomscheduler.model.User;
import com.classroomscheduler.model.Timetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvImportService {
    private final UserDAO userDAO = new UserDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    /**
     * Import students from CSV. Expected columns:
     * name, username, password, programme, level, semester
     * Returns count of successfully imported records.
     */
    public int importStudents(File csvFile) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // Skip header
            if (header == null) return 0;

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 6) continue;

                User user = new User();
                user.setName(cols[0].trim());
                user.setUsername(cols[1].trim());
                user.setPassword(AuthenticationService.hashPassword(cols[2].trim()));
                user.setRole("STUDENT");
                user.setProgramme(cols[3].trim());
                user.setLevel(cols[4].trim());
                user.setSemester(cols[5].trim());
                user.setMustChangePassword(true);

                // Check if username already exists
                if (userDAO.getUserByUsername(user.getUsername()) == null) {
                    if (userDAO.addUser(user)) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("CsvImportService: Error importing students: " + e.getMessage());
        }
        return count;
    }

    /**
     * Import timetable from CSV. Expected columns:
     * course_code, course_name, lecturer, room_name, day, start_time, end_time, programme, level, semester
     * Returns count of successfully imported records.
     */
    public int importTimetable(File csvFile) {
        int count = 0;
        com.classroomscheduler.dao.RoomDAO roomDAO = new com.classroomscheduler.dao.RoomDAO();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // Skip header
            if (header == null) return 0;

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 10) continue;

                String roomName = cols[3].trim();
                com.classroomscheduler.model.Room room = roomDAO.getRoomByName(roomName);
                if (room == null) {
                    System.err.println("CsvImportService: Room not found: " + roomName);
                    continue;
                }

                Timetable t = new Timetable();
                t.setCourseCode(cols[0].trim());
                t.setCourseName(cols[1].trim());
                t.setLecturer(cols[2].trim());
                t.setRoomId(room.getRoomId());
                t.setDay(cols[4].trim());
                t.setStartTime(cols[5].trim());
                t.setEndTime(cols[6].trim());
                t.setProgramme(cols[7].trim());
                t.setLevel(cols[8].trim());
                t.setSemester(cols[9].trim());

                if (timetableDAO.addTimetable(t)) {
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("CsvImportService: Error importing timetable: " + e.getMessage());
        }
        return count;
    }
}
