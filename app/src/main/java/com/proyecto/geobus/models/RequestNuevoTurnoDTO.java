package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

public class RequestNuevoTurnoDTO {

    @SerializedName("idTurno")
    private int idTurno;
    @SerializedName("horaInicio")
    private String horaInicio;
    @SerializedName("horaFin")
    private String horaFin;
    @SerializedName("idColectivo")
    private int idColectivo;
    @SerializedName("idUsuario")
    private int idUsuario;
    @SerializedName("idRecorrido")
    private int idRecorrido;
    @SerializedName("token")
    private String token;

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public int getIdColectivo() {
        return idColectivo;
    }

    public void setIdColectivo(int idColectivo) {
        this.idColectivo = idColectivo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdRecorrido() {
        return idRecorrido;
    }

    public void setIdRecorrido(int idRecorrido) {
        this.idRecorrido = idRecorrido;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }
}
