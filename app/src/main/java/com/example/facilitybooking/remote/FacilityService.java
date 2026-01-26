package com.example.facilitybooking.remote;

import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.models.Facility;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface FacilityService {
    @GET("facilities")
    Call<List<Facility>> getAllFacilities(@Header("api-key") String apiKey);

    @GET("facilities/{id}")
    Call<Facility> getFacility(@Header("api-key") String apiKey, @Path("id") int id);

    @FormUrlEncoded
    @POST("facilities")
    Call<Facility> addFacility(@Header("api-key") String apiKey,
                               @Field("facilityName") String facilityName,
                               @Field("description") String description,
                               @Field("capacity") int capacity,
                               @Field("hourlyRate") double hourlyRate,
                               @Field("location") String location,
                               @Field("status") String status,
                               @Field("imageUrl") String imageUrl);

    @FormUrlEncoded
    @POST("facilities/{id}")
    Call<Facility> updateFacility(@Header("api-key") String apiKey, @Path("id") int id,
                                  @Field("facilityName") String facilityName,
                                  @Field("description") String description,
                                  @Field("capacity") int capacity,
                                  @Field("hourlyRate") double hourlyRate,
                                  @Field("location") String location,
                                  @Field("status") String status,
                                  @Field("imageUrl") String imageUrl);

    @DELETE("facilities/{id}")
    Call<DeleteResponse> deleteFacility(@Header("api-key") String apiKey, @Path("id") int id);

    // Multipart upload for facility image (server may not support yet)
    @Multipart
    @POST("facilities/{id}/images")
    Call<com.example.facilitybooking.models.ImageResponse> uploadFacilityImage(@Header("api-key") String apiKey,
                                                                               @Path("id") int facilityId,
                                                                               @Part MultipartBody.Part file);

    // Add image record by URL (fallback)
    @FormUrlEncoded
    @POST("facilities/{id}/images")
    Call<com.example.facilitybooking.models.ImageResponse> addFacilityImageByUrl(@Header("api-key") String apiKey,
                                                                                 @Path("id") int facilityId,
                                                                                 @Field("imageUrl") String imageUrl,
                                                                                 @Field("isPrimary") int isPrimary);

    // Get images for a facility
    @GET("facilities/{id}/images")
    Call<java.util.List<com.example.facilitybooking.models.ImageResponse>> getFacilityImages(@Header("api-key") String apiKey, @Path("id") int facilityId);
}