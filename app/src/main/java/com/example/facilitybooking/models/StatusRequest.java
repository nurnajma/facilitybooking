package com.example.facilitybooking.models;

public class StatusRequest {

    private String status;
    private String adminNotes;
    private int facilityID;

    public StatusRequest(String status, String adminNotes, int facilityID) {
        this.status = status;
        this.adminNotes = adminNotes;
        this.facilityID = this.facilityID;
    }

    public String getStatus() {
        return status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }
    public int getFacilityID() {
        return facilityID;
    }


}
