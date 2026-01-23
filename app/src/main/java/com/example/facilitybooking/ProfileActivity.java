package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.facilitybooking.models.User;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvRole;
    private Button btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        User user = spm.getUser();
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());

        String role = user.getRole() != null ? user.getRole() : "user";
        tvRole.setText(role.toUpperCase());

        setupBottomNavigation(spm);

        btnLogout.setOnClickListener(v -> {
            spm.logout();
            Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation(SharedPrefManager spm) {
        boolean isAdmin = spm.isAdmin();

        // Inflate correct menu for current role
        bottomNavigation.getMenu().clear();
        if (isAdmin) {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_admin);
        } else {
            bottomNavigation.inflateMenu(R.menu.menu_bottom_nav_user);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent homeIntent = new Intent(this, isAdmin ? AdminDashboardActivity.class : UserDashboardActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                return true;
            } else if (id == R.id.nav_my_bookings && !isAdmin) {
                Intent intent = new Intent(this, MyBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_bookings && isAdmin) {
                Intent intent = new Intent(this, AdminBookingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                // already here
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure selected item matches profile
        if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }
}
