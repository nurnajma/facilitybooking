package com.example.facilitybooking.models;

public class DeleteResponse {
    private int status;
    private String message;

    public DeleteResponse() {}

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "DeleteResponse{status=" + status + ", message='" + message + "'}";
    }
}