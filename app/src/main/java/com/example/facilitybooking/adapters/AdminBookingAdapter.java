package com.example.facilitybooking.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.facilitybooking.R;
import com.example.facilitybooking.models.Booking;
import com.example.facilitybooking.remote.ApiUtils;
import com.example.facilitybooking.remote.FacilityService;
import com.example.facilitybooking.remote.UserService;
import com.example.facilitybooking.sharedpref.SharedPrefManager;
import com.example.facilitybooking.models.Facility;
import com.example.facilitybooking.models.User;
import com.example.facilitybooking.utils.Constants;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private final Context mContext;
    private FacilityService facilityService;
    private UserService userService;
    private SharedPrefManager spm;
    private int currentPos;
    private AdminActionListener actionListener;
    private java.util.Set<Integer> processingSet = new java.util.HashSet<>();

    public interface AdminActionListener {
        void onApprove(Booking booking);
        void onReject(Booking booking);
    }

    public AdminBookingAdapter(List<Booking> bookingList, Context context, AdminActionListener listener) {
        this.bookingList = bookingList;
        this.mContext = context;
        this.facilityService = ApiUtils.getFacilityService();
        this.userService = ApiUtils.getUserService();
        this.spm = new SharedPrefManager(context);
        this.actionListener = listener;
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
        holder.tvBookingID.setText("#" + booking.getBookingID());

        // User info (Username)
        holder.tvUserInfo.setText("Loading user...");
        try {
            User currentUser = spm.getUser();
            if (currentUser != null) {
                userService.getUser(currentUser.getToken(), booking.getUserID()).enqueue(new retrofit2.Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            holder.tvUserInfo.setText("👤 " + response.body().getUsername());
                        } else {
                            holder.tvUserInfo.setText("👤 User ID: " + booking.getUserID());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        holder.tvUserInfo.setText("👤 User ID: " + booking.getUserID());
                    }
                });
            } else {
                holder.tvUserInfo.setText("👤 User ID: " + booking.getUserID());
            }
        } catch (Exception e) {
            holder.tvUserInfo.setText("👤 User ID: " + booking.getUserID());
        }

        // Facility name
        if (booking.getFacility() != null && booking.getFacility().getFacilityName() != null && !booking.getFacility().getFacilityName().isEmpty()) {
            holder.tvFacilityName.setText(booking.getFacility().getFacilityName());
        } else {
            // Validate facilityID before making API call
            int fidTemp = booking.getFacilityID();

            // Try to get facilityID from facility object if booking's facilityID is invalid
            if (fidTemp <= 0 && booking.getFacility() != null && booking.getFacility().getFacilityID() > 0) {
                fidTemp = booking.getFacility().getFacilityID();
                booking.setFacilityID(fidTemp);
                Log.d("AdminBookingAdapter", "Extracted facilityID from facility object: " + fidTemp + " for booking " + booking.getBookingID());
            }

            if (fidTemp <= 0) {
                // Invalid facilityID - show error message
                holder.tvFacilityName.setText("Facility not found");
                Log.e("AdminBookingAdapter", "Invalid facilityID (" + fidTemp + ") for booking " + booking.getBookingID() + ". Cannot fetch facility name.");
            } else {
                // make a final copy so the inner callback can reference it
                final int facilityId = fidTemp;

                holder.tvFacilityName.setText("Loading facility...");
                try {
                    User user = spm.getUser();
                    if (user != null) {
                        facilityService.getFacility(user.getToken(), facilityId).enqueue(new retrofit2.Callback<Facility>() {
                            @Override
                            public void onResponse(retrofit2.Call<Facility> call, Response<Facility> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    holder.tvFacilityName.setText(response.body().getFacilityName());
                                    booking.setFacility(response.body());
                                    // Ensure facilityID is set in booking
                                    if (booking.getFacilityID() <= 0) {
                                        booking.setFacilityID(response.body().getFacilityID());
                                    }
                                } else {
                                    holder.tvFacilityName.setText("Facility not found");
                                    Log.w("AdminBookingAdapter", "Failed to fetch facility " + facilityId + " for booking " + booking.getBookingID() + ": " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<Facility> call, Throwable t) {
                                holder.tvFacilityName.setText("Facility not found");
                                Log.e("AdminBookingAdapter", "Error fetching facility " + facilityId + " for booking " + booking.getBookingID() + ": " + t.getMessage());
                            }
                        });
                    } else {
                        holder.tvFacilityName.setText("Facility not found");
                        Log.e("AdminBookingAdapter", "User not found, cannot fetch facility");
                    }
                } catch (Exception e) {
                    holder.tvFacilityName.setText("Facility not found");
                    Log.e("AdminBookingAdapter", "Exception fetching facility: " + e.getMessage());
                }
            }
        }

        // Date and time
        holder.tvBookingDate.setText(booking.getBookingDate());
        String start = booking.getStartTime() != null && booking.getStartTime().length() >= 5 ? booking.getStartTime().substring(0, 5) : "00:00";
        String end = booking.getEndTime() != null && booking.getEndTime().length() >= 5 ? booking.getEndTime().substring(0, 5) : "00:00";
        holder.tvBookingTime.setText(start + " - " + end);

        // Purpose
        holder.tvPurpose.setText(booking.getPurpose());

        // Cost
        holder.tvCost.setText(String.format(Locale.getDefault(), "RM %.2f", booking.getTotalCost()));

        // Status
        String status = Constants.normalizeStatus(booking.getStatus());
        holder.tvStatus.setText(status.toUpperCase());
        holder.tvStatus.setBackgroundColor(Constants.getStatusColor(status));
        holder.tvStatus.setTextColor(Color.WHITE);

        // Show approve/reject buttons only when pending
        boolean isProcessing = processingSet.contains(booking.getBookingID());
        if (Constants.STATUS_PENDING.equals(status)) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnApprove.setEnabled(!isProcessing);
            holder.btnReject.setEnabled(!isProcessing);
            holder.btnApprove.setText(isProcessing ? "Approving..." : "Approve");
            holder.btnApprove.setOnClickListener(v -> {
                if (actionListener != null && !isProcessing) {
                    actionListener.onApprove(booking);
                }
            });
            holder.btnReject.setOnClickListener(v -> {
                if (actionListener != null && !isProcessing) {
                    actionListener.onReject(booking);
                }
            });
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
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

    // Mark a booking as processing (true) or not (false). Adapter will disable buttons for processing items.
    public void setProcessing(int bookingId, boolean processing) {
        if (processing) processingSet.add(bookingId);
        else processingSet.remove(bookingId);

        // find index and refresh that item only
        for (int i = 0; i < bookingList.size(); i++) {
            if (bookingList.get(i).getBookingID() == bookingId) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView tvBookingID, tvUserInfo, tvFacilityName, tvBookingDate, tvBookingTime, tvPurpose, tvCost, tvStatus;
        Button btnApprove, btnReject;

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
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            currentPos = getBindingAdapterPosition();
            return false;
        }
    }
}