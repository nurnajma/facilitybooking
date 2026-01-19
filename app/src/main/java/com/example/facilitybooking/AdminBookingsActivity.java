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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.adapters.AdminBookingAdapter;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingsActivity extends AppCompatActivity {

    private RecyclerView rvAdminBookings;
    private TextView tvEmptyState;
    private Button btnFilterAll, btnFilterPending, btnFilterApproved, btnFilterRejected;
    private AdminBookingAdapter adapter;
    private BookingService bookingService;
    private List<Booking> allBookings = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        rvAdminBookings = findViewById(R.id.rvAdminBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterApproved = findViewById(R.id.btnFilterApproved);
        btnFilterRejected = findViewById(R.id.btnFilterRejected);

        rvAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvAdminBookings);

        // Filter buttons
        btnFilterAll.setOnClickListener(v -> filterBookings("all"));
        btnFilterPending.setOnClickListener(v -> filterBookings("pending"));
        btnFilterApproved.setOnClickListener(v -> filterBookings("approved"));
        btnFilterRejected.setOnClickListener(v -> filterBookings("rejected"));

        loadAllBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllBookings();
    }

    private void loadAllBookings() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        bookingService = ApiUtils.getBookingService();
        Call<List<Booking>> call = bookingService.getAllBookings(token);

        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                Log.d("AdminBookings", "Response: " + response.code());

                if (response.code() == 200) {
                    allBookings = response.body();
                    if (allBookings != null && !allBookings.isEmpty()) {
                        filterBookings(currentFilter);
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
                Log.e("AdminBookings", "Error: " + t.getMessage());
            }
        });
    }

    private void filterBookings(String filter) {
        currentFilter = filter;
        List<Booking> filteredList = new ArrayList<>();

        if ("all".equals(filter)) {
            filteredList = allBookings;
        } else {
            for (Booking booking : allBookings) {
                if (filter.equalsIgnoreCase(booking.getStatus())) {
                    filteredList.add(booking);
                }
            }
        }

        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            if (adapter == null) {
                adapter = new AdminBookingAdapter(filteredList, getApplicationContext());
                rvAdminBookings.setAdapter(adapter);
            } else {
                adapter.updateList(filteredList);
            }
            tvEmptyState.setVisibility(View.GONE);
            rvAdminBookings.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvAdminBookings.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_booking_context_menu, menu);

        Booking selectedBooking = adapter.getSelectedItem();
        if (selectedBooking != null) {
            String status = selectedBooking.getStatus();
            if (!"pending".equals(status)) {
                menu.findItem(R.id.menu_approve_booking).setVisible(false);
                menu.findItem(R.id.menu_reject_booking).setVisible(false);
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

        if (itemId == R.id.menu_approve_booking) {
            approveBooking(selectedBooking);
        } else if (itemId == R.id.menu_reject_booking) {
            rejectBooking(selectedBooking);
        } else if (itemId == R.id.menu_delete_booking) {
            deleteBooking(selectedBooking);
        }

        return super.onContextItemSelected(item);
    }

    private void approveBooking(Booking booking) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String updatedAt = sdf.format(new Date());

        bookingService = ApiUtils.getBookingService();
        Call<Booking> call = bookingService.updateBooking(
                user.getToken(),
                booking.getBookingID(),
                booking.getUserID(),
                booking.getFacilityID(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                "approved",
                "",
                booking.getTotalCost()
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.code() == 200) {
                    Toast.makeText(AdminBookingsActivity.this, "Booking approved!", Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(AdminBookingsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectBooking(Booking booking) {
        // Show dialog to get rejection reason
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reject Booking");

        final EditText input = new EditText(this);
        input.setHint("Enter rejection reason");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);

        builder.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reason = input.getText().toString().trim();
                if (reason.isEmpty()) {
                    reason = "No reason provided";
                }
                performReject(booking, reason);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void performReject(Booking booking, String reason) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        bookingService = ApiUtils.getBookingService();
        Call<Booking> call = bookingService.updateBooking(
                user.getToken(),
                booking.getBookingID(),
                booking.getUserID(),
                booking.getFacilityID(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                "rejected",
                reason,
                booking.getTotalCost()
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.code() == 200) {
                    Toast.makeText(AdminBookingsActivity.this, "Booking rejected!", Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(AdminBookingsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBooking(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Booking");
        builder.setMessage("Are you sure you want to delete this booking?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDelete(booking);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void performDelete(Booking booking) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        bookingService = ApiUtils.getBookingService();
        Call<DeleteResponse> call = bookingService.deleteBooking(user.getToken(), booking.getBookingID());

        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(AdminBookingsActivity.this, "Booking deleted!", Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(AdminBookingsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}