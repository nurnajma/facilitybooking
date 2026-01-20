package com.example.facilitybooking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditBookingActivity extends AppCompatActivity {

    private TextView tvFacilityName, tvFacilityInfo, tvSelectedDate, tvStartTime, tvEndTime, tvDuration, tvTotalCost;
    private EditText edtPurpose;
    private Button btnPickDate, btnPickStartTime, btnPickEndTime, btnCancel, btnSubmitBooking;

    private int bookingID;
    private Facility facility;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private double hourlyRate = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        // Get references
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

        // Change button text
        btnSubmitBooking.setText("Update Booking");

        // Load existing booking data
        loadBookingData();

        // Date picker
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Start time picker
        btnPickStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(true);
            }
        });

        // End time picker
        btnPickEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(false);
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Update button
        btnSubmitBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBooking();
            }
        });
    }

    private void loadBookingData() {
        bookingID = getIntent().getIntExtra("bookingID", -1);
        int facilityID = getIntent().getIntExtra("facilityID", -1);
        String facilityName = getIntent().getStringExtra("facilityName");
        String bookingDate = getIntent().getStringExtra("bookingDate");
        String startTimeStr = getIntent().getStringExtra("startTime");
        String endTimeStr = getIntent().getStringExtra("endTime");
        String purpose = getIntent().getStringExtra("purpose");
        double totalCost = getIntent().getDoubleExtra("totalCost", 0.0);

        // Create facility object
        facility = new Facility();
        facility.setFacilityID(facilityID);
        facility.setFacilityName(facilityName);

        // Display facility info
        tvFacilityName.setText(facilityName);
        tvFacilityInfo.setText("Editing existing booking");

        // Parse and set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(bookingDate);
            selectedDate.setTime(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.getDefault());
            tvSelectedDate.setText(displayFormat.format(selectedDate.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Parse and set start time
        String[] startParts = startTimeStr.split(":");
        startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
        startTime.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));
        tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1])));

        // Parse and set end time
        String[] endParts = endTimeStr.split(":");
        endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
        endTime.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));
        tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1])));

        // Set purpose
        edtPurpose.setText(purpose);

        // Calculate and display cost
        long diffInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        double hours = diffInMillis / (1000.0 * 60 * 60);
        hourlyRate = totalCost / hours;

        tvDuration.setText(String.format(Locale.getDefault(), "%.1f hours", hours));
        tvTotalCost.setText(String.format(Locale.getDefault(), "RM %.2f", totalCost));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.getDefault());
                        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
                        calculateCost();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar time = isStartTime ? startTime : endTime;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (isStartTime) {
                            startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startTime.set(Calendar.MINUTE, minute);
                            tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        } else {
                            endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            endTime.set(Calendar.MINUTE, minute);
                            tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        }
                        calculateCost();
                    }
                },
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void calculateCost() {
        if (tvStartTime.getText().toString().equals("Select time") ||
                tvEndTime.getText().toString().equals("Select time")) {
            return;
        }

        long diffInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        if (diffInMillis <= 0) {
            tvDuration.setText("Invalid duration");
            tvTotalCost.setText("RM 0.00");
            return;
        }

        double hours = diffInMillis / (1000.0 * 60 * 60);
        double totalCost = hours * hourlyRate;

        tvDuration.setText(String.format(Locale.getDefault(), "%.1f hours", hours));
        tvTotalCost.setText(String.format(Locale.getDefault(), "RM %.2f", totalCost));
    }

    private void updateBooking() {
        String purpose = edtPurpose.getText().toString().trim();

        if (tvSelectedDate.getText().toString().equals("Select date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tvStartTime.getText().toString().equals("Select time")) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tvEndTime.getText().toString().equals("Select time")) {
            Toast.makeText(this, "Please select end time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (purpose.isEmpty()) {
            Toast.makeText(this, "Please enter purpose", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date and times
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String bookingDate = dateFormat.format(selectedDate.getTime());
        String startTimeStr = timeFormat.format(startTime.getTime());
        String endTimeStr = timeFormat.format(endTime.getTime());

        // Calculate total cost
        long diffInMillis = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        double hours = diffInMillis / (1000.0 * 60 * 60);
        double totalCost = hours * hourlyRate;

        // Get user
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // Update booking
        BookingService bookingService = ApiUtils.getBookingService();
        Call<Booking> call = bookingService.updateBooking(
                user.getToken(),
                bookingID,
                user.getId(),
                facility.getFacilityID(),
                bookingDate,
                startTimeStr,
                endTimeStr,
                purpose,
                "pending",
                "",
                totalCost
        );

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.code() == 200) {
                    Toast.makeText(EditBookingActivity.this,
                            "Booking updated successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(EditBookingActivity.this,
                            "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(EditBookingActivity.this,
                        "Error connecting to server", Toast.LENGTH_LONG).show();
                Log.e("EditBooking", "Error: " + t.getMessage());
            }
        });
    }
}
