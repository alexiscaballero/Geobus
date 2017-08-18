package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

public class RequestNuevoColectivoDTO {

    @SerializedName("idColectivo")
    private int idColectivo;
    @SerializedName("patente")
    private String patente;
    @SerializedName("modelo")
    private String modelo;
    @SerializedName("marca")
    private String marca;
    @SerializedName("idSucursal")
    private int idSucursal;
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getIdColectivo() {
        return idColectivo;
    }

    public void setIdColectivo(int idColectivo) {
        this.idColectivo = idColectivo;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

}
