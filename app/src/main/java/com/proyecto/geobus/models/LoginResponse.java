package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lkloster on 9/30/2016.
 */
public class LoginResponse {
    @SerializedName("Token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
