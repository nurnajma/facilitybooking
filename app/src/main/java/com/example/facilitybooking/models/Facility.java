package com.example.facilitybooking.models;

import com.google.gson.annotations.SerializedName;

public class Facility {
    @SerializedName(value = "facilityID", alternate = {"facility_id", "facilityId", "id"})
    private int facilityID;

    @SerializedName(value = "facilityName", alternate = {"facility_name", "name"})
    private String facilityName;

    @SerializedName(value = "description", alternate = {"facilityDescription"})
    private String description;

    @SerializedName(value = "capacity", alternate = {"maxCapacity"})
    private int capacity;

    @SerializedName(value = "hourlyRate", alternate = {"hourly_rate", "rate"})
    private double hourlyRate;

    @SerializedName(value = "location", alternate = {"facilityLocation"})
    private String location;

    @SerializedName(value = "status", alternate = {"facilityStatus"})
    private String status;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    @SerializedName(value = "imageUrl", alternate = {"image_url", "image"})
    private String imageUrl; // new optional image URL

    public Facility() {}

    public int getFacilityID() { return facilityID; }
    public void setFacilityID(int facilityID) { this.facilityID = facilityID; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Facility{" +
                "facilityID=" + facilityID +
                ", facilityName='" + facilityName + '\'' +
                ", capacity=" + capacity +
                ", hourlyRate=" + hourlyRate +
                ", status='" + status + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}