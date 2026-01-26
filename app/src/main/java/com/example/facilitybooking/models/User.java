package com.example.facilitybooking.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName(value = "id", alternate = {"userID", "userId"})
    private int id;
    private String email;
    private String username;
    private String password;
    private String token;
    private String role;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}