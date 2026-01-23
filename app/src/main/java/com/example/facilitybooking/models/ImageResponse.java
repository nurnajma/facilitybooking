package com.example.facilitybooking.models;

import com.google.gson.annotations.SerializedName;

public class ImageResponse {
    private int imageID;
    private int facilityID;
    @SerializedName("imageURL")
    private String imageURL;
    @SerializedName("image_url")
    private String image_url;
    @SerializedName("url")
    private String url;
    private int isPrimary;
    private String createdAt;

    public ImageResponse() {}

    public int getImageID() { return imageID; }
    public void setImageID(int imageID) { this.imageID = imageID; }

    public int getFacilityID() { return facilityID; }
    public void setFacilityID(int facilityID) { this.facilityID = facilityID; }

    // Return the first non-empty URL value from possible server keys
    public String getImageURL() {
        if (imageURL != null && !imageURL.isEmpty()) return imageURL;
        if (image_url != null && !image_url.isEmpty()) return image_url;
        if (url != null && !url.isEmpty()) return url;
        return null;
    }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public int getIsPrimary() { return isPrimary; }
    public void setIsPrimary(int isPrimary) { this.isPrimary = isPrimary; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
