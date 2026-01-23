package com.example.facilitybooking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.BookingService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.BookingValidationUtils;
import com.example.facilitybooking.utils.Constants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingActivity extends AppCompatActivity {

    private TextView tvFacilityName, tvFacilityInfo, tvSelectedDate, tvStartTime, tvEndTime, tvDuration, tvTotalCost;
    private EditText edtPurpose;
    private Button btnPickDate, btnPickStartTime, btnPickEndTime, btnCancel, btnSubmitBooking;
    private ProgressBar progressBar;

    private Facility facility;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        // UI References
        tvFacilityName = findViewById(R.id.tvFacilityName);
        tvFacilityInfo = findViewById(R.id.tvFacilityInfo);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        edtPurpose = findViewById(R.id.edtPurpose);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickStartTime = findViewById(R.id.btnPickStartTime);
        btnPickEndTime = findViewById(R.id.btnPickEndTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnSubmitBooking = findViewById(R.id.btnSubmitBooking);

        // Dynamic lookup for optional progressBar (prevents resource mismatch in analyzer)
        int pbId = getResources().getIdentifier("progressBar", "id", getPackageName());
        if (pbId != 0) {
            progressBar = findViewById(pbId);
        } else {
            progressBar = null;
        }

        // Initial UI state
        if (tvSelectedDate != null) tvSelectedDate.setText("Select date");
        if (tvStartTime != null) tvStartTime.setText("Select time");
        if (tvEndTime != null) tvEndTime.setText("Select time");

        // Facility data from intent
        Intent intent = getIntent();
        facility = new Facility();
        facility.setFacilityID(intent.getIntExtra("facilityID", -1));
        facility.setFacilityName(intent.getStringExtra("facilityName"));
        facility.setCapacity(intent.getIntExtra("capacity", 0));
        facility.setHourlyRate(intent.getDoubleExtra("hourlyRate", 0.0));

        tvFacilityName.setText(facility.getFacilityName());
        tvFacilityInfo.setText("Capacity: " + facility.getCapacity() + " | RM " + String.format("%.2f", facility.getHourlyRate()) + "/hr");

        // Button listeners
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickStartTime.setOnClickListener(v -> showTimePicker(true));
        btnPickEndTime.setOnClickListener(v -> showTimePicker(false));
        btnCancel.setOnClickListener(v -> finish());
        btnSubmitBooking.setOnClickListener(v -> checkAvailabilityAndSubmit());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            tvSelectedDate.setText(new SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.getDefault()).format(selectedDate.getTime()));
            calculateCost();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar time = isStartTime ? startTime : endTime;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (isStartTime) {
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startTime.set(Calendar.MINUTE, minute);
                startTime.set(Calendar.SECOND, 0);
                tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            } else {
                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endTime.set(Calendar.MINUTE, minute);
                endTime.set(Calendar.SECOND, 0);
                tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
            calculateCost();
        }, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void calculateCost() {
        if (tvStartTime.getText().equals("Select time") || tvEndTime.getText().equals("Select time")) return;

        long diff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        if (diff <= 0) {
            tvDuration.setText("Invalid duration");
            tvTotalCost.setText("RM 0.00");
            return;
        }

        double hours = diff / (1000.0 * 60 * 60);
        double totalCost = hours * facility.getHourlyRate();
        tvDuration.setText(String.format(Locale.getDefault(), "%.1f hours", hours));
        tvTotalCost.setText(String.format(Locale.getDefault(), "RM %.2f", totalCost));
    }

    private void checkAvailabilityAndSubmit() {
        // 1. Basic Local Validation
        if (tvSelectedDate.getText().equals("Select date")) {
            Toast.makeText(this, Constants.MSG_SELECT_DATE, Toast.LENGTH_SHORT).show();
            return;
        }
        if (tvStartTime.getText().equals("Select time") || tvEndTime.getText().equals("Select time")) {
            Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String startStr = timeFormat.format(startTime.getTime());
        String endStr = timeFormat.format(endTime.getTime());
        
        if (!BookingValidationUtils.isValidTimeRange(startStr, endStr)) {
            Toast.makeText(this, Constants.MSG_INVALID_TIME_RANGE, Toast.LENGTH_SHORT).show();
            return;
        }

        String purpose = edtPurpose.getText().toString().trim();
        if (purpose.isEmpty()) {
            Toast.makeText(this, Constants.MSG_ENTER_PURPOSE, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Fetch all bookings to check for overlap
        setProgressVisible(true);
        btnSubmitBooking.setEnabled(false);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String dateStr = dateFormat.format(selectedDate.getTime());

        BookingService bookingService = ApiUtils.getBookingService();
        // We get all bookings to perform a comprehensive overlap check
        bookingService.getAllBookings(user.getToken()).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 3. Overlap check logic
                    String overlapError = BookingValidationUtils.checkOverlap(
                            dateStr, startStr, endStr, response.body(), facility.getFacilityID()
                    );

                    if (overlapError != null) {
                        setProgressVisible(false);
                        btnSubmitBooking.setEnabled(true);
                        Toast.makeText(CreateBookingActivity.this, overlapError, Toast.LENGTH_LONG).show();
                    } else {
                        // 4. No overlap - Proceed to create
                        performBookingCreation(user, dateStr, startStr, endStr, purpose);
                    }
                } else {
                    setProgressVisible(false);
                    btnSubmitBooking.setEnabled(true);
                    Toast.makeText(CreateBookingActivity.this, "Failed to verify availability", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                setProgressVisible(false);
                btnSubmitBooking.setEnabled(true);
                String msg = (t == null || t.getMessage() == null) ? "Unable to connect to server. Please check your internet connection." : ("Network error: " + t.getMessage());
                Log.e("CreateBooking", "getAllBookings failure: " + (t == null ? "unknown" : t.getMessage()));
                Toast.makeText(CreateBookingActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performBookingCreation(User user, String date, String start, String end, String purpose) {
        long diff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        double hours = diff / (1000.0 * 60 * 60);
        double totalCost = hours * facility.getHourlyRate();

        BookingService bookingService = ApiUtils.getBookingService();
        bookingService.createBooking(
                user.getToken(),
                user.getId(),
                facility.getFacilityID(),
                date,
                start,
                end,
                purpose,
                Constants.STATUS_PENDING,
                totalCost
        ).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                setProgressVisible(false);
                btnSubmitBooking.setEnabled(true);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Booking created = response.body();
                        Log.d("CreateBooking", "Booking created successfully (form): " + created);
                    } else {
                        Log.i("CreateBooking", "Booking created (form) - empty response body, treating as success. Code=" + response.code());
                    }

                    Toast.makeText(CreateBookingActivity.this, Constants.MSG_BOOKING_CREATED, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CreateBookingActivity.this, MyBookingsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    // Log details and try JSON fallback
                    String errBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errBody = "(error reading errorBody) " + e.getMessage();
                    }

                    Log.w("CreateBooking", "Form submission failed. Code=" + response.code() + " msg=" + response.message() + " body=" + errBody);

                    // Prepare Booking object for JSON fallback
                    Booking fallback = new Booking();
                    fallback.setUserID(user.getId());
                    fallback.setFacilityID(facility.getFacilityID());
                    fallback.setBookingDate(date);
                    fallback.setStartTime(start);
                    fallback.setEndTime(end);
                    fallback.setPurpose(purpose);
                    fallback.setStatus(Constants.STATUS_PENDING);
                    fallback.setTotalCost(totalCost);

                    // Attempt JSON body POST
                    bookingService.createBookingJson(user.getToken(), fallback).enqueue(new Callback<Booking>() {
                        @Override
                        public void onResponse(Call<Booking> call2, Response<Booking> response2) {
                            if (response2.isSuccessful()) {
                                if (response2.body() != null) {
                                    Log.d("CreateBooking", "Booking created successfully (json): " + response2.body());
                                } else {
                                    Log.i("CreateBooking", "Booking created (json) - empty response body, treating as success. Code=" + response2.code());
                                }
                                Toast.makeText(CreateBookingActivity.this, Constants.MSG_BOOKING_CREATED, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CreateBookingActivity.this, MyBookingsActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String err = "";
                                try { if (response2.errorBody() != null) err = response2.errorBody().string(); } catch (Exception ex) { err = ex.getMessage(); }
                                Log.w("CreateBooking", "JSON fallback failed. Code=" + response2.code() + " msg=" + response2.message() + " body=" + err);

                                // Try explicit Map-based JSON body with exact keys
                                java.util.Map<String, Object> map = new java.util.HashMap<>();
                                map.put("userID", user.getId());
                                map.put("facilityID", facility.getFacilityID());
                                map.put("bookingDate", date);
                                map.put("startTime", start);
                                map.put("endTime", end);
                                map.put("purpose", purpose);
                                map.put("status", Constants.STATUS_PENDING);
                                map.put("totalCost", totalCost);

                                bookingService.createBookingJsonMap(user.getToken(), map).enqueue(new Callback<Booking>() {
                                    @Override
                                    public void onResponse(Call<Booking> call3, Response<Booking> response3) {
                                        if (response3.isSuccessful()) {
                                            if (response3.body() != null) {
                                                Log.d("CreateBooking", "Booking created successfully (jsonMap): " + response3.body());
                                            } else {
                                                Log.i("CreateBooking", "Booking created (jsonMap) - empty response body, treating as success. Code=" + response3.code());
                                            }
                                            Toast.makeText(CreateBookingActivity.this, Constants.MSG_BOOKING_CREATED, Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(CreateBookingActivity.this, MyBookingsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            String err2 = "";
                                            try { if (response3.errorBody() != null) err2 = response3.errorBody().string(); } catch (Exception ex) { err2 = ex.getMessage(); }
                                            Log.e("CreateBooking", "JSON map fallback failed. Code=" + response3.code() + " msg=" + response3.message() + " body=" + err2);
                                            Toast.makeText(CreateBookingActivity.this, "Server error (json map fallback): " + response3.code(), Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Booking> call3, Throwable t3) {
                                        Log.e("CreateBooking", "JSON map fallback network error: " + (t3 == null ? "unknown" : t3.getMessage()));
                                        Toast.makeText(CreateBookingActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Booking> call2, Throwable t2) {
                            Log.e("CreateBooking", "JSON fallback network error: " + (t2 == null ? "unknown" : t2.getMessage()));
                            Toast.makeText(CreateBookingActivity.this, Constants.MSG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                setProgressVisible(false);
                btnSubmitBooking.setEnabled(true);
                String msg = (t == null || t.getMessage() == null) ? "Unable to connect to server. Please check your internet connection." : ("Network error: " + t.getMessage());
                Log.e("CreateBooking", "createBooking failure: " + (t == null ? "unknown" : t.getMessage()));
                Toast.makeText(CreateBookingActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper to safely set progress visibility
    private void setProgressVisible(boolean visible) {
        if (progressBar != null) {
            progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
