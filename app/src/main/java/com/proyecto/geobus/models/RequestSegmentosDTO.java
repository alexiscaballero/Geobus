package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

public class RequestSegmentosDTO {

    @SerializedName("recorrido")
    private String recorrido;

    @SerializedName("token")
    private String token;

    public String getRecorrido() {
        return recorrido;
    }

    public void setRecorrido(String recorrido) {
        this.recorrido = recorrido;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
