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
            // Priority: explicit facilityID field > facility object's ID > facility_id field
            // NOTE: Some APIs return facilityID as an object (the entire facility) instead of just an integer
            int extractedFacilityID = 0;
            boolean facilityIDSet = false;
            
            // First, try to get facilityID directly from the booking object
            // Handle both int, string, AND object formats (some APIs return the full facility object in facilityID field)
            if (obj.has("facilityID") && !obj.get("facilityID").isJsonNull()) {
                try { 
                    JsonElement elem = obj.get("facilityID");
                    if (elem.isJsonPrimitive()) {
                        // It's a primitive (int or string)
                        if (elem.getAsJsonPrimitive().isNumber()) {
                            extractedFacilityID = elem.getAsInt();
                        } else if (elem.getAsJsonPrimitive().isString()) {
                            String str = elem.getAsString();
                            if (str != null && !str.isEmpty()) {
                                extractedFacilityID = Integer.parseInt(str);
                            }
                        }
                        if (extractedFacilityID > 0) {
                            b.setFacilityID(extractedFacilityID);
                            facilityIDSet = true;
                        }
                    } else if (elem.isJsonObject()) {
                        // facilityID is actually an object (the full facility) - extract ID from it
                        JsonObject facilityObj = elem.getAsJsonObject();
                        
                        // Try to get the ID from the facility object
                        if (facilityObj.has("facilityID") && !facilityObj.get("facilityID").isJsonNull()) {
                            try {
                                JsonElement idElem = facilityObj.get("facilityID");
                                if (idElem.isJsonPrimitive()) {
                                    if (idElem.getAsJsonPrimitive().isNumber()) {
                                        extractedFacilityID = idElem.getAsInt();
                                    } else if (idElem.getAsJsonPrimitive().isString()) {
                                        String str = idElem.getAsString();
                                        if (str != null && !str.isEmpty()) {
                                            extractedFacilityID = Integer.parseInt(str);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Try alternative field names
                            }
                        }
                        
                        // Also try facility_id or id
                        if (extractedFacilityID <= 0 && facilityObj.has("facility_id") && !facilityObj.get("facility_id").isJsonNull()) {
                            try {
                                JsonElement idElem = facilityObj.get("facility_id");
                                if (idElem.isJsonPrimitive()) {
                                    if (idElem.getAsJsonPrimitive().isNumber()) {
                                        extractedFacilityID = idElem.getAsInt();
                                    } else if (idElem.getAsJsonPrimitive().isString()) {
                                        String str = idElem.getAsString();
                                        if (str != null && !str.isEmpty()) {
                                            extractedFacilityID = Integer.parseInt(str);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Try next
                            }
                        }
                        
                        if (extractedFacilityID <= 0 && facilityObj.has("id") && !facilityObj.get("id").isJsonNull()) {
                            try {
                                JsonElement idElem = facilityObj.get("id");
                                if (idElem.isJsonPrimitive()) {
                                    if (idElem.getAsJsonPrimitive().isNumber()) {
                                        extractedFacilityID = idElem.getAsInt();
                                    } else if (idElem.getAsJsonPrimitive().isString()) {
                                        String str = idElem.getAsString();
                                        if (str != null && !str.isEmpty()) {
                                            extractedFacilityID = Integer.parseInt(str);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        
                        // Deserialize the facility object and set it
                        if (extractedFacilityID > 0) {
                            b.setFacilityID(extractedFacilityID);
                            facilityIDSet = true;
                        }
                        
                        // Deserialize the facility object itself
                        try {
                            Facility f = context.deserialize(elem, Facility.class);
                            if (f != null) {
                                b.setFacility(f);
                                // If we didn't get the ID yet, try from the deserialized object
                                if (!facilityIDSet && f.getFacilityID() > 0) {
                                    b.setFacilityID(f.getFacilityID());
                                    facilityIDSet = true;
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.w("BookingDeserializer", "Failed to deserialize facility from facilityID object: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("BookingDeserializer", "Error processing facilityID field: " + e.getMessage());
                }
            }
            
            // If not set, try facility_id field (handle both int and string)
            if (!facilityIDSet && obj.has("facility_id") && !obj.get("facility_id").isJsonNull()) {
                try { 
                    JsonElement elem = obj.get("facility_id");
                    if (elem.isJsonPrimitive()) {
                        if (elem.getAsJsonPrimitive().isNumber()) {
                            extractedFacilityID = elem.getAsInt();
                        } else if (elem.getAsJsonPrimitive().isString()) {
                            String str = elem.getAsString();
                            if (str != null && !str.isEmpty()) {
                                extractedFacilityID = Integer.parseInt(str);
                            }
                        }
                    }
                    if (extractedFacilityID > 0) {
                        b.setFacilityID(extractedFacilityID);
                        facilityIDSet = true;
                    }
                } catch (Exception ignored) {}
            }
            
            // Also try facilityId (camelCase variant)
            if (!facilityIDSet && obj.has("facilityId") && !obj.get("facilityId").isJsonNull()) {
                try { 
                    JsonElement elem = obj.get("facilityId");
                    if (elem.isJsonPrimitive()) {
                        if (elem.getAsJsonPrimitive().isNumber()) {
                            extractedFacilityID = elem.getAsInt();
                        } else if (elem.getAsJsonPrimitive().isString()) {
                            String str = elem.getAsString();
                            if (str != null && !str.isEmpty()) {
                                extractedFacilityID = Integer.parseInt(str);
                            }
                        }
                    }
                    if (extractedFacilityID > 0) {
                        b.setFacilityID(extractedFacilityID);
                        facilityIDSet = true;
                    }
                } catch (Exception ignored) {}
            }
            
            // If facilityID still not set and we have a facility object, extract ID from it
            if (obj.has("facility") && obj.get("facility").isJsonObject()) {
                try {
                    JsonObject facilityObj = obj.get("facility").getAsJsonObject();
                    
                    // Try to extract facilityID directly from facility object JSON
                    if (!facilityIDSet) {
                        if (facilityObj.has("facilityID") && !facilityObj.get("facilityID").isJsonNull()) {
                            try {
                                extractedFacilityID = facilityObj.get("facilityID").getAsInt();
                                if (extractedFacilityID > 0) {
                                    b.setFacilityID(extractedFacilityID);
                                    facilityIDSet = true;
                                }
                            } catch (Exception ignored) {}
                        }
                        if (!facilityIDSet && facilityObj.has("facility_id") && !facilityObj.get("facility_id").isJsonNull()) {
                            try {
                                extractedFacilityID = facilityObj.get("facility_id").getAsInt();
                                if (extractedFacilityID > 0) {
                                    b.setFacilityID(extractedFacilityID);
                                    facilityIDSet = true;
                                }
                            } catch (Exception ignored) {}
                        }
                        if (!facilityIDSet && facilityObj.has("id") && !facilityObj.get("id").isJsonNull()) {
                            try {
                                extractedFacilityID = facilityObj.get("id").getAsInt();
                                if (extractedFacilityID > 0) {
                                    b.setFacilityID(extractedFacilityID);
                                    facilityIDSet = true;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    // Deserialize the facility object
                    Facility f = context.deserialize(obj.get("facility"), Facility.class);
                    if (f != null) {
                        b.setFacility(f);
                        // If facilityID was not set from JSON but facility object has it, use it
                        if (!facilityIDSet && f.getFacilityID() > 0) {
                            b.setFacilityID(f.getFacilityID());
                            facilityIDSet = true;
                        }
                    }
                } catch (Exception ignored) {}
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
            
            // Final check: if facilityID is still not set, try one more time from facility object
            if (b.getFacilityID() <= 0 && b.getFacility() != null && b.getFacility().getFacilityID() > 0) {
                b.setFacilityID(b.getFacility().getFacilityID());
                facilityIDSet = true;
            }
            
            // Log warning if facilityID is still invalid (for debugging)
            if (b.getFacilityID() <= 0) {
                // Log the entire JSON structure to help debug
                String jsonStr = obj.toString();
                android.util.Log.e("BookingDeserializer", "ERROR: Could not extract facilityID for booking ID=" + b.getBookingID() + 
                    "\nHas facilityID field: " + obj.has("facilityID") + 
                    "\nHas facility_id field: " + obj.has("facility_id") + 
                    "\nHas facility object: " + obj.has("facility") + 
                    "\nFacility object has ID: " + (b.getFacility() != null && b.getFacility().getFacilityID() > 0) +
                    "\nFull JSON: " + jsonStr);
                
                // Last resort: try to extract from any field that might contain the ID
                // Check all possible field names (including nested in facility object)
                String[] possibleFields = {"facilityID", "facility_id", "facilityId"};
                for (String field : possibleFields) {
                    if (obj.has(field)) {
                        try {
                            JsonElement elem = obj.get(field);
                            if (elem.isJsonPrimitive() && !elem.isJsonNull()) {
                                int val = 0;
                                if (elem.getAsJsonPrimitive().isNumber()) {
                                    val = elem.getAsInt();
                                } else if (elem.getAsJsonPrimitive().isString()) {
                                    String str = elem.getAsString();
                                    if (str != null && !str.isEmpty()) {
                                        val = Integer.parseInt(str);
                                    }
                                }
                                if (val > 0) {
                                    b.setFacilityID(val);
                                    android.util.Log.d("BookingDeserializer", "SUCCESS: Extracted facilityID=" + val + " from field '" + field + "'");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // Try next field
                        }
                    }
                }
                
                // If still not found and facility object exists, try to extract from it more aggressively
                if (b.getFacilityID() <= 0 && obj.has("facility")) {
                    try {
                        JsonElement facilityElem = obj.get("facility");
                        if (facilityElem.isJsonObject()) {
                            JsonObject facilityObj = facilityElem.getAsJsonObject();
                            for (String field : possibleFields) {
                                if (facilityObj.has(field)) {
                                    try {
                                        JsonElement elem = facilityObj.get(field);
                                        if (elem.isJsonPrimitive() && !elem.isJsonNull()) {
                                            int val = 0;
                                            if (elem.getAsJsonPrimitive().isNumber()) {
                                                val = elem.getAsInt();
                                            } else if (elem.getAsJsonPrimitive().isString()) {
                                                String str = elem.getAsString();
                                                if (str != null && !str.isEmpty()) {
                                                    val = Integer.parseInt(str);
                                                }
                                            }
                                            if (val > 0) {
                                                b.setFacilityID(val);
                                                android.util.Log.d("BookingDeserializer", "SUCCESS: Extracted facilityID=" + val + " from facility." + field);
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        // Try next field
                                    }
                                }
                            }
                            // Also try "id" field in facility object
                            if (b.getFacilityID() <= 0 && facilityObj.has("id") && !facilityObj.get("id").isJsonNull()) {
                                try {
                                    JsonElement idElem = facilityObj.get("id");
                                    if (idElem.isJsonPrimitive()) {
                                        int val = 0;
                                        if (idElem.getAsJsonPrimitive().isNumber()) {
                                            val = idElem.getAsInt();
                                        } else if (idElem.getAsJsonPrimitive().isString()) {
                                            String str = idElem.getAsString();
                                            if (str != null && !str.isEmpty()) {
                                                val = Integer.parseInt(str);
                                            }
                                        }
                                        if (val > 0) {
                                            b.setFacilityID(val);
                                            android.util.Log.d("BookingDeserializer", "SUCCESS: Extracted facilityID=" + val + " from facility.id");
                                        }
                                    }
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("BookingDeserializer", "Error in last resort extraction: " + e.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            // If parsing fails, throw JsonParseException to bubble up
            throw new JsonParseException("Error deserializing Booking: " + ex.getMessage(), ex);
        }

        return b;
    }
}