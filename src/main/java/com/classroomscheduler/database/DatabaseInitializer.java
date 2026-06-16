package com.classroomscheduler.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initialize() {
        System.out.println("DatabaseInitializer: Initializing database tables...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            executeSchemaScript(conn);
            if (isTableEmpty(conn, "blocks")) {
                seedBlocks(conn);
            }
            if (isTableEmpty(conn, "users")) {
                seedUsers(conn);
            }
            if (isTableEmpty(conn, "block_managers")) {
                seedBlockManagers(conn);
            }
            if (isTableEmpty(conn, "rooms")) {
                seedRooms(conn);
            }
            if (isTableEmpty(conn, "timetable")) {
                seedTimetable(conn);
            }
            if (isTableEmpty(conn, "bookings")) {
                seedBookings(conn);
            }
            System.out.println("DatabaseInitializer: Initialization complete.");
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Error during database setup.");
            e.printStackTrace();
        }
    }

    private static void executeSchemaScript(Connection conn) throws Exception {
        InputStream is = DatabaseInitializer.class.getResourceAsStream("/database/Schema.sql");
        if (is == null) {
            System.err.println("DatabaseInitializer: Schema.sql file not found in resources!");
            return;
        }
        
        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        // Split by semicolon and run each query
        String[] statements = sql.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String query : statements) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query);
                }
            }
        }
        System.out.println("DatabaseInitializer: Tables checked/created successfully.");
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    // === Seed Blocks ===
    private static void seedBlocks(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding blocks...");
        String sql = "INSERT INTO blocks (block_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] blocks = {"NLT Block", "CALC Block", "SW Block", "LT Block", "G Block"};
            for (String name : blocks) {
                pstmt.setString(1, name);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // === Seed Users (5 roles) ===
    private static void seedUsers(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding default users...");
        String sql = "INSERT INTO users (name, username, password, role, programme, level, semester, staff_id, department, must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Main Admin
            setUser(pstmt, "System Administrator", "admin", hashPassword("admin123"), "MAIN_ADMIN",
                    null, null, null, null, null, 0);
            pstmt.addBatch();

            // Facility Managers
            setUser(pstmt, "Mr. Kwame Asante (NLT/CALC FM)", "facility1", hashPassword("facility123"), "FACILITY_MANAGER",
                    null, null, null, "FM001", "Facilities", 0);
            pstmt.addBatch();

            setUser(pstmt, "Mrs. Ama Mensah (SW/LT/G FM)", "facility2", hashPassword("facility123"), "FACILITY_MANAGER",
                    null, null, null, "FM002", "Facilities", 0);
            pstmt.addBatch();

            // Lecturers
            setUser(pstmt, "Dr. Arthur", "lecturer", hashPassword("lecturer123"), "LECTURER",
                    null, null, null, "LEC001", "Computer Science", 0);
            pstmt.addBatch();

            setUser(pstmt, "Prof. Taylor", "taylor", hashPassword("lecturer123"), "LECTURER",
                    null, null, null, "LEC002", "Computer Science", 0);
            pstmt.addBatch();

            // Student Rep
            setUser(pstmt, "Course Representative", "PS/CSC/23/0201", hashPassword("rep123"), "STUDENT_REP",
                    "Computer Science", "300", "1", null, "Computer Science", 0);
            pstmt.addBatch();

            // Student
            setUser(pstmt, "John Doe (Student)", "PS/CSC/23/0202", hashPassword("student123"), "STUDENT",
                    "Computer Science", "300", "1", null, "Computer Science", 1);
            pstmt.addBatch();

            pstmt.executeBatch();
        }
    }

    private static void setUser(PreparedStatement pstmt, String name, String username, String password, String role,
                                 String programme, String level, String semester, String staffId, String department, int mustChange) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, username);
        pstmt.setString(3, password);
        pstmt.setString(4, role);
        pstmt.setString(5, programme);
        pstmt.setString(6, level);
        pstmt.setString(7, semester);
        pstmt.setString(8, staffId);
        pstmt.setString(9, department);
        pstmt.setInt(10, mustChange);
    }

    // === Seed Block-Manager assignments ===
    private static void seedBlockManagers(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding block-manager assignments...");
        String sql = "INSERT INTO block_managers (user_id, block_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Facility1 (user_id=2) manages NLT Block (1) and CALC Block (2)
            pstmt.setInt(1, 2); pstmt.setInt(2, 1); pstmt.addBatch();
            pstmt.setInt(1, 2); pstmt.setInt(2, 2); pstmt.addBatch();
            // Facility2 (user_id=3) manages SW Block (3), LT Block (4), G Block (5)
            pstmt.setInt(1, 3); pstmt.setInt(2, 3); pstmt.addBatch();
            pstmt.setInt(1, 3); pstmt.setInt(2, 4); pstmt.addBatch();
            pstmt.setInt(1, 3); pstmt.setInt(2, 5); pstmt.addBatch();
            pstmt.executeBatch();
        }
    }

    // === Seed Rooms (93 rooms across 5 blocks) ===
    private static void seedRooms(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding classrooms...");
        String sql = "INSERT INTO rooms (room_name, block_id, building, capacity, room_type, has_projector, has_ac, has_whiteboard) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // === NLT Block (block_id=1): NLT 1-20, capacity 200 ===
            for (int i = 1; i <= 20; i++) {
                setRoom(pstmt, "NLT " + i, 1, "NLT Block", 200, "Lecture Theatre", 1, 0, 1);
                pstmt.addBatch();
            }

            // === CALC Block (block_id=2): CALC 1-13 (varying capacities) + Auditorium 900 ===
            for (int i = 1; i <= 13; i++) {
                int capacity;
                if (i == 1 || i == 2 || i == 3 || i == 7) {
                    capacity = 150;
                } else if (i == 8 || i == 9) {
                    capacity = 300;
                } else {
                    capacity = 100;
                }
                setRoom(pstmt, "CALC " + i, 2, "CALC Block", capacity, "Classroom", 1, 0, 1);
                pstmt.addBatch();
            }
            // CALC Auditorium 900
            setRoom(pstmt, "CALC Auditorium 900", 2, "CALC Block", 400, "Auditorium", 1, 1, 1);
            pstmt.addBatch();

            // === SW Block (block_id=3): SW 1-19, capacity 200 ===
            for (int i = 1; i <= 19; i++) {
                setRoom(pstmt, "SW " + i, 3, "SW Block", 200, "Classroom", 1, 0, 1);
                pstmt.addBatch();
            }

            // === LT Block (block_id=4): LT 1-19 (cap 50), LT 20-21 (cap 250), Main Auditorium (500) ===
            for (int i = 1; i <= 21; i++) {
                if (i >= 20) {
                    setRoom(pstmt, "LT " + i, 4, "LT Block", 250, "Lecture Theatre", 1, 1, 1);
                } else {
                    setRoom(pstmt, "LT " + i, 4, "LT Block", 50, "Classroom", 0, 0, 1);
                }
                pstmt.addBatch();
            }
            // LT Main Auditorium
            setRoom(pstmt, "LT Main Auditorium", 4, "LT Block", 500, "Auditorium", 1, 1, 1);
            pstmt.addBatch();

            // === G Block (block_id=5): G 1-18, capacity 100 ===
            for (int i = 1; i <= 18; i++) {
                setRoom(pstmt, "G " + i, 5, "G Block", 100, "Classroom", 0, 0, 1);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }
    }

    private static void setRoom(PreparedStatement pstmt, String name, int blockId, String building, int capacity,
                                 String type, int projector, int ac, int whiteboard) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setInt(2, blockId);
        pstmt.setString(3, building);
        pstmt.setInt(4, capacity);
        pstmt.setString(5, type);
        pstmt.setInt(6, projector);
        pstmt.setInt(7, ac);
        pstmt.setInt(8, whiteboard);
    }

    // === Seed Timetable ===
    private static void seedTimetable(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding official timetable...");
        String sql = "INSERT INTO timetable (course_code, course_name, lecturer, room_id, day, start_time, end_time, programme, level, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // CSC 300-level courses — Computer Science, Level 300, Semester 1
            Object[][] defaultTimetable = {
                {"CSC 301", "Database Systems", "Dr. Arthur", 1, "Monday", "09:30 AM", "11:30 AM", "Computer Science", "300", "1"},
                {"CSC 303", "Operating Systems", "Prof. Taylor", 34, "Monday", "01:00 PM", "03:00 PM", "Computer Science", "300", "1"},
                {"COM 201", "Communication Skills", "Kwame Ansah", 35, "Tuesday", "02:00 PM", "04:00 PM", "Computer Science", "300", "1"},
                {"CSC 305", "Software Engineering", "Dr. E. Osei", 54, "Wednesday", "10:00 AM", "12:00 PM", "Computer Science", "300", "1"},
                {"CSC 307", "Computer Networks", "Prof. Taylor", 73, "Thursday", "08:00 AM", "10:00 AM", "Computer Science", "300", "1"},
            };

            for (Object[] t : defaultTimetable) {
                pstmt.setString(1, (String) t[0]);
                pstmt.setString(2, (String) t[1]);
                pstmt.setString(3, (String) t[2]);
                pstmt.setInt(4, (Integer) t[3]);
                pstmt.setString(5, (String) t[4]);
                pstmt.setString(6, (String) t[5]);
                pstmt.setString(7, (String) t[6]);
                pstmt.setString(8, (String) t[7]);
                pstmt.setString(9, (String) t[8]);
                pstmt.setString(10, (String) t[9]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // === Seed Bookings ===
    private static void seedBookings(Connection conn) throws SQLException {
        System.out.println("DatabaseInitializer: Seeding sample bookings...");
        String sql = "INSERT INTO bookings (user_id, room_id, course, lecturer, representative, booking_date, start_time, end_time, booking_type, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] defaultBookings = {
                {6, 35, "COM 201", "Kwame Ansah", "Course Representative", "2026-06-15", "02:00 PM", "04:00 PM", "Lecture", "Air conditioning issue in original venue", "PENDING"},
                {6, 54, "CSC 305", "Dr. E. Osei", "Course Representative", "2026-06-16", "10:00 AM", "12:00 PM", "Lecture", "Projector bulb failure", "APPROVED"},
                {6, 76, "CSC 307", "Prof. Taylor", "Course Representative", "2026-06-17", "04:00 PM", "06:00 PM", "Lab", "Special laboratory assignment exam", "PENDING"}
            };

            for (Object[] b : defaultBookings) {
                pstmt.setInt(1, (Integer) b[0]);
                pstmt.setInt(2, (Integer) b[1]);
                pstmt.setString(3, (String) b[2]);
                pstmt.setString(4, (String) b[3]);
                pstmt.setString(5, (String) b[4]);
                pstmt.setString(6, (String) b[5]);
                pstmt.setString(7, (String) b[6]);
                pstmt.setString(8, (String) b[7]);
                pstmt.setString(9, (String) b[8]);
                pstmt.setString(10, (String) b[9]);
                pstmt.setString(11, (String) b[10]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
