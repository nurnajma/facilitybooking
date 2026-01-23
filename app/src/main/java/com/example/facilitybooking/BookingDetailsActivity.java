package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.remote.UserService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView tvBookingID, tvStatus, tvFacilityName, tvDate, tvTime, tvPurpose, tvCost, tvAdminNotes, tvUser, tvAdminNotesLabel;
    private CardView cardAdminNotes;
    private LinearLayout layoutActions, layoutAdminActions;
    private Button btnEditBooking, btnCancelBooking, btnApproveBooking, btnRejectBooking;
    private Booking booking;
    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        spm = new SharedPrefManager(getApplicationContext());

        tvBookingID = findViewById(R.id.tvBookingID);
        tvStatus = findViewById(R.id.tvStatus);
        tvUser = findViewById(R.id.tvUser);
        tvFacilityName = findViewById(R.id.tvFacilityName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvPurpose = findViewById(R.id.tvPurpose);
        tvCost = findViewById(R.id.tvCost);
        tvAdminNotes = findViewById(R.id.tvAdminNotes);
        tvAdminNotesLabel = findViewById(R.id.tvAdminNotesLabel);
        cardAdminNotes = findViewById(R.id.cardAdminNotes);
        
        layoutActions = findViewById(R.id.layoutActions);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        
        btnEditBooking = findViewById(R.id.btnEditBooking);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnApproveBooking = findViewById(R.id.btnApproveBooking);
        btnRejectBooking = findViewById(R.id.btnRejectBooking);

        int bookingID = getIntent().getIntExtra("bookingID", -1);

        if (bookingID != -1) {
            loadBookingDetails(bookingID);
        } else {
            Toast.makeText(this, "Error loading booking", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEditBooking.setOnClickListener(v -> editBooking());
        btnCancelBooking.setOnClickListener(v -> cancelBooking());
        btnApproveBooking.setOnClickListener(v -> approveBooking());
        btnRejectBooking.setOnClickListener(v -> rejectBooking());
    }

    private void loadBookingDetails(int bookingID) {
        User user = spm.getUser();
        BookingService bookingService = ApiUtils.getBookingService();
        Call<Booking> call = bookingService.getBooking(user.getToken(), bookingID);

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.code() == 200) {
                    booking = response.body();
                    displayBookingDetails();
                } else {
                    Toast.makeText(BookingDetailsActivity.this, Constants.MSG_GENERIC_ERROR, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(BookingDetailsActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookingDetails() {
        tvBookingID.setText("#" + booking.getBookingID());

        // Resolve Facility Name
        if (booking.getFacility() != null && booking.getFacility().getFacilityName() != null) {
            tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            tvFacilityName.setText("Loading...");
            ApiUtils.getFacilityService().getFacility(spm.getUser().getToken(), booking.getFacilityID()).enqueue(new Callback<Facility>() {
                @Override
                public void onResponse(Call<Facility> call, Response<Facility> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvFacilityName.setText(response.body().getFacilityName());
                    } else {
                        tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
                    }
                }
                @Override
                public void onFailure(Call<Facility> call, Throwable t) {
                    tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
                }
            });
        }

        // Resolve User Name
        tvUser.setText("Loading...");
        ApiUtils.getUserService().getUser(spm.getUser().getToken(), booking.getUserID()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvUser.setText(response.body().getUsername());
                } else {
                    tvUser.setText("User ID: " + booking.getUserID());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvUser.setText("User ID: " + booking.getUserID());
            }
        });

        tvDate.setText(booking.getBookingDate());
        String start = booking.getStartTime() != null && booking.getStartTime().length() >= 5 ? booking.getStartTime().substring(0, 5) : "00:00";
        String end = booking.getEndTime() != null && booking.getEndTime().length() >= 5 ? booking.getEndTime().substring(0, 5) : "00:00";
        tvTime.setText(start + " - " + end);
        
        tvPurpose.setText(booking.getPurpose());
        tvCost.setText(String.format(Locale.getDefault(), "RM %.2f", booking.getTotalCost()));

        String status = Constants.normalizeStatus(booking.getStatus());
        tvStatus.setText(status.toUpperCase());
        tvStatus.setBackgroundColor(Constants.getStatusColor(status));

        // Logic to show/hide action buttons
        boolean isAdmin = spm.isAdmin();
        boolean isPending = Constants.STATUS_PENDING.equals(status);

        if (isAdmin) {
            layoutActions.setVisibility(View.GONE);
            layoutAdminActions.setVisibility(isPending ? View.VISIBLE : View.GONE);
        } else {
            layoutAdminActions.setVisibility(View.GONE);
            layoutActions.setVisibility(isPending ? View.VISIBLE : View.GONE);
        }

        // Show admin notes if available
        if (booking.getAdminNotes() != null && !booking.getAdminNotes().isEmpty()) {
            cardAdminNotes.setVisibility(View.VISIBLE);
            tvAdminNotes.setText(booking.getAdminNotes());
            if (Constants.STATUS_REJECTED.equals(status)) {
                tvAdminNotesLabel.setText("❌ Rejection Reason");
                cardAdminNotes.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
            } else {
                tvAdminNotesLabel.setText("📝 Admin Notes");
                cardAdminNotes.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
            }
        } else {
            cardAdminNotes.setVisibility(View.GONE);
        }
    }

    private void editBooking() {
        Intent intent = new Intent(this, EditBookingActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        startActivity(intent);
        finish();
    }

    private void cancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage(Constants.MSG_CONFIRM_CANCEL)
                .setPositiveButton("Yes", (dialog, which) -> updateBookingStatus(Constants.STATUS_CANCELLED, ""))
                .setNegativeButton("No", null)
                .show();
    }

    private void approveBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Approve Booking")
                .setMessage("Are you sure you want to approve this booking?")
                .setPositiveButton("Approve", (dialog, which) -> updateBookingStatus(Constants.STATUS_APPROVED, ""))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rejectBooking() {
        final EditText input = new EditText(this);
        input.setHint("Reason for rejection");
        
        new AlertDialog.Builder(this)
                .setTitle("Reject Booking")
                .setMessage("Please provide a reason for rejection:")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    updateBookingStatus(Constants.STATUS_REJECTED, reason.isEmpty() ? "No reason provided" : reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBookingStatus(String newStatus, String adminNotes) {
        User user = spm.getUser();
        BookingService bookingService = ApiUtils.getBookingService();
        
        Call<Booking> call = bookingService.updateBooking(
                user.getToken(),
                booking.getBookingID(),
                booking.getUserID(),
                booking.getFacilityID(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                newStatus,
                adminNotes,
                booking.getTotalCost()
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    String msg = "";
                    if (Constants.STATUS_APPROVED.equals(newStatus)) msg = Constants.MSG_BOOKING_APPROVED;
                    else if (Constants.STATUS_REJECTED.equals(newStatus)) msg = Constants.MSG_BOOKING_REJECTED;
                    else if (Constants.STATUS_CANCELLED.equals(newStatus)) msg = Constants.MSG_BOOKING_CANCELLED;
                    
                    Toast.makeText(BookingDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BookingDetailsActivity.this, Constants.MSG_GENERIC_ERROR, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(BookingDetailsActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
