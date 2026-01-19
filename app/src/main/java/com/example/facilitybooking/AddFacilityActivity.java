package com.example.facilitybooking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFacilityActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText edtFacilityName, edtDescription, edtCapacity, edtHourlyRate, edtLocation;
    private Spinner spinnerStatus;
    private Button btnCancel, btnSave;

    private boolean isEdit = false;
    private int facilityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_facility);

        // Get references
        tvTitle = findViewById(R.id.tvTitle);
        edtFacilityName = findViewById(R.id.edtFacilityName);
        edtDescription = findViewById(R.id.edtDescription);
        edtCapacity = findViewById(R.id.edtCapacity);
        edtHourlyRate = findViewById(R.id.edtHourlyRate);
        edtLocation = findViewById(R.id.edtLocation);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Setup status spinner
        String[] statuses = {"available", "maintenance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Check if editing
        if (getIntent().hasExtra("isEdit")) {
            isEdit = getIntent().getBooleanExtra("isEdit", false);
            if (isEdit) {
                tvTitle.setText("Edit Facility");
                btnSave.setText("Update Facility");
                loadFacilityData();
            }
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    updateFacility();
                } else {
                    addFacility();
                }
            }
        });
    }

    private void loadFacilityData() {
        facilityID = getIntent().getIntExtra("facilityID", -1);
        edtFacilityName.setText(getIntent().getStringExtra("facilityName"));
        edtDescription.setText(getIntent().getStringExtra("description"));
        edtCapacity.setText(String.valueOf(getIntent().getIntExtra("capacity", 0)));
        edtHourlyRate.setText(String.valueOf(getIntent().getDoubleExtra("hourlyRate", 0.0)));
        edtLocation.setText(getIntent().getStringExtra("location"));

        String status = getIntent().getStringExtra("status");
        if ("maintenance".equals(status)) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }
    }

    private void addFacility() {
        if (!validateForm()) {
            return;
        }

        String name = edtFacilityName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        int capacity = Integer.parseInt(edtCapacity.getText().toString().trim());
        double hourlyRate = Double.parseDouble(edtHourlyRate.getText().toString().trim());
        String location = edtLocation.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        FacilityService facilityService = ApiUtils.getFacilityService();
        Call<Facility> call = facilityService.addFacility(user.getToken(), name, description, capacity, hourlyRate, location, status);

        call.enqueue(new Callback<Facility>() {
            @Override
            public void onResponse(Call<Facility> call, Response<Facility> response) {
                if (response.code() == 201 || response.code() == 200) {
                    Toast.makeText(AddFacilityActivity.this, "Facility added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddFacilityActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Facility> call, Throwable t) {
                Toast.makeText(AddFacilityActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("AddFacility", "Error: " + t.getMessage());
            }
        });
    }

    private void updateFacility() {
        if (!validateForm()) {
            return;
        }

        String name = edtFacilityName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        int capacity = Integer.parseInt(edtCapacity.getText().toString().trim());
        double hourlyRate = Double.parseDouble(edtHourlyRate.getText().toString().trim());
        String location = edtLocation.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        FacilityService facilityService = ApiUtils.getFacilityService();
        Call<Facility> call = facilityService.updateFacility(user.getToken(), facilityID, name, description, capacity, hourlyRate, location, status);

        call.enqueue(new Callback<Facility>() {
            @Override
            public void onResponse(Call<Facility> call, Response<Facility> response) {
                if (response.code() == 200) {
                    Toast.makeText(AddFacilityActivity.this, "Facility updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddFacilityActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Facility> call, Throwable t) {
                Toast.makeText(AddFacilityActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("UpdateFacility", "Error: " + t.getMessage());
            }
        });
    }

    private boolean validateForm() {
        if (edtFacilityName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter facility name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtCapacity.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter capacity", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtHourlyRate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter hourly rate", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtLocation.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}