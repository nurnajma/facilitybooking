package com.example.facilitybooking.remote;

import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.DeleteResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface BookingService {
    @GET("bookings")
    Call<List<Booking>> getAllBookings(@Header("api-key") String apiKey);

    @GET("bookings")
    Call<List<Booking>> getUserBookings(@Header("api-key") String apiKey, @Query("userID") int userID);

    @GET("bookings")
    Call<List<Booking>> getBookingsByStatus(@Header("api-key") String apiKey, @Query("status") String status);

    @GET("bookings/{id}")
    Call<Booking> getBooking(@Header("api-key") String apiKey, @Path("id") int id);

    @FormUrlEncoded
    @POST("bookings")
    Call<Booking> createBooking(@Header("api-key") String apiKey,
                                @Field("userID") int userID,
                                @Field("facilityID") int facilityID,
                                @Field("bookingDate") String bookingDate,
                                @Field("startTime") String startTime,
                                @Field("endTime") String endTime,
                                @Field("purpose") String purpose,
                                @Field("status") String status,
                                @Field("totalCost") double totalCost);

    // Fallback: send JSON body instead of form-encoded fields. Some backends expect JSON.
    @POST("bookings")
    Call<Booking> createBookingJson(@Header("api-key") String apiKey, @Body Booking booking);

    // More explicit JSON fallback that sends raw key/value map to control field names
    @POST("bookings")
    Call<Booking> createBookingJsonMap(@Header("api-key") String apiKey, @Body java.util.Map<String, Object> body);

    @FormUrlEncoded
    @POST("bookings/{id}")
    Call<Booking> updateBooking(@Header("api-key") String apiKey, @Path("id") int id,
                                @Field("userID") int userID,
                                @Field("facilityID") int facilityID,
                                @Field("bookingDate") String bookingDate,
                                @Field("startTime") String startTime,
                                @Field("endTime") String endTime,
                                @Field("purpose") String purpose,
                                @Field("status") String status,
                                @Field("adminNotes") String adminNotes,
                                @Field("totalCost") double totalCost);

    @DELETE("bookings/{id}")
    Call<DeleteResponse> deleteBooking(@Header("api-key") String apiKey, @Path("id") int id);
}