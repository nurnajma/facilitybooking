package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.UserService;
import com.example.facilitybooking.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Button btnRegister;
    private Button btnCancel;
    private ProgressBar progressBar;

    // Validation flags to track field validity
    private boolean isUsernameValid = false;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isConfirmPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Initially disable register button until all fields are valid
        btnRegister.setEnabled(false);

        // Set up real-time validation for all fields
        setupValidation();

        // Cancel button - close activity
        btnCancel.setOnClickListener(v -> finish());

        // Register button - validate and submit
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });
    }

    /**
     * Set up real-time validation using TextWatchers
     * This provides immediate feedback to users as they type
     */
    private void setupValidation() {
        // ========== USERNAME VALIDATION ==========
        // Username must not be empty
        edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = s.toString().trim();
                if (username.isEmpty()) {
                    edtUsername.setError("Name is required");
                    isUsernameValid = false;
                } else {
                    edtUsername.setError(null);
                    isUsernameValid = true;
                }
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ========== EMAIL VALIDATION ==========
        // Email must be in valid format (e.g., user@example.com)
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    edtEmail.setError("Email is required");
                    isEmailValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEmail.setError("Invalid email format");
                    isEmailValid = false;
                } else {
                    edtEmail.setError(null);
                    isEmailValid = true;
                }
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ========== PASSWORD VALIDATION ==========
        // Password must be at least 8 characters
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.isEmpty()) {
                    edtPassword.setError("Password is required");
                    isPasswordValid = false;
                } else if (password.length() < 8) {
                    edtPassword.setError("Password must be at least 8 characters");
                    isPasswordValid = false;
                } else {
                    edtPassword.setError(null);
                    isPasswordValid = true;
                }

                // Re-validate confirm password when password changes
                validateConfirmPassword();
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ========== CONFIRM PASSWORD VALIDATION ==========
        // Confirm password must match password
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Validate that confirm password matches password
     */
    private void validateConfirmPassword() {
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Please confirm your password");
            isConfirmPasswordValid = false;
        } else if (!confirmPassword.equals(password)) {
            edtConfirmPassword.setError("Passwords do not match");
            isConfirmPasswordValid = false;
        } else {
            edtConfirmPassword.setError(null);
            isConfirmPasswordValid = true;
        }
    }

    /**
     * Enable/disable register button based on all validation states
     * Button is only enabled when ALL fields are valid
     */
    private void updateRegisterButtonState() {
        boolean allFieldsValid = isUsernameValid && isEmailValid &&
                isPasswordValid && isConfirmPasswordValid;
        btnRegister.setEnabled(allFieldsValid);
    }

    /**
     * Perform registration API call
     * Assigns default role as "user" for all new registrations
     */
    private void performRegistration() {
        // Get trimmed values
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // ========== ASSIGN DEFAULT ROLE ==========
        // All new users are registered with "user" role
        // Admins must be created manually by existing admins
        String defaultRole = "user";

        // Make API call
        UserService userService = ApiUtils.getUserService();
        Call<User> call = userService.register(username, email, password, defaultRole);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                // ========== SUCCESS HANDLING ==========
                if (response.code() == 201 || response.code() == 200) {
                    // Registration successful
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG).show();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                // ========== ERROR HANDLING ==========
                else if (response.code() == 409) {
                    // Email already exists (conflict)
                    Toast.makeText(RegisterActivity.this,
                            "Email already registered. Please use a different email or login.",
                            Toast.LENGTH_LONG).show();
                    edtEmail.setError("Email already exists");
                    edtEmail.requestFocus();
                } else if (response.code() == 400) {
                    // Bad request - validation error from server
                    Toast.makeText(RegisterActivity.this,
                            "Invalid registration data. Please check your inputs.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Other errors
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG).show();
                    Log.e("RegisterActivity", "Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                // Network error
                Toast.makeText(RegisterActivity.this,
                        Constants.MSG_NETWORK_ERROR,
                        Toast.LENGTH_LONG).show();
                Log.e("RegisterActivity", "Network error: " + t.getMessage());
            }
        });
    }
}