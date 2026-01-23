package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvMyBookings;
    private TextView tvEmptyState;
    private ProgressBar progressBar; // Loading indicator for API calls
    private BookingAdapter adapter;
    private BookingService bookingService;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvMyBookings = findViewById(R.id.rvMyBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar); // Initialize loading indicator
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Inflate user/admin menu depending on role
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        bottomNavigation.getMenu().clear();
        if (spm.isAdmin()) {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_admin);
        } else {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_user);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            SharedPrefManager spm2 = new SharedPrefManager(getApplicationContext());
            boolean isAdmin = spm2.isAdmin();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(MyBookingsActivity.this, isAdmin ? AdminDashboardActivity.class : UserDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_my_bookings && !isAdmin) {
                // already here
                return true;
            } else if (id == R.id.nav_bookings && isAdmin) {
                Intent intent = new Intent(MyBookingsActivity.this, AdminBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MyBookingsActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });

        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvMyBookings);

        loadMyBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Keep the bottom nav highlight in sync
        if (bottomNavigation != null) {
            // If admin menu is inflated, select bookings; otherwise select my_bookings
            SharedPrefManager spm3 = new SharedPrefManager(getApplicationContext());
            if (spm3.isAdmin()) bottomNavigation.setSelectedItemId(R.id.nav_bookings);
            else bottomNavigation.setSelectedItemId(R.id.nav_my_bookings);
        }
        // refresh list
        loadMyBookings();
    }

    private void loadMyBookings() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();
        int userID = user.getId();

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        rvMyBookings.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        bookingService = ApiUtils.getBookingService();
        Call<List<Booking>> call = bookingService.getUserBookings(token, userID);

        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);

                Log.d("MyBookings", "Response: " + response.code());

                if (response.code() == 200) {
                    List<Booking> bookings = response.body();
                    Log.d("MyBookings", "getUserBookings returned count=" + (bookings == null ? 0 : bookings.size()));
                    if (bookings != null && !bookings.isEmpty()) {
                        // Log booking items for diagnostics
                        for (Booking b : bookings) {
                            Log.d("MyBookings", "Booking item: id=" + b.getBookingID() + " userID=" + b.getUserID() + " facilityID=" + b.getFacilityID() + " status=" + b.getStatus());
                        }
                        adapter = new BookingAdapter(bookings, getApplicationContext());
                        rvMyBookings.setAdapter(adapter);
                        tvEmptyState.setVisibility(View.GONE);
                        rvMyBookings.setVisibility(View.VISIBLE);
                    } else {
                        // Fallback: attempt to retrieve ALL bookings and filter client-side by userID
                        Log.w("MyBookings", "getUserBookings returned empty; trying getAllBookings fallback");
                        performAllBookingsFallback(token, userID);
                    }
                } else if (response.code() == 204) {
                    // No Content - treat as empty and try fallback
                    Log.w("MyBookings", "getUserBookings returned 204 No Content; trying getAllBookings fallback");
                    performAllBookingsFallback(token, userID);
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), Constants.MSG_SESSION_EXPIRED, Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                } else {
                    Toast.makeText(getApplicationContext(), Constants.MSG_GENERIC_ERROR, Toast.LENGTH_LONG).show();
                    Log.e("MyBookings", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);

                Toast.makeText(getApplicationContext(), Constants.MSG_NETWORK_ERROR, Toast.LENGTH_LONG).show();
                Log.e("MyBookings", "Error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvMyBookings.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.booking_context_menu, menu);

        // Show/hide menu items based on booking status
        Booking selectedBooking = adapter.getSelectedItem();
        if (selectedBooking != null) {
            String status = selectedBooking.getStatus();
            if ("approved".equals(status) || "rejected".equals(status) || "completed".equals(status)) {
                menu.findItem(R.id.menu_cancel_booking).setVisible(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Booking selectedBooking = adapter.getSelectedItem();

        if (selectedBooking == null) {
            Toast.makeText(this, "No booking selected", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();

        if (itemId == R.id.menu_view_booking_details) {
            viewBookingDetails(selectedBooking);
        } else if (itemId == R.id.menu_cancel_booking) {
            cancelBooking(selectedBooking);
        }

        return super.onContextItemSelected(item);
    }

    private void viewBookingDetails(Booking booking) {
        Intent intent = new Intent(this, BookingDetailsActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        startActivity(intent);
    }

    private void cancelBooking(Booking booking) {
        // Validate booking status
        if (!"pending".equals(booking.getStatus())) {
            Toast.makeText(this, "Only pending bookings can be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        // ========== CONFIRMATION DIALOG FOR CANCEL ACTION ==========
        // Show confirmation dialog before canceling to prevent accidental cancellations
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage(Constants.MSG_CONFIRM_CANCEL)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed - proceed to booking details
                        Intent intent = new Intent(MyBookingsActivity.this, BookingDetailsActivity.class);
                        intent.putExtra("bookingID", booking.getBookingID());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled - do nothing
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Helper: call getAllBookings and filter by userID
    private void performAllBookingsFallback(String token, int userID) {
        bookingService.getAllBookings(token).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> callAll, Response<List<Booking>> responseAll) {
                if (responseAll.code() == 200 && responseAll.body() != null) {
                    List<Booking> all = responseAll.body();
                    java.util.List<Booking> filtered = new java.util.ArrayList<>();
                    for (Booking b : all) {
                        try {
                            if (b.getUserID() == userID) filtered.add(b);
                        } catch (Exception e) {
                            Log.w("MyBookings", "Error reading booking userID: " + e.getMessage());
                        }
                    }

                    Log.d("MyBookings", "getAllBookings returned " + all.size() + " items; filtered=" + filtered.size());

                    if (!filtered.isEmpty()) {
                        adapter = new BookingAdapter(filtered, getApplicationContext());
                        rvMyBookings.setAdapter(adapter);
                        tvEmptyState.setVisibility(View.GONE);
                        rvMyBookings.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else if (responseAll.code() == 204) {
                    Log.w("MyBookings", "getAllBookings returned 204 No Content; no bookings available on server");
                    showEmptyState();
                } else if (responseAll.code() == 401) {
                    Toast.makeText(getApplicationContext(), Constants.MSG_SESSION_EXPIRED, Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                } else {
                    Log.e("MyBookings", "getAllBookings error: " + responseAll.message());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> callAll, Throwable t) {
                Log.e("MyBookings", "getAllBookings failure: " + (t == null ? "unknown" : t.getMessage()));
                showEmptyState();
            }
        });
    }
}
