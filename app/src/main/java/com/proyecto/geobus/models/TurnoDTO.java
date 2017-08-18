package com.proyecto.geobus.models;

import java.io.Serializable;

public class TurnoDTO implements Serializable{

    private int idTurno;
    private String horaInicio;
    private String horaFin;
    private int idColectivo;
    private String modeloColectivo;
    private String patenteColectivo;
    private int idRecorrido;
    private int idUsuario;
    private String nombreChofer;
    private String ramalRecorrido;

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

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

    public String getModeloColectivo() {
        return modeloColectivo;
    }

    public void setModeloColectivo(String modeloColectivo) {
        this.modeloColectivo = modeloColectivo;
    }

    public String getPatenteColectivo() {
        return patenteColectivo;
    }

    public void setPatenteColectivo(String patenteColectivo) {
        this.patenteColectivo = patenteColectivo;
    }

    public int getIdRecorrido() {
        return idRecorrido;
    }

    public void setIdRecorrido(int idRecorrido) {
        this.idRecorrido = idRecorrido;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreChofer() {
        return nombreChofer;
    }

    public void setNombreChofer(String nombreChofer) {
        this.nombreChofer = nombreChofer;
    }

    public String getRamalRecorrido() {
        return ramalRecorrido;
    }

    public void setRamalRecorrido(String ramalRecorrido) {
        this.ramalRecorrido = ramalRecorrido;
    }
}