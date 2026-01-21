package com.example.facilitybooking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Facility;
import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.ViewHolder> {

    private List<Facility> facilityList;
    private Context mContext;
    private int currentPos;

    public FacilityAdapter(List<Facility> facilityList, Context context) {
        this.facilityList = facilityList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.facility_list_item, parent, false);
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

        String status = facility.getStatus().toUpperCase();
        holder.tvStatus.setText(status);

        // Set status badge background
        if ("AVAILABLE".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_available);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_maintenance);
        }

        // Set facility image (placeholder for now - can be replaced with actual image loading)
        holder.imgFacility.setImageResource(android.R.drawable.ic_menu_gallery);
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
        TextView tvFacilityName, tvLocation, tvCapacity, tvHourlyRate, tvDescription, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFacility = itemView.findViewById(R.id.imgFacility);
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
