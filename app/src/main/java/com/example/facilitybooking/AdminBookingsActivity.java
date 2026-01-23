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
import android.widget.ProgressBar;
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
import com.example.facilitybooking.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private ProgressBar progressBar; // Loading indicator for API calls
    private Button btnFilterAll, btnFilterPending, btnFilterApproved, btnFilterRejected;
    private AdminBookingAdapter adapter;
    private BookingService bookingService;
    private List<Booking> allBookings = new ArrayList<>();
    private String currentFilter = "all";
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        rvAdminBookings = findViewById(R.id.rvAdminBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar); // Initialize loading indicator
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
        try { if (findViewById(R.id.bottomNavigation) != null) findViewById(R.id.bottomNavigation).post(() -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bn = findViewById(R.id.bottomNavigation);
            if (bn != null) bn.setSelectedItemId(R.id.nav_bookings);
        }); } catch (Exception ignored) {}
        loadAllBookings();
    }

    private void loadAllBookings() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        rvAdminBookings.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        bookingService = ApiUtils.getBookingService();
        Call<List<Booking>> call = bookingService.getAllBookings(token);

        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                Log.d("AdminBookings", "Response: " + response.code());

                if (response.code() == 200) {
                    allBookings = response.body();
                    if (allBookings != null && !allBookings.isEmpty()) {
                        filterBookings(currentFilter);
                    } else {
                        showEmptyState();
                    }
                    // Initialize bottom nav selection for admin
                    try {
                        bottomNavigation = findViewById(R.id.bottomNavigation);
                        bottomNavigation.setOnItemSelectedListener(item -> {
                            int id = item.getItemId();
                            if (id == R.id.nav_home) {
                                Intent intent = new Intent(AdminBookingsActivity.this, AdminDashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                return true;
                            } else if (id == R.id.nav_bookings) {
                                // already here
                                return true;
                            } else if (id == R.id.nav_profile) {
                                Intent intent = new Intent(AdminBookingsActivity.this, ProfileActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                return true;
                            }
                            return false;
                        });
                        bottomNavigation.setSelectedItemId(R.id.nav_bookings);
                    } catch (Exception ignored) {}
                 } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), Constants.MSG_SESSION_EXPIRED, Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                } else {
                    Toast.makeText(getApplicationContext(), Constants.MSG_GENERIC_ERROR, Toast.LENGTH_LONG).show();
                    Log.e("AdminBookings", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                
                Toast.makeText(getApplicationContext(), Constants.MSG_NETWORK_ERROR, Toast.LENGTH_LONG).show();
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
                adapter = new AdminBookingAdapter(filteredList, getApplicationContext(), new com.example.facilitybooking.adapters.AdminBookingAdapter.AdminActionListener() {
                    @Override
                    public void onApprove(Booking booking) {
                        approveBooking(booking);
                    }

                    @Override
                    public void onReject(Booking booking) {
                        rejectBooking(booking);
                    }
                });
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
        // Show dialog to optionally capture an admin note before approval
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Approve Booking");

        final EditText input = new EditText(this);
        input.setHint("Optional note (e.g. additional instructions)");
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);

        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String note = input.getText().toString().trim();
                performApprove(booking, note);
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

    // Actual network call to approve booking. Separated so we can show confirmation/note first.
    private void performApprove(Booking booking, String adminNote) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // Disable actions for this booking while request is in-flight
        if (adapter != null) adapter.setProcessing(booking.getBookingID(), true);

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
                adminNote == null ? "" : adminNote,
                booking.getTotalCost()
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                // Clear processing state
                if (adapter != null) adapter.setProcessing(booking.getBookingID(), false);
                if (response.code() == 200) {
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_BOOKING_APPROVED, Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_GENERIC_ERROR, Toast.LENGTH_SHORT).show();
                    Log.e("AdminBookings", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (adapter != null) adapter.setProcessing(booking.getBookingID(), false);
                Toast.makeText(AdminBookingsActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
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

        // Disable actions for this booking while request is in-flight
        if (adapter != null) adapter.setProcessing(booking.getBookingID(), true);

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
                if (adapter != null) adapter.setProcessing(booking.getBookingID(), false);
                if (response.code() == 200) {
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_BOOKING_REJECTED, Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_GENERIC_ERROR, Toast.LENGTH_SHORT).show();
                    Log.e("AdminBookings", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (adapter != null) adapter.setProcessing(booking.getBookingID(), false);
                Toast.makeText(AdminBookingsActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== CONFIRMATION DIALOG FOR DELETE ACTION ==========
    private void deleteBooking(Booking booking) {
        // Show confirmation dialog before deleting to prevent accidental deletions
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Booking");
        builder.setMessage(Constants.MSG_CONFIRM_DELETE);

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
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_BOOKING_DELETED, Toast.LENGTH_SHORT).show();
                    loadAllBookings();
                } else {
                    Toast.makeText(AdminBookingsActivity.this, Constants.MSG_GENERIC_ERROR, Toast.LENGTH_SHORT).show();
                    Log.e("AdminBookings", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(AdminBookingsActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
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









