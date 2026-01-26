package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facilitybooking.adapters.FacilityAdapter;
import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageFacilitiesActivity extends AppCompatActivity {

    private RecyclerView rvManageFacilities;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddFacility;
    private FacilityAdapter adapter;
    private FacilityService facilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_facilities);

        rvManageFacilities = findViewById(R.id.rvManageFacilities);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddFacility = findViewById(R.id.fabAddFacility);

        rvManageFacilities.setLayoutManager(new LinearLayoutManager(this));

        fabAddFacility.setOnClickListener(v -> {
            Intent intent = new Intent(ManageFacilitiesActivity.this, AddFacilityActivity.class);
            startActivityForResult(intent, 2001);
        });

        loadFacilities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFacilities();
    }

    private void loadFacilities() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        facilityService = ApiUtils.getFacilityService();
        Call<List<Facility>> call = facilityService.getAllFacilities(token);

        call.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                if (response.code() == 200) {
                    List<Facility> facilities = response.body();
                    if (facilities != null && !facilities.isEmpty()) {
                        // Initialize adapter for admin: show delete button, edit on click
                        adapter = new FacilityAdapter(ManageFacilitiesActivity.this, false);
                        adapter.submitList(facilities);
                        rvManageFacilities.setAdapter(adapter);

                        tvEmptyState.setVisibility(View.GONE);
                        rvManageFacilities.setVisibility(View.VISIBLE);
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
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error connecting to server", Toast.LENGTH_LONG).show();
                Log.e("ManageFacilities", "Error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvManageFacilities.setVisibility(View.GONE);
    }

    public void deleteFacility(Facility facility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Facility");
        builder.setMessage("Are you sure you want to delete " + facility.getFacilityName() + "?");

        builder.setPositiveButton("Delete", (dialog, which) -> performDelete(facility));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void performDelete(Facility facility) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        facilityService = ApiUtils.getFacilityService();
        Call<DeleteResponse> call = facilityService.deleteFacility(user.getToken(), facility.getFacilityID());

        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(ManageFacilitiesActivity.this, "Facility deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadFacilities();
                } else {
                    Toast.makeText(ManageFacilitiesActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(ManageFacilitiesActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            loadFacilities();
        }
    }
}
