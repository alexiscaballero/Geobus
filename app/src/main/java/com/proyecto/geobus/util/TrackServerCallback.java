package com.proyecto.geobus.util;

import com.proyecto.geobus.models.TrackDTO;

public interface TrackServerCallback {
    void onSuccess(TrackDTO track);
}