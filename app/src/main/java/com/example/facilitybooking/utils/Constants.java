package com.example.facilitybooking.utils;

import android.graphics.Color;

public final class Constants {
    private Constants() {}

    // ====== Booking status values (stored/transmitted) ======
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_COMPLETED = "completed";

    // ====== Common UI messages ======
    public static final String MSG_NETWORK_ERROR = "Network error. Please try again.";
    public static final String MSG_GENERIC_ERROR = "Something went wrong. Please try again.";
    public static final String MSG_SESSION_EXPIRED = "Session expired. Please log in again.";

    public static final String MSG_SELECT_DATE = "Please select a date";
    public static final String MSG_INVALID_TIME_RANGE = "End time must be after start time";
    public static final String MSG_ENTER_PURPOSE = "Please enter a purpose";

    public static final String MSG_BOOKING_CREATED = "Booking created successfully";
    public static final String MSG_BOOKING_APPROVED = "Booking approved";
    public static final String MSG_BOOKING_REJECTED = "Booking rejected";
    public static final String MSG_BOOKING_CANCELLED = "Booking cancelled";
    public static final String MSG_BOOKING_DELETED = "Booking deleted";

    public static final String MSG_CONFIRM_CANCEL = "Are you sure you want to cancel this booking?";
    public static final String MSG_CONFIRM_DELETE = "Are you sure you want to delete this item?";

    // ====== Role helpers ======
    public static boolean isAdmin(String role) {
        if (role == null) return false;
        String r = role.trim().toLowerCase();
        return r.equals("admin") || r.equals("administrator");
    }

    // ====== Status helpers ======
    public static String normalizeStatus(String status) {
        if (status == null) return "";
        return status.trim().toLowerCase();
    }

    public static int getStatusColor(String status) {
        String s = normalizeStatus(status);
        if (STATUS_APPROVED.equals(s)) return Color.parseColor("#2E7D32"); // green
        if (STATUS_PENDING.equals(s)) return Color.parseColor("#EF6C00"); // orange
        if (STATUS_REJECTED.equals(s)) return Color.parseColor("#C62828"); // red
        if (STATUS_CANCELLED.equals(s)) return Color.parseColor("#546E7A"); // grey/blue
        if (STATUS_COMPLETED.equals(s)) return Color.parseColor("#1565C0"); // blue
        return Color.parseColor("#616161"); // default grey
    }
}

