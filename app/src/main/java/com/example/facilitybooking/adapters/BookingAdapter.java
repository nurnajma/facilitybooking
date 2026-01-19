package com.example.facilitybooking.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context mContext;
    private int currentPos;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.booking_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Set facility name (if available)
        if (booking.getFacility() != null) {
            holder.tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
        }

        // Set date and time
        holder.tvBookingDate.setText(booking.getBookingDate());
        String time = booking.getStartTime().substring(0, 5) + " - " + booking.getEndTime().substring(0, 5);
        holder.tvBookingTime.setText(time);

        // Set purpose
        holder.tvPurpose.setText(booking.getPurpose());

        // Set cost
        holder.tvCost.setText("RM " + String.format("%.2f", booking.getTotalCost()));

        // Set status with color
        String status = booking.getStatus().toUpperCase();
        holder.tvStatus.setText(status);

        switch (status) {
            case "PENDING":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "APPROVED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "REJECTED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
                break;
            case "COMPLETED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "CANCELLED":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Grey
                break;
        }

        // Show admin notes if rejected
        if ("REJECTED".equals(status) && booking.getAdminNotes() != null && !booking.getAdminNotes().isEmpty()) {
            holder.layoutAdminNotes.setVisibility(View.VISIBLE);
            holder.tvAdminNotes.setText(booking.getAdminNotes());
        } else {
            holder.layoutAdminNotes.setVisibility(View.GONE);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView tvFacilityName, tvBookingDate, tvBookingTime, tvPurpose, tvCost, tvStatus, tvAdminNotes;
        LinearLayout layoutAdminNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFacilityName = itemView.findViewById(R.id.tvFacilityName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingTime = itemView.findViewById(R.id.tvBookingTime);
            tvPurpose = itemView.findViewById(R.id.tvPurpose);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAdminNotes = itemView.findViewById(R.id.tvAdminNotes);
            layoutAdminNotes = itemView.findViewById(R.id.layoutAdminNotes);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            currentPos = getAdapterPosition();
            return false;
        }
    }
}