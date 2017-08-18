package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

public class LoginDto {

    @SerializedName("usuario")
    private String username;

    @SerializedName("clave")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
