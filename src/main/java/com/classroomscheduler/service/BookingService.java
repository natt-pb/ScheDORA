package com.classroomscheduler.service;

import com.classroomscheduler.dao.BlockDAO;
import com.classroomscheduler.dao.BookingDAO;
import com.classroomscheduler.model.Block;
import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.Room;
import com.classroomscheduler.dao.RoomDAO;

import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ConflictDetectionService conflictService = new ConflictDetectionService();
    private final NotificationService notificationService = new NotificationService();
    private final ActivityLogService activityLogService = new ActivityLogService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();

    public String requestBooking(Booking booking) {
        // 1. Perform validations
        if (booking.getCourse() == null || booking.getCourse().trim().isEmpty() ||
            booking.getLecturer() == null || booking.getLecturer().trim().isEmpty() ||
            booking.getBookingDate() == null || booking.getBookingDate().trim().isEmpty() ||
            booking.getStartTime() == null || booking.getStartTime().trim().isEmpty() ||
            booking.getEndTime() == null || booking.getEndTime().trim().isEmpty()) {
            return "Invalid input: All required fields must be completed.";
        }

        // 2. Perform Conflict Detection
        boolean isConflicted = conflictService.hasConflict(
                booking.getRoomId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        if (isConflicted) {
            return "Room already occupied: Another booking or official class exists during this period.";
        }

        // 3. Auto-assign Facility Manager based on room's block
        Room room = roomDAO.getRoomById(booking.getRoomId());
        if (room != null && room.getBlockId() > 0) {
            int managerId = blockDAO.getManagerIdForBlock(room.getBlockId());
            if (managerId > 0) {
                booking.setAssignedManagerId(managerId);
            }
        }

        // 4. Save booking to DB
        boolean success = bookingDAO.createBooking(booking);
        if (success) {
            // Log activity
            activityLogService.log(booking.getUserId(), "BOOKING_SUBMITTED",
                "Booking request for " + booking.getCourse() + " in room #" + booking.getRoomId());

            // Notify assigned manager
            if (booking.getAssignedManagerId() > 0) {
                notificationService.notifyUser(booking.getAssignedManagerId(),
                    "New Booking Request",
                    "A new booking request for " + booking.getCourse() + " has been submitted and requires your approval.",
                    "BOOKING_SUBMITTED");
            }

            // Notify requester
            notificationService.notifyUser(booking.getUserId(),
                "Booking Request Submitted",
                "Your booking request for " + booking.getCourse() + " has been submitted successfully.",
                "BOOKING_SUBMITTED");

            return "Success";
        } else {
            return "Error: Could not save the reservation to the database.";
        }
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.getBookings();
    }

    public List<Booking> getBookingsByUser(int userId) {
        return bookingDAO.getBookingsByUserId(userId);
    }

    public List<Booking> getPendingBookings() {
        return bookingDAO.getPendingBookings();
    }

    public List<Booking> getPendingBookingsForManager(int managerId) {
        List<Block> blocks = blockDAO.getBlocksByManager(managerId);
        List<Integer> blockIds = blocks.stream().map(Block::getBlockId).collect(Collectors.toList());
        return bookingDAO.getPendingBookingsByBlockIds(blockIds);
    }

    public boolean approveBooking(int bookingId, int approverId) {
        boolean result = bookingDAO.updateBookingStatus(bookingId, "APPROVED");
        if (result) {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking != null) {
                // Notify requester
                notificationService.notifyUser(booking.getUserId(),
                    "Booking Approved",
                    "Your booking request for " + booking.getCourse() + " has been approved.",
                    "BOOKING_APPROVED");
                activityLogService.log(approverId, "BOOKING_APPROVED",
                    "Approved booking #" + bookingId + " for " + booking.getCourse());
            }
        }
        return result;
    }

    public boolean rejectBooking(int bookingId, String reason, int alternativeRoomId, int rejecterId) {
        boolean result = bookingDAO.rejectBookingWithReason(bookingId, reason, alternativeRoomId);
        if (result) {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking != null) {
                String message = "Your booking request for " + booking.getCourse() + " has been rejected.";
                if (reason != null && !reason.isEmpty()) {
                    message += " Reason: " + reason;
                }
                if (booking.getAlternativeRoomName() != null) {
                    message += " Suggested alternative: " + booking.getAlternativeRoomName();
                }
                notificationService.notifyUser(booking.getUserId(),
                    "Booking Rejected",
                    message,
                    "BOOKING_REJECTED");
                activityLogService.log(rejecterId, "BOOKING_REJECTED",
                    "Rejected booking #" + bookingId + " for " + booking.getCourse());
            }
        }
        return result;
    }

    public boolean cancelBooking(int bookingId) {
        return bookingDAO.updateBookingStatus(bookingId, "CANCELLED");
    }

    public boolean releaseBooking(int bookingId, int userId) {
        boolean result = bookingDAO.updateBookingStatus(bookingId, "RELEASED");
        if (result) {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking != null) {
                notificationService.notifyUser(userId,
                    "Room Released",
                    "The room for " + booking.getCourse() + " has been released and is now available.",
                    "INFO");
                activityLogService.log(userId, "BOOKING_RELEASED",
                    "Released booking #" + bookingId + " for " + booking.getCourse());
            }
        }
        return result;
    }
}
