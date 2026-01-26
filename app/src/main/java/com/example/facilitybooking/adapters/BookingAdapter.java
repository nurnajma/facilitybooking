package com.example.facilitybooking.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final Context mContext;
    private final FacilityService facilityService;
    private final SharedPrefManager spm;
    private final OnBookingActionListener listener;

    // Interface to communicate actions back to the Activity
    public interface OnBookingActionListener {
        void onEditBooking(Booking booking);
        void onCancelBooking(Booking booking);
        void onViewDetails(Booking booking);
    }

    // Constructor
    public BookingAdapter(List<Booking> bookingList, Context context, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.mContext = context;
        this.facilityService = ApiUtils.getFacilityService();
        this.spm = new SharedPrefManager(context);
        this.listener = listener;
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

        // Set facility name
        if (booking.getFacility() != null && booking.getFacility().getFacilityName() != null && !booking.getFacility().getFacilityName().isEmpty()) {
            holder.tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            int fidTemp = booking.getFacilityID();
            if (fidTemp <= 0 && booking.getFacility() != null && booking.getFacility().getFacilityID() > 0) {
                fidTemp = booking.getFacility().getFacilityID();
                booking.setFacilityID(fidTemp);
                Log.d("BookingAdapter", "Extracted facilityID from facility object: " + fidTemp + " for booking " + booking.getBookingID());
            }

            if (fidTemp <= 0) {
                holder.tvFacilityName.setText("Facility not found");
            } else {
                final int facilityId = fidTemp;
                holder.tvFacilityName.setText("Loading facility...");
                try {
                    User user = spm.getUser();
                    if (user != null) {
                        facilityService.getFacility(user.getToken(), facilityId).enqueue(new Callback<Facility>() {
                            @Override
                            public void onResponse(Call<Facility> call, Response<Facility> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String name = response.body().getFacilityName();
                                    holder.tvFacilityName.setText(name);
                                    booking.setFacility(response.body());
                                    if (booking.getFacilityID() <= 0) {
                                        booking.setFacilityID(response.body().getFacilityID());
                                    }
                                } else {
                                    holder.tvFacilityName.setText("Facility not found");
                                }
                            }

                            @Override
                            public void onFailure(Call<Facility> call, Throwable t) {
                                holder.tvFacilityName.setText("Facility not found");
                                Log.e("BookingAdapter", "Error fetching facility " + facilityId + ": " + t.getMessage());
                            }
                        });
                    } else {
                        holder.tvFacilityName.setText("Facility not found");
                    }
                } catch (Exception e) {
                    holder.tvFacilityName.setText("Facility not found");
                    Log.e("BookingAdapter", "Exception fetching facility: " + e.getMessage());
                }
            }
        }

        // Set booking info
        holder.tvBookingDate.setText(booking.getBookingDate());
        holder.tvBookingTime.setText(booking.getStartTime().substring(0, 5) + " - " + booking.getEndTime().substring(0, 5));
        holder.tvPurpose.setText(booking.getPurpose());
        holder.tvCost.setText("RM " + String.format("%.2f", booking.getTotalCost()));

        // Status display
        String status = Constants.normalizeStatus(booking.getStatus());
        holder.tvStatus.setText(status != null ? status.toUpperCase() : "");
        int statusColor = Constants.getStatusColor(status);
        holder.tvStatus.setBackgroundColor(statusColor);
        holder.tvStatus.setTextColor(Color.WHITE);

        // Admin notes if rejected
        if (Constants.STATUS_REJECTED.equalsIgnoreCase(status) && booking.getAdminNotes() != null && !booking.getAdminNotes().isEmpty()) {
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
            Booking booking = bookingList.get(getAdapterPosition());
            if (booking == null || listener == null) return false;

            PopupMenu popup = new PopupMenu(mContext, v);
            popup.getMenuInflater().inflate(R.menu.booking_context_menu, popup.getMenu());

            // Hide cancel if not pending
            if (!"pending".equalsIgnoreCase(booking.getStatus())) {
                popup.getMenu().findItem(R.id.menu_cancel_booking).setVisible(false);
                if (popup.getMenu().findItem(R.id.menu_edit_booking) != null) {
                    popup.getMenu().findItem(R.id.menu_edit_booking).setVisible(false);
                }
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_view_booking_details) {
                    listener.onViewDetails(booking);
                    return true;
                } else if (id == R.id.menu_edit_booking) {
                    listener.onEditBooking(booking);
                    return true;
                } else if (id == R.id.menu_cancel_booking) {
                    listener.onCancelBooking(booking);
                    return true;
                }
                return false;
            });

            popup.show();
            return true;
        }
    }
}
