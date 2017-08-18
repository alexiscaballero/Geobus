package com.proyecto.geobus.api;

import android.content.Context;
import android.content.res.Resources;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.proyecto.geobus.R;
import com.proyecto.geobus.models.ChoferDTO;
import com.proyecto.geobus.models.ColectivoDTO;
import com.proyecto.geobus.models.RequestNuevoChoferDTO;
import com.proyecto.geobus.models.RequestNuevoColectivoDTO;
import com.proyecto.geobus.models.RequestNuevoTurnoDTO;
import com.proyecto.geobus.models.RequestSegmentosDTO;
import com.proyecto.geobus.models.RequestTrackDTO;
import com.proyecto.geobus.models.SegmentoDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.models.TrackDTO;
import com.proyecto.geobus.models.TurnoDTO;
import com.proyecto.geobus.util.SaveElementServerCallback;
import com.proyecto.geobus.util.SegmentosServerCallback;
import com.proyecto.geobus.util.TrackServerCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GeobusApi {

    private String host;
    private static String segmentosTurnoActivoApi = "getRecorridoTurnoActivo";
    private static String trackColectivoApi = "getTrackColectivo";
    private static String saveColectivoApi = "saveColectivo";
    private static String updateColectivoApi = "updateColectivo";
    private static String saveChoferApi = "saveChofer";
    private static String updateChoferApi = "updatePersona";
    private static String saveTurnoApi = "saveTurno";
    private static String updateTurnoApi = "updateTurno";
    private static GeobusApi mInstance;
    private List<SegmentoDTO> listaSegmentos = new ArrayList<SegmentoDTO>();
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private GeobusApi(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        host = mCtx.getResources().getString(R.string.host);
    }

    public static synchronized GeobusApi getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GeobusApi(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void obtenerSegmentosTurnoActivo(int idRecorrido, TokenDTO tokenDTO, final SegmentosServerCallback callback) {
        listaSegmentos.clear();
        Gson gson = new Gson();
        RequestSegmentosDTO request = new RequestSegmentosDTO();
        request.setRecorrido(String.valueOf(idRecorrido));
        request.setToken(tokenDTO.getToken());
        String jsonString = gson.toJson(request);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, String.format("%s%s", host, segmentosTurnoActivoApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //chequear si tuvo exito o no
                        Boolean success = false;
                        try {
                            success = Boolean.parseBoolean(response.getString("success"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (success) {
                            try {
                                JsonParser parser = new JsonParser();
                                JsonElement tradeElement = parser.parse(response.getString("segmentos"));
                                JsonArray trade = tradeElement.getAsJsonArray();
                                for(int i=0;i<trade.size();i++){
                                    JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                    SegmentoDTO segmento = new SegmentoDTO();
                                    segmento.setIdSegmento(jsonObject.get("idSegmento").getAsInt());
                                    segmento.setIdUbicacion(jsonObject.get("idubicacion").getAsInt());
                                    segmento.setLatitud(jsonObject.get("latitud").getAsFloat());
                                    segmento.setLongitud(jsonObject.get("longitud").getAsFloat());
                                    segmento.setParada(jsonObject.get("parada").getAsBoolean());
                                    listaSegmentos.add(segmento);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        callback.onSuccess((List<SegmentoDTO>) listaSegmentos);
                    }
                }, new Response.ErrorListener() {
                    @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                });
                addToRequestQueue(jsObjRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public void obtenerTrack(int idRecorrido, TokenDTO tokenDTO, final TrackServerCallback callback) {
        Gson gson = new Gson();
        RequestTrackDTO request = new RequestTrackDTO();
        request.setRecorrido(String.valueOf(idRecorrido));
        request.setToken(tokenDTO.getToken());
        String jsonString = gson.toJson(request);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, trackColectivoApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            TrackDTO trackDTO = new TrackDTO();
                            //chequear si tuvo exito o no
                            Boolean success = false;
                            try {
                                success = Boolean.parseBoolean(response.getString("success"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (success) {
                                try {
                                    JsonParser parser = new JsonParser();
                                    JsonElement tradeElement = parser.parse(response.getString("track"));
                                    JsonObject object = tradeElement.getAsJsonObject();
                                    trackDTO.setIdColectivo(object.get("idColectivo").getAsInt());
                                    trackDTO.setPatente(object.get("patente").getAsString());
                                    trackDTO.setMarca(object.get("marca").getAsString());
                                    trackDTO.setIdTrack(object.get("idTrack").getAsInt());
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                                    String fecha = object.get("fecha").getAsString().substring(0,10);
                                    String hora = object.get("hora").getAsString();
                                    Date date = formatter.parse(fecha+" "+hora);
                                    trackDTO.setFecha(date);
                                    trackDTO.setLatitud(object.get("latitud").getAsFloat());
                                    trackDTO.setLongitud(object.get("longitud").getAsFloat());
                                    trackDTO.setVelocidad(object.get("velocidad").getAsDouble());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            callback.onSuccess(trackDTO);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            addToRequestQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registrarColectivo(ColectivoDTO colectivo, TokenDTO tokenDTO, boolean edicion, final SaveElementServerCallback callback) {
        Gson gson = new Gson();
        RequestNuevoColectivoDTO request = new RequestNuevoColectivoDTO();
        request.setMarca(colectivo.getMarca());
        request.setModelo(colectivo.getModelo());
        request.setPatente(colectivo.getPatente());
        request.setIdSucursal(colectivo.getIdSucursal());
        request.setToken(tokenDTO.getToken());
        String ruta = "";
        if (edicion) {
            ruta = updateColectivoApi;
            request.setIdColectivo(colectivo.getIdColectivo());
        } else {
            ruta = saveColectivoApi;
        }
        String jsonString = gson.toJson(request);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, ruta), new JSONObject(jsonString), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //chequear si tuvo exito o no
                            Boolean success = false;
                            try {
                                success = Boolean.parseBoolean(response.getString("success"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (success) {
                                callback.onSuccess(true);
                            } else {
                                callback.onSuccess(false);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            addToRequestQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registrarChofer(ChoferDTO chofer, TokenDTO tokenDTO, boolean edicion, final SaveElementServerCallback callback) {
        Gson gson = new Gson();
        RequestNuevoChoferDTO request = new RequestNuevoChoferDTO();
        request.setNombre(chofer.getNombre());
        request.setDni(chofer.getDni());
        request.setUsuario(chofer.getUsuario());
        request.setToken(tokenDTO.getToken());
        String ruta = "";
        if (edicion) {
            ruta = updateChoferApi;
            request.setIdPersona(chofer.getIdPersona());
        } else {
            ruta = saveChoferApi;
        }
        String jsonString = gson.toJson(request);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, ruta), new JSONObject(jsonString), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //chequear si tuvo exito o no
                            Boolean success = false;
                            try {
                                success = Boolean.parseBoolean(response.getString("success"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (success) {
                                callback.onSuccess(true);
                            } else {
                                callback.onSuccess(false);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            addToRequestQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registrarTurno(TurnoDTO turno, TokenDTO tokenDTO, boolean edicion, final SaveElementServerCallback callback) {
        Gson gson = new Gson();
        RequestNuevoTurnoDTO request = new RequestNuevoTurnoDTO();
        request.setHoraInicio(turno.getHoraInicio());
        request.setHoraFin(turno.getHoraFin());
        request.setIdColectivo(turno.getIdColectivo());
        request.setIdUsuario(turno.getIdUsuario());
        request.setIdRecorrido(turno.getIdRecorrido());
        request.setToken(tokenDTO.getToken());
        String ruta = "";
        if (edicion) {
            ruta = updateTurnoApi;
            request.setIdTurno(turno.getIdTurno());
        } else {
            ruta = saveTurnoApi;
        }
        String jsonString = gson.toJson(request);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, ruta), new JSONObject(jsonString), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //chequear si tuvo exito o no
                            Boolean success = false;
                            try {
                                success = Boolean.parseBoolean(response.getString("success"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (success) {
                                callback.onSuccess(true);
                            } else {
                                callback.onSuccess(false);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            addToRequestQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
