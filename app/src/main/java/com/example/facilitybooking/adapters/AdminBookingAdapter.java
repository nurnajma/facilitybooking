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
import com.example.facilitybooking.models.Booking;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context mContext;
    private int currentPos;

    public AdminBookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.admin_booking_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Booking ID
        holder.tvBookingID.setText(String.valueOf(booking.getBookingID()));

        // User info
        holder.tvUserInfo.setText("👤 User ID: " + booking.getUserID());

        // Facility name
        if (booking.getFacility() != null) {
            holder.tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
        }

        // Date and time
        holder.tvBookingDate.setText(booking.getBookingDate());
        String time = booking.getStartTime().substring(0, 5) + " - " + booking.getEndTime().substring(0, 5);
        holder.tvBookingTime.setText(time);

        // Purpose
        holder.tvPurpose.setText(booking.getPurpose());

        // Cost
        holder.tvCost.setText("RM " + String.format("%.2f", booking.getTotalCost()));

        // Status
        String status = booking.getStatus().toUpperCase();
        holder.tvStatus.setText(status);

        switch (status) {
            case "PENDING":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case "APPROVED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "REJECTED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                break;
            case "COMPLETED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#2196F3"));
                break;
            case "CANCELLED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public Booking getSelectedItem() {
        if (currentPos >= 0 && bookingList != null && currentPos < bookingList.size()) {
            return bookingList.get(currentPos);
        }
        return null;
    }

    public void updateList(List<Booking> newList) {
        this.bookingList = newList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView tvBookingID, tvUserInfo, tvFacilityName, tvBookingDate, tvBookingTime, tvPurpose, tvCost, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingID = itemView.findViewById(R.id.tvBookingID);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
            tvFacilityName = itemView.findViewById(R.id.tvFacilityName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingTime = itemView.findViewById(R.id.tvBookingTime);
            tvPurpose = itemView.findViewById(R.id.tvPurpose);
            tvCost = itemView.findViewById(R.id.tvCost);
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