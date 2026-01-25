package com.example.facilitybooking.models;

import com.google.gson.*;
import java.lang.reflect.Type;

public class BookingDeserializer implements JsonDeserializer<Booking> {
    @Override
    public Booking deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || !json.isJsonObject()) return null;
        JsonObject obj = json.getAsJsonObject();
        Booking b = new Booking();

        try {
            // bookingID (id, booking_id, bookingID)
            if (obj.has("bookingID") && !obj.get("bookingID").isJsonNull()) {
                b.setBookingID(obj.get("bookingID").getAsInt());
            } else if (obj.has("id") && !obj.get("id").isJsonNull()) {
                b.setBookingID(obj.get("id").getAsInt());
            } else if (obj.has("booking_id") && !obj.get("booking_id").isJsonNull()) {
                b.setBookingID(obj.get("booking_id").getAsInt());
            }

            // userID might be primitive or object
            if (obj.has("userID") && !obj.get("userID").isJsonNull()) {
                JsonElement userEl = obj.get("userID");
                if (userEl.isJsonPrimitive()) {
                    try { b.setUserID(userEl.getAsInt()); } catch (Exception ignored) {}
                } else if (userEl.isJsonObject()) {
                    JsonObject userObj = userEl.getAsJsonObject();
                    if (userObj.has("id") && !userObj.get("id").isJsonNull()) {
                        try { b.setUserID(userObj.get("id").getAsInt()); } catch (Exception ignored) {}
                    } else if (userObj.has("userID") && !userObj.get("userID").isJsonNull()) {
                        try { b.setUserID(userObj.get("userID").getAsInt()); } catch (Exception ignored) {}
                    } else if (userObj.has("user_id") && !userObj.get("user_id").isJsonNull()) {
                        try { b.setUserID(userObj.get("user_id").getAsInt()); } catch (Exception ignored) {}
                    }
                }
            } else if (obj.has("user") && !obj.get("user").isJsonNull()) {
                JsonElement userEl = obj.get("user");
                if (userEl.isJsonPrimitive()) {
                    try { b.setUserID(userEl.getAsInt()); } catch (Exception ignored) {}
                } else if (userEl.isJsonObject()) {
                    JsonObject userObj = userEl.getAsJsonObject();
                    if (userObj.has("id") && !userObj.get("id").isJsonNull()) {
                        try { b.setUserID(userObj.get("id").getAsInt()); } catch (Exception ignored) {}
                    } else if (userObj.has("userID") && !userObj.get("userID").isJsonNull()) {
                        try { b.setUserID(userObj.get("userID").getAsInt()); } catch (Exception ignored) {}
                    } else if (userObj.has("user_id") && !userObj.get("user_id").isJsonNull()) {
                        try { b.setUserID(userObj.get("user_id").getAsInt()); } catch (Exception ignored) {}
                    }
                }
            }

            // facilityID or facility object
            // Common backend variations: facilityID, facilityId, facility_id, facility (id or object)
            if (obj.has("facilityID") && !obj.get("facilityID").isJsonNull()) {
                try { b.setFacilityID(obj.get("facilityID").getAsInt()); } catch (Exception ignored) {}
            } else if (obj.has("facilityId") && !obj.get("facilityId").isJsonNull()) {
                try { b.setFacilityID(obj.get("facilityId").getAsInt()); } catch (Exception ignored) {}
            } else if (obj.has("facility_id") && !obj.get("facility_id").isJsonNull()) {
                try { b.setFacilityID(obj.get("facility_id").getAsInt()); } catch (Exception ignored) {}
            } else if (obj.has("facility") && !obj.get("facility").isJsonNull()) {
                JsonElement facilityEl = obj.get("facility");
                if (facilityEl.isJsonPrimitive()) {
                    // sometimes backend returns facility: 123
                    try { b.setFacilityID(facilityEl.getAsInt()); } catch (Exception ignored) {}
                } else if (facilityEl.isJsonObject()) {
                    try {
                        Facility f = context.deserialize(facilityEl, Facility.class);
                        b.setFacility(f);
                        int fid = (f == null) ? 0 : f.getFacilityID();
                        if (fid <= 0) {
                            // extra safety in case Facility mapping fails
                            JsonObject fObj = facilityEl.getAsJsonObject();
                            if (fObj.has("facilityID") && !fObj.get("facilityID").isJsonNull()) {
                                try { fid = fObj.get("facilityID").getAsInt(); } catch (Exception ignored) {}
                            } else if (fObj.has("facilityId") && !fObj.get("facilityId").isJsonNull()) {
                                try { fid = fObj.get("facilityId").getAsInt(); } catch (Exception ignored) {}
                            } else if (fObj.has("facility_id") && !fObj.get("facility_id").isJsonNull()) {
                                try { fid = fObj.get("facility_id").getAsInt(); } catch (Exception ignored) {}
                            } else if (fObj.has("id") && !fObj.get("id").isJsonNull()) {
                                try { fid = fObj.get("id").getAsInt(); } catch (Exception ignored) {}
                            }
                        }
                        if (fid > 0) b.setFacilityID(fid);
                    } catch (Exception ignored) {}
                }
            }

            // bookingDate
            if (obj.has("bookingDate") && !obj.get("bookingDate").isJsonNull()) b.setBookingDate(obj.get("bookingDate").getAsString());
            else if (obj.has("booking_date") && !obj.get("booking_date").isJsonNull()) b.setBookingDate(obj.get("booking_date").getAsString());

            // startTime / endTime
            if (obj.has("startTime") && !obj.get("startTime").isJsonNull()) b.setStartTime(obj.get("startTime").getAsString());
            else if (obj.has("start_time") && !obj.get("start_time").isJsonNull()) b.setStartTime(obj.get("start_time").getAsString());

            if (obj.has("endTime") && !obj.get("endTime").isJsonNull()) b.setEndTime(obj.get("endTime").getAsString());
            else if (obj.has("end_time") && !obj.get("end_time").isJsonNull()) b.setEndTime(obj.get("end_time").getAsString());

            // purpose, status, adminNotes
            if (obj.has("purpose") && !obj.get("purpose").isJsonNull()) b.setPurpose(obj.get("purpose").getAsString());
            if (obj.has("status") && !obj.get("status").isJsonNull()) b.setStatus(obj.get("status").getAsString());
            if (obj.has("adminNotes") && !obj.get("adminNotes").isJsonNull()) b.setAdminNotes(obj.get("adminNotes").getAsString());
            else if (obj.has("admin_notes") && !obj.get("admin_notes").isJsonNull()) b.setAdminNotes(obj.get("admin_notes").getAsString());

            // totalCost may be number or string
            if (obj.has("totalCost") && !obj.get("totalCost").isJsonNull()) {
                try { b.setTotalCost(obj.get("totalCost").getAsDouble()); } catch (Exception ignored) {}
            } else if (obj.has("total_cost") && !obj.get("total_cost").isJsonNull()) {
                try { b.setTotalCost(obj.get("total_cost").getAsDouble()); } catch (Exception ignored) {}
            }

            // createdAt / updatedAt
            if (obj.has("createdAt") && !obj.get("createdAt").isJsonNull()) b.setCreatedAt(obj.get("createdAt").getAsString());
            if (obj.has("updatedAt") && !obj.get("updatedAt").isJsonNull()) b.setUpdatedAt(obj.get("updatedAt").getAsString());

            // facility object if present and not already set
            if (b.getFacility() == null && obj.has("facility") && obj.get("facility").isJsonObject()) {
                try { Facility f = context.deserialize(obj.get("facility"), Facility.class); b.setFacility(f); } catch (Exception ignored) {}
            }

        } catch (Exception ex) {
            // If parsing fails, throw JsonParseException to bubble up
            throw new JsonParseException("Error deserializing Booking: " + ex.getMessage(), ex);
        }

        return b;
    }
}
