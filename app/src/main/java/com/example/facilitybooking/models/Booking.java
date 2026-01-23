package com.example.facilitybooking.models;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName(value = "bookingID", alternate = {"booking_id", "id"})
    private int bookingID;

    @SerializedName(value = "userID", alternate = {"user_id", "userId"})
    private int userID;

    @SerializedName(value = "facilityID", alternate = {"facility_id", "facilityId"})
    private int facilityID;

    @SerializedName(value = "bookingDate", alternate = {"booking_date", "date"})
    private String bookingDate;

    @SerializedName(value = "startTime", alternate = {"start_time"})
    private String startTime;

    @SerializedName(value = "endTime", alternate = {"end_time"})
    private String endTime;

    private String purpose;
    private String status;

    @SerializedName(value = "adminNotes", alternate = {"admin_notes"})
    private String adminNotes;

    @SerializedName(value = "totalCost", alternate = {"total_cost"})
    private double totalCost;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    private Facility facility;

    public Booking() {}

    public int getBookingID() { return bookingID; }
    public void setBookingID(int bookingID) { this.bookingID = bookingID; }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public int getFacilityID() { return facilityID; }
    public void setFacilityID(int facilityID) { this.facilityID = facilityID; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingID=" + bookingID +
                ", facilityID=" + facilityID +
                ", bookingDate='" + bookingDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}