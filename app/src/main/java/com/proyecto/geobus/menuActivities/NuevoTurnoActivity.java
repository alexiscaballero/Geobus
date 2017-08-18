package com.proyecto.geobus.menuActivities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.proyecto.geobus.LoginActivity;
import com.proyecto.geobus.R;
import com.proyecto.geobus.api.GeobusApi;
import com.proyecto.geobus.models.ChoferDTO;
import com.proyecto.geobus.models.ColectivoDTO;
import com.proyecto.geobus.models.RecorridoDTO;
import com.proyecto.geobus.models.SucursalDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.models.TurnoDTO;
import com.proyecto.geobus.util.RequestSingleton;
import com.proyecto.geobus.util.SaveElementServerCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class NuevoTurnoActivity extends AppCompatActivity {

    private String host;
    private static String listarRecorridosApi = "getRecorridos";
    private static String listarChoferesApi = "getChoferes";
    private static String listarColectivosApi = "getColectivos";
    private Spinner spinnerRecorridos;
    private Spinner spinnerChoferes;
    private Spinner spinnerColectivos;
    private ArrayAdapter<String> adapter;
    private ArrayList<RecorridoDTO> items;
    private ArrayList<String> descripcionesItems;
    private ArrayAdapter<String> adapter2;
    private ArrayList<ChoferDTO> items2;
    private ArrayList<String> descripcionesItems2;
    private ArrayAdapter<String> adapter3;
    private ArrayList<ColectivoDTO> items3;
    private ArrayList<String> descripcionesItems3;
    private boolean edicion = false;
    private TurnoDTO turnoEditar;
    private TimePicker timePicker;
    private TimePicker timePicker2;
    private TextView timeView;
    private TextView timeView2;

    public NuevoTurnoActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_turno);
        host = getResources().getString(R.string.host);

        items = new ArrayList<RecorridoDTO>();
        descripcionesItems = new ArrayList<String>();
        spinnerRecorridos = (Spinner) findViewById(R.id.fRecorrido);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, descripcionesItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerRecorridos.setAdapter(adapter);

        items2 = new ArrayList<ChoferDTO>();
        descripcionesItems2 = new ArrayList<String>();
        spinnerChoferes = (Spinner) findViewById(R.id.fChofer);
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, descripcionesItems2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerChoferes.setAdapter(adapter2);

        items3 = new ArrayList<ColectivoDTO>();
        descripcionesItems3 = new ArrayList<String>();
        spinnerColectivos = (Spinner) findViewById(R.id.fColectivo);
        adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, descripcionesItems3);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerColectivos.setAdapter(adapter3);

        Button registrar = (Button) findViewById(R.id.bt_registrar_turno);
        if (getIntent().getExtras()!=null) {
            TurnoDTO turno = (TurnoDTO) getIntent().getSerializableExtra("Turno");
            registrar.setText("Guardar Cambios");
            TextView titulo = (TextView) findViewById(R.id.textView7);
            titulo.setText("Editar Turno");
            turnoEditar = turno;
            edicion = true;
        }
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (
                        (  ( !spinnerRecorridos.getSelectedItem().toString().equals("")) &&
                                ( !spinnerChoferes.getSelectedItem().toString().equals("")) &&
                                ( !timeView.getText().toString().equals("")) &&
                                ( !timeView2.getText().toString().equals("")) &&
                                ( !spinnerColectivos.getSelectedItem().toString().equals("")))
                        )
                {
                        TurnoDTO turno = new TurnoDTO();
                        int idRecorrido = items.get(spinnerRecorridos.getSelectedItemPosition()).getIdRecorrido();
                        turno.setIdRecorrido(idRecorrido);
                        int idChofer = items2.get(spinnerChoferes.getSelectedItemPosition()).getIdUsuario();
                        turno.setIdUsuario(idChofer);
                        int idColectivo = items3.get(spinnerColectivos.getSelectedItemPosition()).getIdColectivo();
                        turno.setIdColectivo(idColectivo);
                        turno.setHoraInicio(timeView.getText().toString());
                        turno.setHoraFin(timeView2.getText().toString());
                        guardarTurno(turno);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Uno o mas campos estan vacios", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cargarRecorridos();
        cargarChoferes();
        cargarColectivos();

        timeView = (TextView) findViewById(R.id.textView12);
        timeView2 = (TextView) findViewById(R.id.textView13);

        if (edicion) {
            timeView.setText(turnoEditar.getHoraInicio().substring(0,5));
            timeView2.setText(turnoEditar.getHoraFin().substring(0,5));
        }
    }

    private void cargarRecorridos() {
        TokenDTO tokenDTO = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            tokenDTO.setToken(settings.getString("TOKEN", null));
            Gson gson = new Gson();
            String jsonString = gson.toJson(tokenDTO);
            try {
                final ProgressDialog progressDialog= ProgressDialog.show(NuevoTurnoActivity.this, "", "Cargando Recorridos...", true);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, String.format("%s%s", host, listarRecorridosApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

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
                                        JsonElement tradeElement = parser.parse(response.getString("recorridos"));
                                        JsonArray trade = tradeElement.getAsJsonArray();
                                        int posicionEditar = -1;
                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            RecorridoDTO recorrido = new RecorridoDTO();
                                            recorrido.setIdRecorrido(jsonObject.get("idRecorrido").getAsInt());
                                            recorrido.setRamalRecorrido(jsonObject.get("ramal").getAsString());
                                            items.add(recorrido);
                                            descripcionesItems.add(recorrido.getRamalRecorrido());
                                            if (edicion) {
                                                if (recorrido.getIdRecorrido() == turnoEditar.getIdRecorrido()) {
                                                    posicionEditar = i;
                                                }
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                        if (edicion && posicionEditar!=-1) {
                                            spinnerRecorridos.setSelection(posicionEditar);
                                        }
                                        progressDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    try {
                                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
                            }
                        });
                RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest, null);
                RequestSingleton.getInstance(getApplicationContext()).getRequestQueue().start();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cargarChoferes() {
        TokenDTO tokenDTO = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            tokenDTO.setToken(settings.getString("TOKEN", null));
            Gson gson = new Gson();
            String jsonString = gson.toJson(tokenDTO);
            try {
                final ProgressDialog progressDialog= ProgressDialog.show(NuevoTurnoActivity.this, "", "Cargando Choferes...", true);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, String.format("%s%s", host, listarChoferesApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

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
                                        JsonElement tradeElement = parser.parse(response.getString("choferes"));
                                        JsonArray trade = tradeElement.getAsJsonArray();
                                        int posicionEditar = -1;
                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            ChoferDTO chofer = new ChoferDTO();
                                            chofer.setIdUsuario(jsonObject.get("id").getAsInt());
                                            chofer.setDni(jsonObject.get("dni").getAsLong());
                                            chofer.setNombre(jsonObject.get("nombre").getAsString());
                                            chofer.setUsuario(jsonObject.get("usuario").getAsString());
                                            chofer.setIdRol(jsonObject.get("idRol").getAsInt());
                                            items2.add(chofer);
                                            descripcionesItems2.add(chofer.getNombre());
                                            if (edicion) {
                                                if (chofer.getIdUsuario() == turnoEditar.getIdUsuario()) {
                                                    posicionEditar = i;
                                                }
                                            }
                                        }
                                        adapter2.notifyDataSetChanged();
                                        if (edicion && posicionEditar!=-1) {
                                            spinnerChoferes.setSelection(posicionEditar);
                                        }
                                        progressDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    try {
                                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
                            }
                        });
                RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest, null);
                RequestSingleton.getInstance(getApplicationContext()).getRequestQueue().start();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cargarColectivos() {
        TokenDTO tokenDTO = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            tokenDTO.setToken(settings.getString("TOKEN", null));
            Gson gson = new Gson();
            String jsonString = gson.toJson(tokenDTO);
            try {
                final ProgressDialog progressDialog= ProgressDialog.show(NuevoTurnoActivity.this, "", "Cargando Colectivos...", true);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, String.format("%s%s", host, listarColectivosApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

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
                                        JsonElement tradeElement = parser.parse(response.getString("colectivos"));
                                        JsonArray trade = tradeElement.getAsJsonArray();
                                        int posicionEditar = -1;
                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject = trade.get(i).getAsJsonObject();
                                            ColectivoDTO colectivo = new ColectivoDTO();
                                            colectivo.setIdColectivo(jsonObject.get("idColectivo").getAsInt());
                                            colectivo.setIdSucursal(jsonObject.get("idsucursal").getAsInt());
                                            colectivo.setPatente(jsonObject.get("patente").getAsString());
                                            colectivo.setMarca(jsonObject.get("marca").getAsString());
                                            colectivo.setModelo(jsonObject.get("modelo").getAsString());
                                            items3.add(colectivo);
                                            descripcionesItems3.add(colectivo.getPatente());
                                            if (edicion) {
                                                if (colectivo.getIdColectivo() == turnoEditar.getIdColectivo()) {
                                                    posicionEditar = i;
                                                }
                                            }
                                        }
                                        adapter3.notifyDataSetChanged();
                                        if (edicion && posicionEditar!=-1) {
                                            spinnerColectivos.setSelection(posicionEditar);
                                        }
                                        progressDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    try {
                                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
                            }
                        });
                RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest, null);
                RequestSingleton.getInstance(getApplicationContext()).getRequestQueue().start();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void guardarTurno(TurnoDTO turno) {
        if (edicion) {
            turno.setIdTurno(turnoEditar.getIdTurno());
        }
        TokenDTO token = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            token.setToken(settings.getString("TOKEN", null));
            GeobusApi.getInstance(getApplicationContext()).registrarTurno(turno, token, edicion, new SaveElementServerCallback() {
                @Override
                public void onSuccess(boolean exito) {
                    if (exito) {
                        cargarTurnos();
                    } else {
                        Toast.makeText(getApplicationContext(), "Se produjo un error!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void cargarTurnos() {
        Intent data = new Intent();
        data.putExtra("cargarTurnos", "true");
        setResult(RESULT_OK, data);
        this.finish();
    }

    @SuppressWarnings("deprecation")
    public void setTimeHoraInicio(View view) {
        showDialog(111);
    }

    @SuppressWarnings("deprecation")
    public void setTimeHoraFin(View view) {
        showDialog(222);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 111) {
            return new TimePickerDialog(this, myTimeListener, 0, 0, true);
        } else {
            return new TimePickerDialog(this, myTimeListener2, 0, 0, true);
        }
    }

    private TimePickerDialog.OnTimeSetListener myTimeListener = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int h, int m) {
                    showDate(h, m);
                }
            };

    private TimePickerDialog.OnTimeSetListener myTimeListener2 = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int h, int m) {
                    showDate2(h, m);
                }
            };

    private void showDate(int h, int m) {
        timeView.setText(h+":"+m);
    }

    private void showDate2(int h, int m) {
        timeView2.setText(h+":"+m);
    }
}
