package com.example.facilitybooking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.adapters.FacilityAdapter;
import com.example.facilitybooking.models.DeleteResponse;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageFacilitiesActivity extends AppCompatActivity {

    private RecyclerView rvManageFacilities;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddFacility;
    private FacilityAdapter adapter;
    private FacilityService facilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_facilities);

        rvManageFacilities = findViewById(R.id.rvManageFacilities);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddFacility = findViewById(R.id.fabAddFacility);

        rvManageFacilities.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvManageFacilities);

        fabAddFacility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageFacilitiesActivity.this, AddFacilityActivity.class);
                startActivityForResult(intent, 2001);
            }
        });

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
                Log.d("ManageFacilities", "Response: " + response.code());

                if (response.code() == 200) {
                    List<Facility> facilities = response.body();
                    if (facilities != null && !facilities.isEmpty()) {
                        adapter = new com.example.facilitybooking.adapters.FacilityAdapter(facilities, ManageFacilitiesActivity.this, false);
                        rvManageFacilities.setAdapter(adapter);
                        // For facilities missing imageUrl, try to fetch images and update adapter when available
                        for (int i = 0; i < facilities.size(); i++) {
                            com.example.facilitybooking.models.Facility fac = facilities.get(i);
                            if (fac.getImageUrl() == null || fac.getImageUrl().isEmpty()) {
                                int idx = i;
                                facilityService.getFacilityImages(token, fac.getFacilityID()).enqueue(new Callback<java.util.List<com.example.facilitybooking.models.ImageResponse>>() {
                                    @Override
                                    public void onResponse(Call<java.util.List<com.example.facilitybooking.models.ImageResponse>> call2, Response<java.util.List<com.example.facilitybooking.models.ImageResponse>> resp) {
                                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                                            String url = resp.body().get(0).getImageURL();
                                            if (url != null && !url.isEmpty()) {
                                                facilities.get(idx).setImageUrl(url);
                                                adapter.notifyItemChanged(idx);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<java.util.List<com.example.facilitybooking.models.ImageResponse>> call2, Throwable t2) { }
                                });
                            }
                        }
                        tvEmptyState.setVisibility(View.GONE);
                        rvManageFacilities.setVisibility(View.VISIBLE);
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
                Log.e("ManageFacilities", "Error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvManageFacilities.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_facility_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Facility selectedFacility = adapter.getSelectedItem();

        if (selectedFacility == null) {
            Toast.makeText(this, "No facility selected", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();

        if (itemId == R.id.menu_edit_facility) {
            editFacility(selectedFacility);
        } else if (itemId == R.id.menu_delete_facility) {
            deleteFacility(selectedFacility);
        }

        return super.onContextItemSelected(item);
    }

    private void editFacility(Facility facility) {
        Intent intent = new Intent(this, AddFacilityActivity.class);
        intent.putExtra("isEdit", true);
        intent.putExtra("facilityID", facility.getFacilityID());
        intent.putExtra("facilityName", facility.getFacilityName());
        intent.putExtra("description", facility.getDescription());
        intent.putExtra("capacity", facility.getCapacity());
        intent.putExtra("hourlyRate", facility.getHourlyRate());
        intent.putExtra("location", facility.getLocation());
        intent.putExtra("status", facility.getStatus());
        intent.putExtra("imageUrl", facility.getImageUrl());
        startActivityForResult(intent, 2001);
    }

    public void deleteFacility(Facility facility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Facility");
        builder.setMessage("Are you sure you want to delete " + facility.getFacilityName() + "?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDelete(facility);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void performDelete(Facility facility) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        facilityService = ApiUtils.getFacilityService();
        Call<DeleteResponse> call = facilityService.deleteFacility(user.getToken(), facility.getFacilityID());

        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.code() == 200) {
                    Toast.makeText(ManageFacilitiesActivity.this, "Facility deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadFacilities();
                } else {
                    Toast.makeText(ManageFacilitiesActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(ManageFacilitiesActivity.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            // Either reload entire list or update single item. For simplicity reload.
            Log.d("ManageFacilities", "AddFacilityActivity returned OK, reloading facilities");
            loadFacilities();
        }
    }
}