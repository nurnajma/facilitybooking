package com.example.facilitybooking.remote;

public class ApiUtils {
    // TODO: Change to YOUR pRESTige URL
    public static final String BASE_URL = "http://aptitude.my/2023268698/api/";

    public static UserService getUserService() {
        return RetrofitClient.getClient(BASE_URL).create(UserService.class);
    }

    public static FacilityService getFacilityService() {
        return RetrofitClient.getClient(BASE_URL).create(FacilityService.class);
    }

    public static BookingService getBookingService() {
        return RetrofitClient.getClient(BASE_URL).create(BookingService.class);
    }
}