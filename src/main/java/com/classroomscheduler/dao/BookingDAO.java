package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, room_id, course, lecturer, representative, booking_date, start_time, end_time, booking_type, reason, status, assigned_manager_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, booking.getUserId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setString(3, booking.getCourse());
            pstmt.setString(4, booking.getLecturer());
            pstmt.setString(5, booking.getRepresentative());
            pstmt.setString(6, booking.getBookingDate());
            pstmt.setString(7, booking.getStartTime());
            pstmt.setString(8, booking.getEndTime());
            pstmt.setString(9, booking.getBookingType());
            pstmt.setString(10, booking.getReason());
            pstmt.setString(11, booking.getStatus());
            if (booking.getAssignedManagerId() > 0) {
                pstmt.setInt(12, booking.getAssignedManagerId());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error creating booking: " + e.getMessage());
        }
        return false;
    }

    public List<Booking> getBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error listing bookings: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id " +
                "WHERE b.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error listing user bookings: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> getPendingBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id " +
                "WHERE b.status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error listing pending bookings: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> getPendingBookingsByBlockIds(List<Integer> blockIds) {
        if (blockIds == null || blockIds.isEmpty()) return new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < blockIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id " +
                "WHERE b.status = 'PENDING' AND r.block_id IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < blockIds.size(); i++) {
                pstmt.setInt(i + 1, blockIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error listing pending bookings by block: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> getBookingsByDateRange(String startDate, String endDate) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id " +
                "WHERE b.booking_date BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error getting bookings by date range: " + e.getMessage());
        }
        return bookings;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error updating booking status: " + e.getMessage());
        }
        return false;
    }

    public boolean rejectBookingWithReason(int bookingId, String reason, int alternativeRoomId) {
        String sql = "UPDATE bookings SET status = 'REJECTED', rejection_reason = ?, alternative_room_id = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reason);
            if (alternativeRoomId > 0) {
                pstmt.setInt(2, alternativeRoomId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setInt(3, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error rejecting booking: " + e.getMessage());
        }
        return false;
    }

    public Booking getBookingById(int bookingId) {
        String sql = "SELECT b.*, r.room_name, u.name as user_real_name, ar.room_name as alt_room_name FROM bookings b " +
                "LEFT JOIN rooms r ON b.room_id = r.room_id " +
                "LEFT JOIN users u ON b.user_id = u.user_id " +
                "LEFT JOIN rooms ar ON b.alternative_room_id = ar.room_id " +
                "WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error getting booking by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingDAO: Error deleting booking: " + e.getMessage());
        }
        return false;
    }

    private Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setCourse(rs.getString("course"));
        b.setLecturer(rs.getString("lecturer"));
        b.setRepresentative(rs.getString("representative"));
        b.setBookingDate(rs.getString("booking_date"));
        b.setStartTime(rs.getString("start_time"));
        b.setEndTime(rs.getString("end_time"));
        b.setBookingType(rs.getString("booking_type"));
        b.setReason(rs.getString("reason"));
        b.setStatus(rs.getString("status"));
        b.setRejectionReason(rs.getString("rejection_reason"));
        b.setAlternativeRoomId(rs.getInt("alternative_room_id"));
        b.setAssignedManagerId(rs.getInt("assigned_manager_id"));

        b.setRoomName(rs.getString("room_name"));
        b.setRequesterName(rs.getString("user_real_name"));
        try {
            b.setAlternativeRoomName(rs.getString("alt_room_name"));
        } catch (SQLException ignored) {}
        return b;
    }
}
