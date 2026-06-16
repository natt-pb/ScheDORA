package com.classroomscheduler.dao;

import com.classroomscheduler.database.DatabaseConnection;
import com.classroomscheduler.model.Block;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockDAO {

    public boolean addBlock(Block block) {
        String sql = "INSERT INTO blocks (block_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, block.getBlockName());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        block.setBlockId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error adding block: " + e.getMessage());
        }
        return false;
    }

    public List<Block> getAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        String sql = "SELECT * FROM blocks ORDER BY block_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Block b = new Block();
                b.setBlockId(rs.getInt("block_id"));
                b.setBlockName(rs.getString("block_name"));
                blocks.add(b);
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error listing blocks: " + e.getMessage());
        }
        return blocks;
    }

    public Block getBlockById(int id) {
        String sql = "SELECT * FROM blocks WHERE block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Block b = new Block();
                    b.setBlockId(rs.getInt("block_id"));
                    b.setBlockName(rs.getString("block_name"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error getting block: " + e.getMessage());
        }
        return null;
    }

    public Block getBlockByName(String name) {
        String sql = "SELECT * FROM blocks WHERE block_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Block b = new Block();
                    b.setBlockId(rs.getInt("block_id"));
                    b.setBlockName(rs.getString("block_name"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error getting block by name: " + e.getMessage());
        }
        return null;
    }

    public boolean updateBlock(Block block) {
        String sql = "UPDATE blocks SET block_name = ? WHERE block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, block.getBlockName());
            pstmt.setInt(2, block.getBlockId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error updating block: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteBlock(int blockId) {
        String sql = "DELETE FROM blocks WHERE block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blockId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error deleting block: " + e.getMessage());
        }
        return false;
    }

    // === Block-Manager assignments ===

    public boolean assignManager(int userId, int blockId) {
        String sql = "INSERT OR IGNORE INTO block_managers (user_id, block_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, blockId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error assigning manager: " + e.getMessage());
        }
        return false;
    }

    public boolean removeManager(int userId, int blockId) {
        String sql = "DELETE FROM block_managers WHERE user_id = ? AND block_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, blockId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error removing manager: " + e.getMessage());
        }
        return false;
    }

    public List<Block> getBlocksByManager(int userId) {
        List<Block> blocks = new ArrayList<>();
        String sql = "SELECT b.* FROM blocks b JOIN block_managers bm ON b.block_id = bm.block_id WHERE bm.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Block b = new Block();
                    b.setBlockId(rs.getInt("block_id"));
                    b.setBlockName(rs.getString("block_name"));
                    blocks.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error getting blocks by manager: " + e.getMessage());
        }
        return blocks;
    }

    public int getManagerIdForBlock(int blockId) {
        String sql = "SELECT user_id FROM block_managers WHERE block_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blockId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("BlockDAO: Error getting manager for block: " + e.getMessage());
        }
        return -1;
    }
}
