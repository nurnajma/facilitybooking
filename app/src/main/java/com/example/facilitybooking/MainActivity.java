package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.sharedpref.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvUserInfo;
    private Button btnLogout;
    private Button btnBrowseFacilities;
    private Button btnMyBookings;
    private Button btnManageFacilities;
    private Button btnAllBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnBrowseFacilities = findViewById(R.id.btnBrowseFacilities);
        btnMyBookings = findViewById(R.id.btnMyBookings);
        btnManageFacilities = findViewById(R.id.btnManageFacilities);
        btnAllBookings = findViewById(R.id.btnAllBookings);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            User user = spm.getUser();
            tvWelcome.setText("Welcome, " + user.getUsername() + "!");

            String roleText = "Role: " + user.getRole();
            if (spm.isAdmin()) {
                roleText += " (Administrator)";
                btnManageFacilities.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ManageFacilitiesActivity.class);
                        startActivity(intent);
                    }
                });
                btnAllBookings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AdminBookingsActivity.class);
                        startActivity(intent);
                    }
                });
            }
            tvUserInfo.setText(roleText);
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutClicked();
            }
        });

        btnBrowseFacilities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FacilityListActivity.class);
                startActivity(intent);
            }
        });

        btnMyBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyBookingsActivity.class);
                startActivity(intent);
            }
        });


    }

    private void logoutClicked() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_LONG).show();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}