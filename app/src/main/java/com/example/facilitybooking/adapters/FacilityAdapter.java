package com.example.facilitybooking.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.facilitybooking.AddFacilityActivity;
import com.example.facilitybooking.CreateBookingActivity;
import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.utils.Constants;

public class FacilityAdapter extends ListAdapter<Facility, FacilityAdapter.ViewHolder> {

    private final Activity activity;
    private final boolean enableBookingOnClick; // true=user, false=admin

    public FacilityAdapter(Activity activity, boolean enableBookingOnClick) {
        super(DIFF_CALLBACK);
        this.activity = activity;
        this.enableBookingOnClick = enableBookingOnClick;
    }

    // DiffUtil for efficient updates
    private static final DiffUtil.ItemCallback<Facility> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Facility>() {
                @Override
                public boolean areItemsTheSame(@NonNull Facility oldItem, @NonNull Facility newItem) {
                    return oldItem.getFacilityID() == newItem.getFacilityID();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Facility oldItem, @NonNull Facility newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_facility, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Facility facility = getItem(position);

        // Set text fields
        holder.tvFacilityName.setText(facility.getFacilityName());
        holder.tvLocation.setText(facility.getLocation());
        holder.tvCapacity.setText(facility.getCapacity() + " Guests");
        holder.tvHourlyRate.setText("RM " + String.format("%.2f", facility.getHourlyRate()) + "/hr");

        // Description
        if (facility.getDescription() != null && !facility.getDescription().isEmpty()) {
            holder.tvDescription.setText(facility.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Status
        String status = facility.getStatus() != null ? facility.getStatus().toUpperCase() : "AVAILABLE";
        holder.tvStatus.setText(status);
        holder.tvStatus.setBackgroundResource(
                "AVAILABLE".equals(status) ? R.drawable.status_badge_available : R.drawable.status_badge_maintenance
        );

        // Image
        String imageUrl = facility.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(activity)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgFacility);
        } else {
            holder.imgFacility.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Delete button (ADMIN ONLY)
        if (!enableBookingOnClick) { // admin mode
            holder.btnDeleteFacility.setVisibility(View.VISIBLE);

            holder.btnDeleteFacility.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(activity)
                        .setTitle("Delete Facility")
                        .setMessage("Are you sure you want to delete this facility?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (activity instanceof com.example.facilitybooking.ManageFacilitiesActivity) {
                                ((com.example.facilitybooking.ManageFacilitiesActivity)
                                        activity).deleteFacility(facility);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            holder.btnDeleteFacility.setVisibility(View.GONE);
        }


        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (enableBookingOnClick) {
                // User click: book facility or edit booking
                String facilityStatus = facility.getStatus() != null ? facility.getStatus().toLowerCase() : Constants.FACILITY_STATUS_AVAILABLE;
                if (Constants.FACILITY_STATUS_MAINTENANCE.equalsIgnoreCase(facilityStatus)) {
                    Toast.makeText(activity, Constants.MSG_FACILITY_MAINTENANCE, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(activity, CreateBookingActivity.class);
                intent.putExtra("facilityID", facility.getFacilityID());
                intent.putExtra("facilityName", facility.getFacilityName());
                intent.putExtra("capacity", facility.getCapacity());
                intent.putExtra("hourlyRate", facility.getHourlyRate());
                activity.startActivity(intent);
            } else {
                // Admin click: edit facility
                Intent intent = new Intent(activity, AddFacilityActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("facilityID", facility.getFacilityID());
                intent.putExtra("facilityName", facility.getFacilityName());
                intent.putExtra("description", facility.getDescription());
                intent.putExtra("imageUrl", facility.getImageUrl());
                intent.putExtra("capacity", facility.getCapacity());
                intent.putExtra("hourlyRate", facility.getHourlyRate());
                intent.putExtra("location", facility.getLocation());
                intent.putExtra("status", facility.getStatus());
                activity.startActivity(intent);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFacility;
        ImageButton btnDeleteFacility;
        TextView tvFacilityName, tvLocation, tvCapacity, tvHourlyRate, tvDescription, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFacility = itemView.findViewById(R.id.imgFacility);
            btnDeleteFacility = itemView.findViewById(R.id.btnDeleteFacility);
            tvFacilityName = itemView.findViewById(R.id.tvFacilityName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
