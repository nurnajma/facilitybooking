package com.example.facilitybooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFacilityActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText edtFacilityName, edtDescription, edtCapacity, edtHourlyRate, edtLocation;
    private ImageView ivImagePreview;
    private Spinner spinnerStatus;
    private Button btnCancel, btnSave;

    private boolean isEdit = false;
    private int facilityID;
    private android.net.Uri selectedImageUri = null;
    private android.content.ContentResolver contentResolver;
    private Button btnChooseImage;
    private String lastUploadedImageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_facility);

        // Get references
        tvTitle = findViewById(R.id.tvTitle);
        edtFacilityName = findViewById(R.id.edtFacilityName);
        edtDescription = findViewById(R.id.edtDescription);
        edtCapacity = findViewById(R.id.edtCapacity);
        edtHourlyRate = findViewById(R.id.edtHourlyRate);
        edtLocation = findViewById(R.id.edtLocation);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        contentResolver = getContentResolver();

        // Setup status spinner
        String[] statuses = {"available", "maintenance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Check if editing
        if (getIntent().hasExtra("isEdit")) {
            isEdit = getIntent().getBooleanExtra("isEdit", false);
            if (isEdit) {
                tvTitle.setText("Edit Facility");
                btnSave.setText("Update Facility");
                loadFacilityData();
            }
        }

        btnChooseImage.setOnClickListener(v -> {
            // Open image picker
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Choose image"), 1001);
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    updateFacility();
                } else {
                    addFacility();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).centerCrop().placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(ivImagePreview);
            }
        }
    }

    private void loadFacilityData() {
        facilityID = getIntent().getIntExtra("facilityID", -1);
        edtFacilityName.setText(getIntent().getStringExtra("facilityName"));
        edtDescription.setText(getIntent().getStringExtra("description"));
        String existingImage = getIntent().getStringExtra("imageUrl");
        if (existingImage != null && !existingImage.isEmpty()) {
            Glide.with(this).load(existingImage).centerCrop().placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(ivImagePreview);
        }
        edtCapacity.setText(String.valueOf(getIntent().getIntExtra("capacity", 0)));
        edtHourlyRate.setText(String.valueOf(getIntent().getDoubleExtra("hourlyRate", 0.0)));
        edtLocation.setText(getIntent().getStringExtra("location"));

        String status = getIntent().getStringExtra("status");
        if ("maintenance".equals(status)) {
            spinnerStatus.setSelection(1);
        } else {
            spinnerStatus.setSelection(0);
        }
    }

    private void addFacility() {
        if (!validateForm()) {
            return;
        }

        String name = edtFacilityName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        //String imageUrl = edtImageUrl.getText().toString().trim();
        int capacity = Integer.parseInt(edtCapacity.getText().toString().trim());
        double hourlyRate = Double.parseDouble(edtHourlyRate.getText().toString().trim());
        String location = edtLocation.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        FacilityService facilityService = ApiUtils.getFacilityService();
        Call<Facility> call = facilityService.addFacility(user.getToken(), name, description, capacity, hourlyRate, location, status, null);

        call.enqueue(new Callback<Facility>() {
            @Override
            public void onResponse(Call<Facility> call, Response<Facility> response) {
                if (response.code() == 201 || response.code() == 200) {
                    Facility created = response.body();
                    if (created != null && selectedImageUri != null) {
                        // Upload image first, then finish when upload completes (success or fail)
                        uploadImageForFacility(user.getToken(), created.getFacilityID(), selectedImageUri, () -> runOnUiThread(() -> {
                            Toast.makeText(AddFacilityActivity.this, "Facility added successfully!", Toast.LENGTH_SHORT).show();
                            finishSuccess(created.getFacilityID());
                        }));
                    } else {
                        Toast.makeText(AddFacilityActivity.this, "Facility added successfully!", Toast.LENGTH_SHORT).show();
                        finishSuccess(created == null ? -1 : created.getFacilityID());
                    }
                } else {
                    Toast.makeText(AddFacilityActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Facility> call, Throwable t) {
                Toast.makeText(AddFacilityActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("AddFacility", "Error: " + t.getMessage());
            }
        });
    }

    private void updateFacility() {
        if (!validateForm()) {
            return;
        }

        String name = edtFacilityName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        //String imageUrl = edtImageUrl.getText().toString().trim();
        int capacity = Integer.parseInt(edtCapacity.getText().toString().trim());
        double hourlyRate = Double.parseDouble(edtHourlyRate.getText().toString().trim());
        String location = edtLocation.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        FacilityService facilityService = ApiUtils.getFacilityService();
        Call<Facility> call = facilityService.updateFacility(user.getToken(), facilityID, name, description, capacity, hourlyRate, location, status, null);

        call.enqueue(new Callback<Facility>() {
            @Override
            public void onResponse(Call<Facility> call, Response<Facility> response) {
                if (response.code() == 200) {
                    if (selectedImageUri != null) {
                        uploadImageForFacility(user.getToken(), facilityID, selectedImageUri, () -> runOnUiThread(() -> {
                            Toast.makeText(AddFacilityActivity.this, "Facility updated successfully!", Toast.LENGTH_SHORT).show();
                            finishSuccess(facilityID);
                        }));
                    } else {
                        Toast.makeText(AddFacilityActivity.this, "Facility updated successfully!", Toast.LENGTH_SHORT).show();
                        finishSuccess(facilityID);
                    }
                } else {
                    Toast.makeText(AddFacilityActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Facility> call, Throwable t) {
                Toast.makeText(AddFacilityActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                Log.e("UpdateFacility", "Error: " + t.getMessage());
            }
        });
    }

    private boolean validateForm() {
        if (edtFacilityName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter facility name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtCapacity.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter capacity", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtHourlyRate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter hourly rate", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtLocation.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Upload image and call onComplete.run() on completion (success or fail). If onComplete is null, nothing happens.
    private void uploadImageForFacility(String token, int facilityId, android.net.Uri uri, Runnable onComplete) {
        try {
            java.io.InputStream is = contentResolver.openInputStream(uri);
            byte[] bytes = null;
            if (is != null) {
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                bytes = buffer.toByteArray();
                is.close();
            }

            if (bytes == null) return;

            // If image is large, try to compress to JPEG to reduce the payload and avoid server-side validation errors
            int maxSizeBytes = 1024 * 1024; // 1 MB
            byte[] uploadBytes = bytes;
            try {
                if (uploadBytes.length > maxSizeBytes) {
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) {
                        int quality = 85;
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, baos);
                        byte[] compressed = baos.toByteArray();
                        // If still too large, reduce quality in a loop
                        while (compressed.length > maxSizeBytes && quality > 30) {
                            baos.reset();
                            quality -= 10;
                            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, baos);
                            compressed = baos.toByteArray();
                        }
                        if (compressed.length < uploadBytes.length) uploadBytes = compressed;
                        try { baos.close(); } catch (Exception ignored) {}
                    }
                }
            } catch (OutOfMemoryError oom) {
                Log.w("AddFacility", "OOM while compressing image, will upload original bytes");
            } catch (Exception ex) {
                Log.w("AddFacility", "Error compressing image: " + ex.getMessage());
            }

            String mime = contentResolver.getType(uri);
            if (mime == null || mime.isEmpty()) mime = "image/jpeg";
            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(uploadBytes, okhttp3.MediaType.parse(mime));
            okhttp3.MultipartBody.Part bodyFile = okhttp3.MultipartBody.Part.createFormData("file", "image.jpg", requestFile);


            // helper to perform upload with given part name (body already constructed with appropriate name)
            FacilityService fs = ApiUtils.getFacilityService();
            performUpload(fs, token, facilityId, bodyFile, onComplete, true);
        } catch (Exception e) {
            Log.e("AddFacility", "Error uploading image: " + e.getMessage());
            if (onComplete != null) onComplete.run();
        }
    }

    // perform upload and retry with alternative partName 'image' if server returns 422
    private void performUpload(FacilityService fs, String token, int facilityId, okhttp3.MultipartBody.Part bodyFile, Runnable onComplete, boolean allowRetry) {
        fs.uploadFacilityImage(token, facilityId, bodyFile).enqueue(new Callback<com.example.facilitybooking.models.ImageResponse>() {
            @Override
            public void onResponse(Call<com.example.facilitybooking.models.ImageResponse> call, Response<com.example.facilitybooking.models.ImageResponse> response) {
                if (!response.isSuccessful()) {
                    int code = response.code();
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception e) { err = "<error reading errorBody>"; }
                    Log.w("AddFacility", "Image upload failed: " + code + " " + response.message() + " body=" + err);

                    // If 422 Unprocessable Entity and retry allowed, try alternate part name 'image'
                    if (code == 422 && allowRetry) {
                        Log.d("AddFacility", "Retrying upload with alternative part name 'image'...");
                        // rebuild multipart with name 'image'
                        okhttp3.MultipartBody.Part altBody = okhttp3.MultipartBody.Part.createFormData("image", "image.jpg", bodyFile.body());
                        performUpload(fs, token, facilityId, altBody, onComplete, false);
                        return;
                    }

                    if (onComplete != null) onComplete.run();
                    return;
                }

                com.example.facilitybooking.models.ImageResponse img = response.body();
                if (img == null || img.getImageURL() == null || img.getImageURL().isEmpty()) {
                    Log.w("AddFacility", "Image upload succeeded but no imageURL returned");
                    if (onComplete != null) onComplete.run();
                    return;
                }

                final String uploadedUrl = img.getImageURL();
                lastUploadedImageUrl = uploadedUrl;
                Log.d("AddFacility", "Image uploaded, URL=" + uploadedUrl + ", updating facility record...");

                // Fetch current facility, then update it with imageUrl so list endpoint returns it
                FacilityService fs2 = ApiUtils.getFacilityService();
                fs2.getFacility(token, facilityId).enqueue(new Callback<com.example.facilitybooking.models.Facility>() {
                    @Override
                    public void onResponse(Call<com.example.facilitybooking.models.Facility> call2, Response<com.example.facilitybooking.models.Facility> resp2) {
                        if (resp2.isSuccessful() && resp2.body() != null) {
                            com.example.facilitybooking.models.Facility f = resp2.body();
                            // Use existing fields and set imageUrl
                            String name = f.getFacilityName();
                            String desc = f.getDescription();
                            int cap = f.getCapacity();
                            double hr = f.getHourlyRate();
                            String loc = f.getLocation();
                            String stat = f.getStatus();

                            // update facility to store imageUrl
                            fs2.updateFacility(token, facilityId, name, desc, cap, hr, loc, stat, uploadedUrl).enqueue(new Callback<com.example.facilitybooking.models.Facility>() {
                                @Override
                                public void onResponse(Call<com.example.facilitybooking.models.Facility> call3, Response<com.example.facilitybooking.models.Facility> resp3) {
                                    if (resp3.isSuccessful()) {
                                        Log.d("AddFacility", "Facility updated with imageUrl");
                                    } else {
                                        Log.w("AddFacility", "Failed to update facility with imageUrl: " + resp3.code());
                                    }
                                    if (onComplete != null) onComplete.run();
                                }

                                @Override
                                public void onFailure(Call<com.example.facilitybooking.models.Facility> call3, Throwable t3) {
                                    Log.w("AddFacility", "Failed to update facility with imageUrl network error: " + (t3 == null ? "unknown" : t3.getMessage()));
                                    if (onComplete != null) onComplete.run();
                                }
                            });
                        } else {
                            Log.w("AddFacility", "Failed to fetch facility after upload: " + (resp2 == null ? "null response" : resp2.code()));
                            if (onComplete != null) onComplete.run();
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.facilitybooking.models.Facility> call2, Throwable t2) {
                        Log.w("AddFacility", "Error fetching facility after upload: " + (t2 == null ? "unknown" : t2.getMessage()));
                        if (onComplete != null) onComplete.run();
                    }
                });
            }

            @Override
            public void onFailure(Call<com.example.facilitybooking.models.ImageResponse> call, Throwable t) {
                Log.w("AddFacility", "Image upload network error: " + (t == null ? "unknown" : t.getMessage()));
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void finishSuccess(int facilityId) {
        Intent data = new Intent();
        data.putExtra("facilityID", facilityId);
        if (lastUploadedImageUrl != null) data.putExtra("imageUrl", lastUploadedImageUrl);
        setResult(RESULT_OK, data);
        finish();
    }
}