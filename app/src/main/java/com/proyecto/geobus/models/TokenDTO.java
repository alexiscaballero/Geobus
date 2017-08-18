package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alexis on 31/01/17.
 */

public class TokenDTO {

    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
