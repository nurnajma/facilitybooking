package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView tvBookingID, tvStatus, tvFacilityName, tvDate, tvTime, tvPurpose, tvCost, tvAdminNotes;
    private CardView cardAdminNotes;
    private LinearLayout layoutActions;
    private Button btnEditBooking, btnCancelBooking;
    private Booking booking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        tvBookingID = findViewById(R.id.tvBookingID);
        tvStatus = findViewById(R.id.tvStatus);
        tvFacilityName = findViewById(R.id.tvFacilityName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvPurpose = findViewById(R.id.tvPurpose);
        tvCost = findViewById(R.id.tvCost);
        tvAdminNotes = findViewById(R.id.tvAdminNotes);
        cardAdminNotes = findViewById(R.id.cardAdminNotes);
        layoutActions = findViewById(R.id.layoutActions);
        btnEditBooking = findViewById(R.id.btnEditBooking);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);

        int bookingID = getIntent().getIntExtra("bookingID", -1);

        if (bookingID != -1) {
            loadBookingDetails(bookingID);
        } else {
            Toast.makeText(this, "Error loading booking", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEditBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBooking();
            }
        });

        btnCancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBooking();
            }
        });
    }

    private void loadBookingDetails(int bookingID) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
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
                    Toast.makeText(BookingDetailsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(BookingDetailsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("BookingDetails", "Error: " + t.getMessage());
            }
        });
    }

    private void displayBookingDetails() {
        tvBookingID.setText("#" + booking.getBookingID());

        if (booking.getFacility() != null) {
            tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
        }

        tvDate.setText(booking.getBookingDate());
        String time = booking.getStartTime().substring(0, 5) + " - " + booking.getEndTime().substring(0, 5);
        tvTime.setText(time);
        tvPurpose.setText(booking.getPurpose());
        tvCost.setText("RM " + String.format("%.2f", booking.getTotalCost()));

        String status = booking.getStatus().toUpperCase();
        tvStatus.setText(status);

        switch (status) {
            case "PENDING":
                tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                layoutActions.setVisibility(View.VISIBLE);
                break;
            case "APPROVED":
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                layoutActions.setVisibility(View.GONE);
                break;
            case "REJECTED":
                tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                layoutActions.setVisibility(View.GONE);
                if (booking.getAdminNotes() != null && !booking.getAdminNotes().isEmpty()) {
                    cardAdminNotes.setVisibility(View.VISIBLE);
                    tvAdminNotes.setText(booking.getAdminNotes());
                }
                break;
            default:
                tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
                layoutActions.setVisibility(View.GONE);
                break;
        }
    }

    private void editBooking() {
        Intent intent = new Intent(this, EditBookingActivity.class);
        intent.putExtra("bookingID", booking.getBookingID());
        intent.putExtra("facilityID", booking.getFacilityID());
        intent.putExtra("facilityName", booking.getFacility() != null ? booking.getFacility().getFacilityName() : "");
        intent.putExtra("bookingDate", booking.getBookingDate());
        intent.putExtra("startTime", booking.getStartTime());
        intent.putExtra("endTime", booking.getEndTime());
        intent.putExtra("purpose", booking.getPurpose());
        intent.putExtra("totalCost", booking.getTotalCost());
        startActivity(intent);
        finish();
    }

    private void cancelBooking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Booking");
        builder.setMessage("Are you sure you want to cancel this booking?");

        builder.setPositiveButton("Yes, Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performCancel();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void performCancel() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String updatedAt = sdf.format(new Date());

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
                "cancelled",
                "",
                booking.getTotalCost()
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.code() == 200) {
                    Toast.makeText(BookingDetailsActivity.this, "Booking cancelled successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BookingDetailsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(BookingDetailsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
