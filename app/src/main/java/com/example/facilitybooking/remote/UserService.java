package com.example.facilitybooking.remote;

import com.example.facilitybooking.models.User;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/login")
    Call<User> loginEmail(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/register")
    Call<User> register(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("role") String role
    );

    @GET("users/{id}")
    Call<User> getUser(@Header("api-key") String apiKey, @Path("id") int id);
}