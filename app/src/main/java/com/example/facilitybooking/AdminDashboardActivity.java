package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.facilitybooking.models.User;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardManageFacilities;
    private CardView cardAllBookings;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        cardManageFacilities = findViewById(R.id.cardManageFacilities);
        cardAllBookings = findViewById(R.id.cardAllBookings);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        User user = spm.getUser();
        tvWelcome.setText("Welcome, " + user.getUsername() + "!");

        cardManageFacilities.setOnClickListener(v ->
                startActivity(new Intent(this, ManageFacilitiesActivity.class)));

        cardAllBookings.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingsActivity.class)));

        setupBottomNavigation(spm);
    }

    private void setupBottomNavigation(SharedPrefManager spm) {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already here
                return true;
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, AdminBookingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}

