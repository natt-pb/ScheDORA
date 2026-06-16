package com.classroomscheduler.service;

import com.classroomscheduler.dao.BookingDAO;
import com.classroomscheduler.dao.RoomDAO;
import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.Room;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    /**
     * Room utilization: count of approved bookings per room in date range.
     */
    public Map<String, Integer> getRoomUtilization(String startDate, String endDate) {
        List<Booking> bookings = bookingDAO.getBookingsByDateRange(startDate, endDate);
        Map<String, Integer> utilization = new LinkedHashMap<>();

        for (Booking b : bookings) {
            if ("APPROVED".equalsIgnoreCase(b.getStatus())) {
                String roomName = b.getRoomName() != null ? b.getRoomName() : "Room #" + b.getRoomId();
                utilization.merge(roomName, 1, Integer::sum);
            }
        }
        return utilization;
    }

    /**
     * Get booking history with optional filters.
     */
    public List<Booking> getBookingHistory(String startDate, String endDate, String status, String roomFilter) {
        List<Booking> bookings;
        if (startDate != null && endDate != null) {
            bookings = bookingDAO.getBookingsByDateRange(startDate, endDate);
        } else {
            bookings = bookingDAO.getBookings();
        }

        return bookings.stream()
            .filter(b -> status == null || status.isEmpty() || "All".equalsIgnoreCase(status) || status.equalsIgnoreCase(b.getStatus()))
            .filter(b -> roomFilter == null || roomFilter.isEmpty() || "All".equalsIgnoreCase(roomFilter) ||
                    (b.getRoomName() != null && b.getRoomName().toLowerCase().contains(roomFilter.toLowerCase())))
            .collect(Collectors.toList());
    }

    /**
     * Export booking data to CSV file.
     */
    public boolean exportToCsv(List<Booking> bookings, File outputFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
            pw.println("Booking ID,Room,Course,Lecturer,Representative,Date,Start Time,End Time,Type,Status,Reason,Rejection Reason");
            for (Booking b : bookings) {
                pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,\"%s\",%s,\"%s\",\"%s\"%n",
                    b.getBookingId(),
                    safe(b.getRoomName()),
                    safe(b.getCourse()),
                    safe(b.getLecturer()),
                    safe(b.getRepresentative()),
                    safe(b.getBookingDate()),
                    safe(b.getStartTime()),
                    safe(b.getEndTime()),
                    safe(b.getBookingType()),
                    safe(b.getStatus()),
                    safe(b.getReason()),
                    safe(b.getRejectionReason()));
            }
            return true;
        } catch (Exception e) {
            System.err.println("ReportService: Error exporting CSV: " + e.getMessage());
        }
        return false;
    }

    private String safe(String s) {
        return s != null ? s.replace("\"", "\"\"") : "";
    }
}
