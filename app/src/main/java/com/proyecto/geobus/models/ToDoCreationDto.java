package com.proyecto.geobus.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lkloster on 9/30/2016.
 */
public class ToDoCreationDto {
    @SerializedName("Titulo")
    String titulo;

    @SerializedName("Descripcion")
    String descripcion;

    @SerializedName("UsuarioId")
    Long usuarioId;

    @SerializedName("PrioridadId")
    Long prioridadId;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPrioridadId() {
        return prioridadId;
    }

    public void setPrioridadId(Long prioridadId) {
        this.prioridadId = prioridadId;
    }
}
