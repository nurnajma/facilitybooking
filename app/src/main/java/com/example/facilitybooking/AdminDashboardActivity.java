package com.example.facilitybooking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvTotalBookings;
    private TextView tvPendingBookings;
    private TextView tvTotalFacilities;
    private TextView tvTotalRevenue;
    private CardView cardManageFacilities;
    private CardView cardAllBookings;
    private BottomNavigationView bottomNavigation;
    private BookingService bookingService;
    private FacilityService facilityService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvPendingBookings = findViewById(R.id.tvPendingBookings);
        tvTotalFacilities = findViewById(R.id.tvTotalFacilities);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        cardManageFacilities = findViewById(R.id.cardManageFacilities);
        cardAllBookings = findViewById(R.id.cardAllBookings);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        bookingService = ApiUtils.getBookingService();
        facilityService = ApiUtils.getFacilityService();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        User user = spm.getUser();

        // ========== ROLE-BASED ACCESS CONTROL ==========
        // Only admins should access this dashboard
        // Redirect regular users to UserDashboardActivity
        if (!Constants.isAdmin(user.getRole())) {
            finish();
            startActivity(new Intent(this, UserDashboardActivity.class));
            return;
        }

        tvWelcome.setText("Welcome, " + user.getUsername() + "!");

        cardManageFacilities.setOnClickListener(v ->
                startActivity(new Intent(this, ManageFacilitiesActivity.class)));

        cardAllBookings.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingsActivity.class)));

        setupBottomNavigation(spm);
        
        // Load statistics
        loadStatistics(user.getToken());
    }

    private void setupBottomNavigation(SharedPrefManager spm) {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already here
                return true;
            } else if (id == R.id.nav_bookings) {
                Intent intent = new Intent(this, AdminBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        // Refresh statistics when returning to dashboard
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (spm.isLoggedIn()) {
            User user = spm.getUser();
            loadStatistics(user.getToken());
        }
    }
    
    private void loadStatistics(String token) {
        // Load bookings statistics
        Call<List<Booking>> bookingsCall = bookingService.getAllBookings(token);
        bookingsCall.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<Booking> bookings = response.body();
                    updateBookingStatistics(bookings);
                } else {
                    Log.e("AdminDashboard", "Failed to load bookings: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Log.e("AdminDashboard", "Error loading bookings: " + t.getMessage());
            }
        });
        
        // Load facilities statistics
        Call<List<Facility>> facilitiesCall = facilityService.getAllFacilities(token);
        facilitiesCall.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<Facility> facilities = response.body();
                    updateFacilityStatistics(facilities);
                } else {
                    Log.e("AdminDashboard", "Failed to load facilities: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                Log.e("AdminDashboard", "Error loading facilities: " + t.getMessage());
            }
        });
    }
    
    private void updateBookingStatistics(List<Booking> bookings) {
        if (bookings == null) {
            tvTotalBookings.setText("0");
            tvPendingBookings.setText("0");
            tvTotalRevenue.setText("RM 0.00");
            return;
        }
        
        int totalBookings = bookings.size();
        int pendingBookings = 0;
        double totalRevenue = 0.0;
        
        for (Booking booking : bookings) {
            String status = Constants.normalizeStatus(booking.getStatus());
            if (Constants.STATUS_PENDING.equals(status)) {
                pendingBookings++;
            }
            // Calculate revenue from approved bookings only
            if (Constants.STATUS_APPROVED.equals(status)) {
                totalRevenue += booking.getTotalCost();
            }
        }
        
        tvTotalBookings.setText(String.valueOf(totalBookings));
        tvPendingBookings.setText(String.valueOf(pendingBookings));
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvTotalRevenue.setText("RM " + df.format(totalRevenue));
    }
    
    private void updateFacilityStatistics(List<Facility> facilities) {
        if (facilities == null) {
            tvTotalFacilities.setText("0");
            return;
        }
        
        tvTotalFacilities.setText(String.valueOf(facilities.size()));
    }
}
