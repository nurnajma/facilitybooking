package com.example.facilitybooking;

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

public class FacilityListActivity extends AppCompatActivity {

    private RecyclerView rvFacilityList;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddFacility;
    private FacilityAdapter adapter;
    private FacilityService facilityService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_list);

        rvFacilityList = findViewById(R.id.rvFacilityList);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddFacility = findViewById(R.id.fabAddFacility);

        rvFacilityList.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(rvFacilityList);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (spm.isAdmin()) {
            fabAddFacility.setVisibility(View.VISIBLE);
            fabAddFacility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(FacilityListActivity.this, "Add Facility - Coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
                Log.d("FacilityList", "Response: " + response.code());

                if (response.code() == 200) {
                    List<Facility> facilities = response.body();
                    if (facilities != null && !facilities.isEmpty()) {
                        adapter = new FacilityAdapter(facilities, getApplicationContext());
                        rvFacilityList.setAdapter(adapter);
                        tvEmptyState.setVisibility(View.GONE);
                        rvFacilityList.setVisibility(View.VISIBLE);
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
                Log.e("FacilityList", "Error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvFacilityList.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.facility_context_menu, menu);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isAdmin()) {
            menu.findItem(R.id.menu_update_facility).setVisible(false);
            menu.findItem(R.id.menu_delete_facility).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Facility selectedFacility = adapter.getSelectedItem();

        if (selectedFacility == null) {
            Toast.makeText(this, "No facility selected", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();

        if (itemId == R.id.menu_view_details) {
            viewFacilityDetails(selectedFacility);
        } else if (itemId == R.id.menu_book_facility) {
            bookFacility(selectedFacility);
        } else if (itemId == R.id.menu_update_facility) {
            Toast.makeText(this, "Update facility: " + selectedFacility.getFacilityName(), Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.menu_delete_facility) {
            Toast.makeText(this, "Delete facility: " + selectedFacility.getFacilityName(), Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    private void viewFacilityDetails(Facility facility) {
        Toast.makeText(this, "Viewing: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void bookFacility(Facility facility) {
        Intent intent = new Intent(this, CreateBookingActivity.class);
        intent.putExtra("facilityID", facility.getFacilityID());
        intent.putExtra("facilityName", facility.getFacilityName());
        intent.putExtra("capacity", facility.getCapacity());
        intent.putExtra("hourlyRate", facility.getHourlyRate());
        startActivity(intent);
    }

    private void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}