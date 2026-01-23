package com.example.facilitybooking.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.AddFacilityActivity;
import com.example.facilitybooking.CreateBookingActivity;
import com.example.facilitybooking.ManageFacilitiesActivity;
import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Facility;
import com.bumptech.glide.Glide;
import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.ViewHolder> {

    private List<Facility> facilityList;
    private Activity activity;
    private int currentPos;
    private boolean enableBookingOnClick = true;

    public FacilityAdapter(List<Facility> facilityList, Activity activity) {
        this(facilityList, activity, true);
    }

    public FacilityAdapter(List<Facility> facilityList, Activity activity, boolean enableBookingOnClick) {
        this.facilityList = facilityList;
        this.activity = activity;
        this.enableBookingOnClick = enableBookingOnClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.facility_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Facility facility = facilityList.get(position);

        holder.tvFacilityName.setText(facility.getFacilityName());
        holder.tvLocation.setText(facility.getLocation());
        holder.tvCapacity.setText(facility.getCapacity() + " Guests");
        holder.tvHourlyRate.setText("RM " + String.format("%.2f", facility.getHourlyRate()) + "/hr");

        if (facility.getDescription() != null && !facility.getDescription().isEmpty()) {
            holder.tvDescription.setText(facility.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        String status = facility.getStatus() != null ? facility.getStatus().toUpperCase() : "AVAILABLE";
        holder.tvStatus.setText(status);

        if ("AVAILABLE".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_available);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_maintenance);
        }

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

        // Handle Admin-only Delete Button
        if (!enableBookingOnClick && activity instanceof ManageFacilitiesActivity) {
            holder.btnDeleteFacility.setVisibility(View.VISIBLE);
            holder.btnDeleteFacility.setOnClickListener(v -> {
                ((ManageFacilitiesActivity) activity).deleteFacility(facility);
            });
        } else {
            holder.btnDeleteFacility.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (enableBookingOnClick) {
                Intent intent = new Intent(activity, CreateBookingActivity.class);
                intent.putExtra("facilityID", facility.getFacilityID());
                intent.putExtra("facilityName", facility.getFacilityName());
                intent.putExtra("capacity", facility.getCapacity());
                intent.putExtra("hourlyRate", facility.getHourlyRate());
                activity.startActivity(intent);
            } else {
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

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    public Facility getSelectedItem() {
        if (currentPos >= 0 && facilityList != null && currentPos < facilityList.size()) {
            return facilityList.get(currentPos);
        }
        return null;
    }

    public void updateList(List<Facility> newList) {
        this.facilityList = newList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
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
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            currentPos = getAdapterPosition();
            return false;
        }
    }
}
