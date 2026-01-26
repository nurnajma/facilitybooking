package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facilitybooking.adapters.BookingAdapter;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvMyBookings;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private BookingAdapter adapter;
    private BookingService bookingService;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvMyBookings = findViewById(R.id.rvMyBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNavigation();
        loadMyBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        bottomNavigation.setSelectedItemId(
                spm.isAdmin() ? R.id.nav_bookings : R.id.nav_my_bookings
        );
        loadMyBookings();
    }

    private void setupBottomNavigation() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        bottomNavigation.getMenu().clear();

        if (spm.isAdmin()) {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_admin);
        } else {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_user);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            boolean isAdmin = spm.isAdmin();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this,
                        isAdmin ? AdminDashboardActivity.class : UserDashboardActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return true;
        });
    }

    private void loadMyBookings() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        progressBar.setVisibility(View.VISIBLE);
        rvMyBookings.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        bookingService = ApiUtils.getBookingService();
        bookingService.getUserBookings(user.getToken(), user.getId())
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.code() == 200 && response.body() != null && !response.body().isEmpty()) {
                            setupAdapter(response.body());
                        } else {
                            performAllBookingsFallback(user.getToken(), user.getId());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                Constants.MSG_NETWORK_ERROR, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupAdapter(List<Booking> bookings) {
        adapter = new BookingAdapter(bookings, MyBookingsActivity.this,
                new BookingAdapter.OnBookingActionListener() {

                    @Override
                    public void onViewDetails(Booking booking) {
                        viewBookingDetails(booking);
                    }

                    @Override
                    public void onCancelBooking(Booking booking) {
                        cancelBooking(booking);
                    }

                    @Override
                    public void onEditBooking(Booking booking) {
                        editBooking(booking);
                    }
                });

        rvMyBookings.setAdapter(adapter);
        rvMyBookings.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void viewBookingDetails(Booking booking) {
        Intent intent = new Intent(this, BookingDetailsActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        startActivity(intent);
    }

    private void cancelBooking(Booking booking) {
        if (!"pending".equalsIgnoreCase(booking.getStatus())) {
            Toast.makeText(this, "Only pending bookings can be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage(Constants.MSG_CONFIRM_CANCEL)
                .setPositiveButton("Yes", (d, w) -> {
                    Intent intent = new Intent(this, BookingDetailsActivity.class);
                    intent.putExtra("bookingID", booking.getBookingID());
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void editBooking(Booking booking) {
        if (!"pending".equalsIgnoreCase(booking.getStatus())) {
            Toast.makeText(this, "Only pending bookings can be edited", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EditBookingActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        intent.putExtra("facilityID", booking.getFacilityID());
        intent.putExtra("bookingDate", booking.getBookingDate());
        intent.putExtra("startTime", booking.getStartTime());
        intent.putExtra("endTime", booking.getEndTime());
        intent.putExtra("purpose", booking.getPurpose());
        startActivity(intent);
    }

    private void performAllBookingsFallback(String token, int userID) {
        bookingService.getAllBookings(token).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<Booking> filtered = new ArrayList<>();
                    for (Booking b : response.body()) {
                        if (b.getUserID() == userID) filtered.add(b);
                    }

                    if (!filtered.isEmpty()) {
                        setupAdapter(filtered);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvMyBookings.setVisibility(View.GONE);
    }
}
