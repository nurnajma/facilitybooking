package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facilitybooking.adapters.FacilityAdapter;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView rvFacilities;
    private View tvEmptyState;
    private EditText edtSearch;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    private FacilityAdapter adapter;
    private FacilityService facilityService;
    private List<Facility> allFacilities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        rvFacilities = findViewById(R.id.rvFacilities);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        edtSearch = findViewById(R.id.edtSearch);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvFacilities.setLayoutManager(new LinearLayoutManager(this));

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        User user = spm.getUser();
        tvWelcome.setText("Welcome, " + (user.getUsername() != null ? user.getUsername() : "") + "!");

        setupBottomNavigation();
        setupSearch();
        loadFacilities();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_my_bookings) {
                startActivity(new Intent(this, MyBookingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            android.os.Handler handler = new android.os.Handler();
            Runnable searchRunnable = UserDashboardActivity.this::applySearch;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void loadFacilities() {
        progressBar.setVisibility(View.VISIBLE);
        rvFacilities.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        facilityService = ApiUtils.getFacilityService();
        Call<List<Facility>> call = facilityService.getAllFacilities(token);
        call.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.code() == 200) {
                    List<Facility> facilities = response.body();
                    allFacilities = facilities != null ? facilities : new ArrayList<>();

                    if (adapter == null) {
                        adapter = new FacilityAdapter(UserDashboardActivity.this, true); // enable booking
                        rvFacilities.setAdapter(adapter);
                        adapter.submitList(allFacilities);
                    }
                    adapter.submitList(new ArrayList<>(allFacilities));

                    if (allFacilities.isEmpty()) showEmptyState();
                    else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvFacilities.setVisibility(View.VISIBLE);
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), Constants.MSG_SESSION_EXPIRED, Toast.LENGTH_LONG).show();
                    spm.logout();
                    finish();
                    startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), Constants.MSG_GENERIC_ERROR, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), Constants.MSG_NETWORK_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvFacilities.setVisibility(View.GONE);
    }

    private void applySearch() {
        String query = edtSearch.getText().toString().trim().toLowerCase();
        List<Facility> filtered = new ArrayList<>();
        for (Facility f : allFacilities) {
            if (TextUtils.isEmpty(query) || (f.getFacilityName() != null && f.getFacilityName().toLowerCase().contains(query)) ||
                    (f.getLocation() != null && f.getLocation().toLowerCase().contains(query)) ||
                    (f.getDescription() != null && f.getDescription().toLowerCase().contains(query))) {
                filtered.add(f);
            }
        }
        adapter.submitList(filtered);
        if (filtered.isEmpty()) showEmptyState();
        else {
            tvEmptyState.setVisibility(View.GONE);
            rvFacilities.setVisibility(View.VISIBLE);
        }
    }

    // Called from FacilityAdapter when "Book" button is clicked
    public void applyBooking(Facility facility) {
        if (facility.getStatus() != null && facility.getStatus().equalsIgnoreCase("Maintenance")) {
            Toast.makeText(this, "Facility is under maintenance.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CreateBookingActivity.class);
        intent.putExtra("facilityID", facility.getFacilityID());
        intent.putExtra("facilityName", facility.getFacilityName());
        intent.putExtra("capacity", facility.getCapacity());
        intent.putExtra("hourlyRate", facility.getHourlyRate());
        startActivity(intent);
    }
}
