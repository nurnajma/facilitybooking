package com.example.facilitybooking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacilityDetailsActivity extends AppCompatActivity {

    private TextView tvFacilityName, tvStatus, tvLocation, tvCapacity, tvHourlyRate, tvDescription;
    private Button btnBookNow;
    private ImageView ivFacilityHeader;
    private Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_details);

        tvFacilityName = findViewById(R.id.tvFacilityName);
        tvStatus = findViewById(R.id.tvStatus);
        tvLocation = findViewById(R.id.tvLocation);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvHourlyRate = findViewById(R.id.tvHourlyRate);
        tvDescription = findViewById(R.id.tvDescription);
        btnBookNow = findViewById(R.id.btnBookNow);
        ivFacilityHeader = findViewById(R.id.ivFacilityHeader);

        int facilityID = getIntent().getIntExtra("facilityID", -1);

        if (facilityID != -1) {
            loadFacilityDetails(facilityID);
        } else {
            Toast.makeText(this, "Error loading facility", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facility != null) {
                    // Check if facility is under maintenance - prevent booking
                    String facilityStatus = facility.getStatus() != null ? facility.getStatus().toLowerCase() : Constants.FACILITY_STATUS_AVAILABLE;
                    if (Constants.FACILITY_STATUS_MAINTENANCE.equalsIgnoreCase(facilityStatus)) {
                        Toast.makeText(FacilityDetailsActivity.this, Constants.MSG_FACILITY_MAINTENANCE, Toast.LENGTH_LONG).show();
                        return; // Don't proceed to booking
                    }
                    
                    // Facility is available - proceed to booking
                    Intent intent = new Intent(FacilityDetailsActivity.this, CreateBookingActivity.class);
                    intent.putExtra("facilityID", facility.getFacilityID());
                    intent.putExtra("facilityName", facility.getFacilityName());
                    intent.putExtra("capacity", facility.getCapacity());
                    intent.putExtra("hourlyRate", facility.getHourlyRate());
                    startActivity(intent);
                }
            }
        });
    }

    private void loadFacilityDetails(int facilityID) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        FacilityService facilityService = ApiUtils.getFacilityService();
        Call<Facility> call = facilityService.getFacility(user.getToken(), facilityID);

        call.enqueue(new Callback<Facility>() {
            @Override
            public void onResponse(Call<Facility> call, Response<Facility> response) {
                if (response.code() == 200) {
                    facility = response.body();
                    displayFacilityDetails();
                } else {
                    Toast.makeText(FacilityDetailsActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Facility> call, Throwable t) {
                Toast.makeText(FacilityDetailsActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("FacilityDetails", "Error: " + t.getMessage());
            }
        });
    }

    private void displayFacilityDetails() {
        tvFacilityName.setText(facility.getFacilityName());
        tvLocation.setText(facility.getLocation());
        tvCapacity.setText(facility.getCapacity() + " people");
        tvHourlyRate.setText("RM " + String.format("%.2f", facility.getHourlyRate()) + "/hour");
        tvDescription.setText(facility.getDescription());

        // Load image if available
        String imageUrl = facility.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivFacilityHeader);
        }

        String status = facility.getStatus() != null ? facility.getStatus().toUpperCase() : "AVAILABLE";
        String statusLower = facility.getStatus() != null ? facility.getStatus().toLowerCase() : Constants.FACILITY_STATUS_AVAILABLE;
        tvStatus.setText(status);

        boolean isMaintenance = Constants.FACILITY_STATUS_MAINTENANCE.equalsIgnoreCase(statusLower);

        if (Constants.FACILITY_STATUS_AVAILABLE.equalsIgnoreCase(statusLower)) {
            tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnBookNow.setEnabled(true);
            btnBookNow.setText("Book Now");
        } else if (isMaintenance) {
            tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
            btnBookNow.setEnabled(false);
            btnBookNow.setText("Under Maintenance");
        } else {
            // Other status (fallback)
            tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
            btnBookNow.setEnabled(false);
            btnBookNow.setText("FACILITY UNAVAILABLE");
        }
    }
}