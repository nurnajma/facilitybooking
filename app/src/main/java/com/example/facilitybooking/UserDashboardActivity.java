package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvFacilities.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvFacilities);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        User user = spm.getUser();
        tvWelcome.setText("Welcome, " + user.getUsername() + "!");

        setupBottomNavigation();
        setupSearch();
        loadFacilities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFacilities();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already here
                return true;
            } else if (id == R.id.nav_my_bookings) {
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
        // Search as user types (with slight delay for better performance)
        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                applySearch();
            }
        });

        // Also search on text change with debounce
        android.os.Handler handler = new android.os.Handler();
        Runnable searchRunnable = () -> applySearch();

        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, 500); // 500ms delay
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
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
                        allFacilities = facilities;
                        if (adapter == null) {
                            adapter = new FacilityAdapter(new ArrayList<>(allFacilities), UserDashboardActivity.this);
                            rvFacilities.setAdapter(adapter);
                        } else {
                            adapter.updateList(new ArrayList<>(allFacilities));
                        }
                        tvEmptyState.setVisibility(View.GONE);
                        rvFacilities.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    spm.logout();
                    finish();
                    startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
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
        rvFacilities.setVisibility(View.GONE);
    }

    private void applySearch() {
        if (allFacilities == null || allFacilities.isEmpty()) {
            return;
        }

        String query = edtSearch.getText().toString().trim().toLowerCase();
        if (TextUtils.isEmpty(query)) {
            adapter.updateList(new ArrayList<>(allFacilities));
            tvEmptyState.setVisibility(View.GONE);
            rvFacilities.setVisibility(View.VISIBLE);
            return;
        }

        List<Facility> filtered = new ArrayList<>();
        for (Facility f : allFacilities) {
            if ((f.getFacilityName() != null && f.getFacilityName().toLowerCase().contains(query)) ||
                    (f.getLocation() != null && f.getLocation().toLowerCase().contains(query)) ||
                    (f.getDescription() != null && f.getDescription().toLowerCase().contains(query))) {
                filtered.add(f);
            }
        }

        if (filtered.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            showEmptyState();
        } else {
            adapter.updateList(filtered);
            tvEmptyState.setVisibility(View.GONE);
            rvFacilities.setVisibility(View.VISIBLE);
        }
    }
}

