package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.facilitybooking.models.FailLogin;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.UserService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (spm.isLoggedIn()) {
            navigateToDashboard(spm);
            finish();
        }
    }

    public void loginClicked(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (validateLogin(username, password)) {
            doLogin(username, password);
        }
    }

    private void doLogin(String username, String password) {
        UserService userService = ApiUtils.getUserService();

        Call<User> call;
        if (username.contains("@")) {
            call = userService.loginEmail(username, password);
        } else {
            call = userService.login(username, password);
        }

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null && user.getToken() != null) {
                        displayToast("Login successful! Welcome " + user.getUsername());
                        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
                        spm.storeUser(user);
                        navigateToDashboard(spm);
                        finish();
                    } else {
                        displayToast("Login error: No user data received");
                    }
                } else {
                    try {
                        String errorResp = response.errorBody().string();
                        FailLogin failLogin = new Gson().fromJson(errorResp, FailLogin.class);
                        displayToast(failLogin.getError().getMessage());
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Error: " + e.toString());
                        displayToast("Login failed. Please check your credentials.");
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                displayToast("Error connecting to server");
                Log.e("LoginActivity", "Network error: " + t.getMessage());
            }
        });
    }

    private boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            displayToast("Username or email is required");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            displayToast("Password is required");
            return false;
        }
        return true;
    }

    public void openRegister(View view) {
        // Registration API is not defined yet; this simply navigates
        // to a placeholder screen that can be wired to backend later.
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    private void navigateToDashboard(SharedPrefManager spm) {
        Intent intent;
        if (spm.isAdmin()) {
            intent = new Intent(getApplicationContext(), AdminDashboardActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), UserDashboardActivity.class);
        }
        startActivity(intent);
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}