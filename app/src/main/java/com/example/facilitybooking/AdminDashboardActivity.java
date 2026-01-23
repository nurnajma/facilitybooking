package com.example.facilitybooking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.facilitybooking.models.User;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardManageFacilities;
    private CardView cardAllBookings;
    private BottomNavigationView bottomNavigation;

    @SuppressLint("SetTextI18n")
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
        
        // ========== ROLE-BASED ACCESS CONTROL ==========
        // Only admins should access this dashboard
        // Redirect regular users to UserDashboardActivity
        if (!Constants.isAdmin(user.getRole())) {
            finish();
            startActivity(new Intent(this, UserDashboardActivity.class));
            return;
        }
        
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
                Intent intent = new Intent(this, AdminBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}

