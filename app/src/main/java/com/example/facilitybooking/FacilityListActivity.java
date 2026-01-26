package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facilitybooking.adapters.FacilityAdapter;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacilityListActivity extends AppCompatActivity {

    private RecyclerView rvFacilityList;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddFacility;
    private FacilityAdapter adapter;
    private FacilityService facilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_list);

        rvFacilityList = findViewById(R.id.rvFacilityList);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddFacility = findViewById(R.id.fabAddFacility);

        rvFacilityList.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvFacilityList);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (spm.isAdmin()) {
            fabAddFacility.setVisibility(View.VISIBLE);
            fabAddFacility.setOnClickListener(v -> {
                Intent intent = new Intent(FacilityListActivity.this, AddFacilityActivity.class);
                startActivity(intent);
            });
        } else {
            fabAddFacility.setVisibility(View.GONE);
        }

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
                        adapter = new FacilityAdapter(FacilityListActivity.this, true);
                        adapter.submitList(facilities);
                        rvFacilityList.setAdapter(adapter);
                        tvEmptyState.setVisibility(View.GONE);
                        rvFacilityList.setVisibility(View.VISIBLE);
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
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvFacilityList.setVisibility(View.GONE);
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
