package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_name, block_id, building, capacity, room_type, has_projector, has_ac, has_whiteboard, availability_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getBlockId());
            pstmt.setString(3, room.getBuilding());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getRoomType());
            pstmt.setInt(6, room.isHasProjector() ? 1 : 0);
            pstmt.setInt(7, room.isHasAc() ? 1 : 0);
            pstmt.setInt(8, room.isHasWhiteboard() ? 1 : 0);
            pstmt.setString(9, room.getAvailabilityStatus() != null ? room.getAvailabilityStatus() : "AVAILABLE");
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setRoomId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error adding room: " + e.getMessage());
        }
        return false;
    }

    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error getting rooms: " + e.getMessage());
        }
        return rooms;
    }

    public Room getRoomById(int id) {
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id WHERE r.room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoomFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error getting room by ID: " + e.getMessage());
        }
        return null;
    }

    public Room getRoomByName(String name) {
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id WHERE r.room_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoomFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error getting room by Name: " + e.getMessage());
        }
        return null;
    }

    public List<Room> getRoomsByBlockId(int blockId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id WHERE r.block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blockId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error getting rooms by block: " + e.getMessage());
        }
        return rooms;
    }

    public List<Room> getRoomsByBlockIds(List<Integer> blockIds) {
        if (blockIds == null || blockIds.isEmpty()) return new ArrayList<>();
        List<Room> rooms = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < blockIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id WHERE r.block_id IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < blockIds.size(); i++) {
                pstmt.setInt(i + 1, blockIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error getting rooms by block ids: " + e.getMessage());
        }
        return rooms;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_name = ?, block_id = ?, building = ?, capacity = ?, room_type = ?, has_projector = ?, has_ac = ?, has_whiteboard = ?, availability_status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomName());
            pstmt.setInt(2, room.getBlockId());
            pstmt.setString(3, room.getBuilding());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getRoomType());
            pstmt.setInt(6, room.isHasProjector() ? 1 : 0);
            pstmt.setInt(7, room.isHasAc() ? 1 : 0);
            pstmt.setInt(8, room.isHasWhiteboard() ? 1 : 0);
            pstmt.setString(9, room.getAvailabilityStatus());
            pstmt.setInt(10, room.getRoomId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error updating room: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error deleting room: " + e.getMessage());
        }
        return false;
    }

    public List<Room> searchRoom(String query) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.block_name FROM rooms r LEFT JOIN blocks b ON r.block_id = b.block_id " +
                     "WHERE r.room_name LIKE ? OR r.building LIKE ? OR r.room_type LIKE ? OR b.block_name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcardQuery = "%" + query + "%";
            pstmt.setString(1, wildcardQuery);
            pstmt.setString(2, wildcardQuery);
            pstmt.setString(3, wildcardQuery);
            pstmt.setString(4, wildcardQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO: Error searching rooms: " + e.getMessage());
        }
        return rooms;
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomName(rs.getString("room_name"));
        r.setBlockId(rs.getInt("block_id"));
        r.setBuilding(rs.getString("building"));
        r.setCapacity(rs.getInt("capacity"));
        r.setRoomType(rs.getString("room_type"));
        r.setHasProjector(rs.getInt("has_projector") == 1);
        r.setHasAc(rs.getInt("has_ac") == 1);
        r.setHasWhiteboard(rs.getInt("has_whiteboard") == 1);
        r.setAvailabilityStatus(rs.getString("availability_status"));
        try {
            r.setBlockName(rs.getString("block_name"));
        } catch (SQLException ignored) {}
        return r;
    }
}
