package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvMyBookings;
    private TextView tvEmptyState;
    private BookingAdapter adapter;
    private BookingService bookingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvMyBookings = findViewById(R.id.rvMyBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvMyBookings);

        loadMyBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyBookings(); // Refresh when returning to this activity
    }

    private void loadMyBookings() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();
        int userID = user.getId();

        bookingService = ApiUtils.getBookingService();
        Call<List<Booking>> call = bookingService.getUserBookings(token, userID);

        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                Log.d("MyBookings", "Response: " + response.code());

                if (response.code() == 200) {
                    List<Booking> bookings = response.body();
                    if (bookings != null && !bookings.isEmpty()) {
                        adapter = new BookingAdapter(bookings, getApplicationContext());
                        rvMyBookings.setAdapter(adapter);
                        tvEmptyState.setVisibility(View.GONE);
                        rvMyBookings.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error connecting to server", Toast.LENGTH_LONG).show();
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
        if (!"pending".equals(booking.getStatus())) {
            Toast.makeText(this, "Only pending bookings can be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, BookingDetailsActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        startActivity(intent);
    }


    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}