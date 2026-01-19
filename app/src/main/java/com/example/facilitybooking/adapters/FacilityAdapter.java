package com.example.facilitybooking.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.tvCapacity.setText("Capacity: " + facility.getCapacity());
        holder.tvHourlyRate.setText("RM " + String.format("%.2f", facility.getHourlyRate()) + "/hr");
        holder.tvDescription.setText(facility.getDescription());

        String status = facility.getStatus().toUpperCase();
        holder.tvStatus.setText(status);

        if ("AVAILABLE".equals(status)) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
        }
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView tvFacilityName, tvLocation, tvCapacity, tvHourlyRate, tvDescription, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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