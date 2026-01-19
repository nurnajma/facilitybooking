package com.example.facilitybooking.remote;

import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.models.Facility;
import java.util.List;
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
                               @Field("status") String status);

    @FormUrlEncoded
    @POST("facilities/{id}")
    Call<Facility> updateFacility(@Header("api-key") String apiKey, @Path("id") int id,
                                  @Field("facilityName") String facilityName,
                                  @Field("description") String description,
                                  @Field("capacity") int capacity,
                                  @Field("hourlyRate") double hourlyRate,
                                  @Field("location") String location,
                                  @Field("status") String status);

    @DELETE("facilities/{id}")
    Call<DeleteResponse> deleteFacility(@Header("api-key") String apiKey, @Path("id") int id);
}