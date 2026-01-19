package com.example.facilitybooking.models;

public class FailLogin {
    private int status;
    private Error error;

    public FailLogin() {}

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Error getError() { return error; }
    public void setError(Error error) { this.error = error; }

    public static class Error {
        private int code;
        private String status;
        private String message;

        public Error() {}

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}