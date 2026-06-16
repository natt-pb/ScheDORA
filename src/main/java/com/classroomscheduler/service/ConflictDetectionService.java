package com.classroomscheduler.service;

import com.classroomscheduler.dao.BookingDAO;
import com.classroomscheduler.dao.TimetableDAO;
import com.classroomscheduler.model.Booking;
import com.classroomscheduler.model.Timetable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ConflictDetectionService {
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Operating hours in minutes from midnight
    private static final int OPERATING_START = 6 * 60 + 30;  // 6:30 AM
    private static final int OPERATING_END = 20 * 60 + 30;    // 8:30 PM
    private static final int MAX_SESSION_MINUTES = 3 * 60;     // 3 hours

    public boolean hasConflict(int roomId, String bookingDate, String startTimeStr, String endTimeStr) {
        int newStart = parseTimeToMinutes(startTimeStr);
        int newEnd = parseTimeToMinutes(endTimeStr);

        if (newStart >= newEnd) {
            return true; // Invalid time range is treated as a conflict/error
        }

        // 1. Check conflicts against official Timetable
        String dayOfWeek = getDayOfWeekFromDate(bookingDate);
        List<Timetable> timetableSlots = timetableDAO.getTimetablesByRoom(roomId);
        for (Timetable t : timetableSlots) {
            if (t.getDay().equalsIgnoreCase(dayOfWeek)) {
                int tStart = parseTimeToMinutes(t.getStartTime());
                int tEnd = parseTimeToMinutes(t.getEndTime());
                // Check overlap: newStart < existingEnd AND newEnd > existingStart
                if (newStart < tEnd && newEnd > tStart) {
                    System.out.printf("Conflict detected with Timetable: %s (%s - %s)%n",
                            t.getCourseCode(), t.getStartTime(), t.getEndTime());
                    return true;
                }
            }
        }

        // 2. Check conflicts against existing active bookings
        List<Booking> bookings = bookingDAO.getBookings();
        for (Booking b : bookings) {
            if (b.getRoomId() == roomId && b.getBookingDate().equals(bookingDate)) {
                // Ignore rejected or cancelled bookings
                if (b.getStatus().equalsIgnoreCase("REJECTED") || b.getStatus().equalsIgnoreCase("CANCELLED") || b.getStatus().equalsIgnoreCase("RELEASED")) {
                    continue;
                }
                int bStart = parseTimeToMinutes(b.getStartTime());
                int bEnd = parseTimeToMinutes(b.getEndTime());
                if (newStart < bEnd && newEnd > bStart) {
                    System.out.printf("Conflict detected with Booking ID %d: %s (%s - %s)%n",
                            b.getBookingId(), b.getCourse(), b.getStartTime(), b.getEndTime());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validates that the booking date is a weekday (Mon-Fri).
     */
    public static boolean isWeekday(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            DayOfWeek dow = date.getDayOfWeek();
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that the time slot falls within operating hours (6:30 AM - 8:30 PM).
     */
    public static boolean isWithinOperatingHours(String startTimeStr, String endTimeStr) {
        int start = parseTimeToMinutes(startTimeStr);
        int end = parseTimeToMinutes(endTimeStr);
        return start >= OPERATING_START && end <= OPERATING_END && start < end;
    }

    /**
     * Validates that the session duration does not exceed 3 hours.
     */
    public static boolean isValidDuration(String startTimeStr, String endTimeStr) {
        int start = parseTimeToMinutes(startTimeStr);
        int end = parseTimeToMinutes(endTimeStr);
        int duration = end - start;
        return duration > 0 && duration <= MAX_SESSION_MINUTES;
    }

    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0;
        timeStr = timeStr.trim().toUpperCase();
        boolean pm = false;
        if (timeStr.endsWith("PM")) {
            pm = true;
            timeStr = timeStr.substring(0, timeStr.length() - 2).trim();
        } else if (timeStr.endsWith("AM")) {
            timeStr = timeStr.substring(0, timeStr.length() - 2).trim();
        }
        String[] parts = timeStr.split(":");
        if (parts.length < 2) return 0;
        try {
            int hour = Integer.parseInt(parts[0].trim());
            int min = Integer.parseInt(parts[1].trim());
            if (pm && hour < 12) hour += 12;
            if (!pm && hour == 12) hour = 0;
            return hour * 60 + min;
        } catch (NumberFormatException e) {
            System.err.println("ConflictDetectionService: Error parsing time: " + timeStr);
            return 0;
        }
    }

    public static String getDayOfWeekFromDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            System.err.println("ConflictDetectionService: Error formatting day of week from date: " + dateStr);
            return "Monday"; // Fallback
        }
    }
}
