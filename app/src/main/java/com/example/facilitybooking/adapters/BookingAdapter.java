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
import com.example.facilitybooking.utils.Constants;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context mContext;
    private FacilityService facilityService;
    private SharedPrefManager spm;
    private int currentPos;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.mContext = context;
        this.facilityService = ApiUtils.getFacilityService();
        this.spm = new SharedPrefManager(context);
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

        // Set facility name (if available). If not present, fetch by ID.
        if (booking.getFacility() != null && booking.getFacility().getFacilityName() != null && !booking.getFacility().getFacilityName().isEmpty()) {
            holder.tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            holder.tvFacilityName.setText("Loading facility...");
            // Fetch facility name asynchronously
            try {
                User user = spm.getUser();
                if (user != null) {
                    int fid = booking.getFacilityID();
                    facilityService.getFacility(user.getToken(), fid).enqueue(new retrofit2.Callback<Facility>() {
                        @Override
                        public void onResponse(retrofit2.Call<Facility> call, retrofit2.Response<Facility> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String name = response.body().getFacilityName();
                                holder.tvFacilityName.setText(name);
                                // also cache into booking object for reuse
                                booking.setFacility(response.body());
                            } else {
                                holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Facility> call, Throwable t) {
                            holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
                        }
                    });
                } else {
                    holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
                }
            } catch (Exception e) {
                holder.tvFacilityName.setText("Facility ID: " + booking.getFacilityID());
            }
        }

        // Set date and time
        holder.tvBookingDate.setText(booking.getBookingDate());
        String time = booking.getStartTime().substring(0, 5) + " - " + booking.getEndTime().substring(0, 5);
        holder.tvBookingTime.setText(time);

        // Set purpose
        holder.tvPurpose.setText(booking.getPurpose());

        // Set cost
        holder.tvCost.setText("RM " + String.format("%.2f", booking.getTotalCost()));

        // ========== STATUS DISPLAY WITH COLOR INDICATORS ==========
        // Normalize status to uppercase for consistency
        String status = Constants.normalizeStatus(booking.getStatus());
        holder.tvStatus.setText(status != null ? status.toUpperCase() : "");

        // Apply background color and set text color for contrast
        int statusColor = Constants.getStatusColor(status);
        holder.tvStatus.setBackgroundColor(statusColor);
        holder.tvStatus.setTextColor(Color.WHITE);

        // Show admin notes if rejected
        // This helps users understand why their booking was rejected
        if (Constants.STATUS_REJECTED.equals(status) && booking.getAdminNotes() != null && !booking.getAdminNotes().isEmpty()) {
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