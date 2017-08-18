package com.proyecto.geobus.util;

import com.proyecto.geobus.models.SegmentoDTO;

import org.json.JSONObject;

import java.util.List;

public interface SegmentosServerCallback {
    void onSuccess(List<SegmentoDTO> list);
}