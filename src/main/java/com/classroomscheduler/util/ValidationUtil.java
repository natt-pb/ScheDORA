package com.classroomscheduler.util;

public class ValidationUtil {

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates capacity is a positive numeric value.
     */
    public static boolean isValidCapacity(String capacityStr) {
        if (isEmpty(capacityStr)) return false;
        try {
            int capacity = Integer.parseInt(capacityStr.trim());
            return capacity > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates time format matches either 12-hour AM/PM (e.g. "09:30 AM") or 24-hour (e.g. "14:00").
     */
    public static boolean isValidTimeFormat(String timeStr) {
        if (isEmpty(timeStr)) return false;
        String timeRegex12 = "^(0?[1-9]|1[0-2]):[0-5][0-9]\\s*(AM|PM|am|pm)$";
        String timeRegex24 = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return timeStr.matches(timeRegex12) || timeStr.matches(timeRegex24);
    }
}
