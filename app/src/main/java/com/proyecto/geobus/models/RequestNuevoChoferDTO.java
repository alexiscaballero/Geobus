package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

public class RequestNuevoChoferDTO {

    @SerializedName("dni")
    private Long dni;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("usuario")
    private String usuario;
    @SerializedName("idPersona")
    private int idPersona;
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getDni() {
        return dni;
    }

    public void setDni(Long dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }
}
