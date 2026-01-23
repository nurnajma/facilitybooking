package com.example.facilitybooking.utils;

import com.example.facilitybooking.models.Booking;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingValidationUtils {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Checks if a new booking overlaps with any existing bookings for the same facility on the same date.
     */
    public static String checkOverlap(String newDate, String newStart, String newEnd, List<Booking> existingBookings, int facilityId) {
        try {
            Date startNew = TIME_FORMAT.parse(newStart);
            Date endNew = TIME_FORMAT.parse(newEnd);

            for (Booking existing : existingBookings) {
                // Only check approved or pending bookings for the same facility and date
                if (existing.getFacilityID() == facilityId && 
                    existing.getBookingDate().equals(newDate) &&
                    !Constants.STATUS_CANCELLED.equalsIgnoreCase(existing.getStatus()) &&
                    !Constants.STATUS_REJECTED.equalsIgnoreCase(existing.getStatus())) {

                    Date startExist = TIME_FORMAT.parse(existing.getStartTime());
                    Date endExist = TIME_FORMAT.parse(existing.getEndTime());

                    // Overlap logic: (StartA < EndB) and (EndA > StartB)
                    if (startNew.before(endExist) && endNew.after(startExist)) {
                        return "This time slot overlaps with an existing booking (" + 
                               existing.getStartTime() + " - " + existing.getEndTime() + ")";
                    }
                }
            }
        } catch (ParseException e) {
            return "Error parsing time format";
        }
        return null; // No overlap
    }

    /**
     * Validates that the end time is after the start time.
     */
    public static boolean isValidTimeRange(String startTime, String endTime) {
        try {
            Date start = TIME_FORMAT.parse(startTime);
            Date end = TIME_FORMAT.parse(endTime);
            return end.after(start);
        } catch (ParseException e) {
            return false;
        }
    }
}
