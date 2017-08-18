package com.proyecto.geobus.menuActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.proyecto.geobus.LoginActivity;
import com.proyecto.geobus.R;
import com.proyecto.geobus.api.GeobusApi;
import com.proyecto.geobus.models.ColectivoDTO;
import com.proyecto.geobus.models.RegistroDto;
import com.proyecto.geobus.models.RequestNuevoColectivoDTO;
import com.proyecto.geobus.models.SucursalDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.models.TrackDTO;
import com.proyecto.geobus.util.Misc;
import com.proyecto.geobus.util.RequestSingleton;
import com.proyecto.geobus.util.SaveElementServerCallback;
import com.proyecto.geobus.util.TrackServerCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NuevoColectivoActivity extends AppCompatActivity {

    private String host;
    private static String listarSucursalesApi = "getSucursales";
    private EditText inputModelo;
    private EditText inputMarca;
    private EditText inputPatente;
    private Spinner spinnerSucursal;
    private ArrayAdapter<String> adapter;
    private ArrayList<SucursalDTO> items;
    private ArrayList<String> descripcionesItems;
    private boolean edicion = false;
    private ColectivoDTO colectivoEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_colectivo);
        host = getResources().getString(R.string.host);
        inputModelo = (EditText) findViewById(R.id.fmodelo);
        inputMarca = (EditText) findViewById(R.id.fMarca);
        inputPatente = (EditText) findViewById(R.id.fPatente);

        items = new ArrayList<SucursalDTO>();
        descripcionesItems = new ArrayList<String>();
        spinnerSucursal = (Spinner) findViewById(R.id.fSucursal);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, descripcionesItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinnerSucursal.setAdapter(adapter);
        Button registrar = (Button) findViewById(R.id.bt_registrar_colectivo);
        if (getIntent().getExtras()!=null) {
            ColectivoDTO colectivo = (ColectivoDTO) getIntent().getSerializableExtra("Colectivo");
            inputModelo.setText(colectivo.getModelo());
            inputMarca.setText(colectivo.getMarca());
            inputPatente.setText(colectivo.getPatente());
            registrar.setText("Guardar Cambios");
            TextView titulo = (TextView) findViewById(R.id.textView);
            titulo.setText("Editar Colectivo");
            colectivoEditar = colectivo;
            edicion = true;
        }

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (
                        ( !inputModelo.getText().toString().equals("")) &&
                                ( !inputMarca.getText().toString().equals("")) &&
                                ( !inputPatente.getText().toString().equals("")) &&
                                ( !spinnerSucursal.getSelectedItem().toString().equals(""))
                        )
                {
                        ColectivoDTO colectivo = new ColectivoDTO();
                        colectivo.setMarca(inputMarca.getText().toString());
                        colectivo.setModelo(inputModelo.getText().toString());
                        colectivo.setPatente(inputPatente.getText().toString());
                        int idSucursal = items.get(spinnerSucursal.getSelectedItemPosition()).getIdSucursal();
                        colectivo.setIdSucursal(idSucursal);
                        guardarColectivo(colectivo);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Uno o mas campos estan vacios", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cargarSucursales();
    }

    private void cargarSucursales() {
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
                final ProgressDialog progressDialog= ProgressDialog.show(NuevoColectivoActivity.this, "", "Cargando Sucursales...", true);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, String.format("%s%s", host, listarSucursalesApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

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
                                        JsonElement tradeElement = parser.parse(response.getString("sucursales"));
                                        JsonArray trade = tradeElement.getAsJsonArray();
                                        int posicionEditar = -1;
                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            SucursalDTO sucursal = new SucursalDTO();
                                            sucursal.setIdSucursal(jsonObject.get("idSucursal").getAsInt());
                                            sucursal.setDescripcion(jsonObject.get("descripcion").getAsString());
                                            sucursal.setDireccion(jsonObject.get("direccion").getAsString());
                                            sucursal.setIdCiudad(jsonObject.get("idciudad").getAsInt());
                                            sucursal.setIdEmpresa(jsonObject.get("idempresa").getAsInt());
                                            items.add(sucursal);
                                            descripcionesItems.add(sucursal.getDescripcion());
                                            if (edicion) {
                                                if (sucursal.getIdSucursal() == colectivoEditar.getIdSucursal()) {
                                                    posicionEditar = i;
                                                }
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                        if (edicion && posicionEditar!=-1) {
                                            spinnerSucursal.setSelection(posicionEditar);
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
                //progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void guardarColectivo(ColectivoDTO colectivo) {
        if (edicion) {
            colectivo.setIdColectivo(colectivoEditar.getIdColectivo());
        }
        TokenDTO token = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            token.setToken(settings.getString("TOKEN", null));
            GeobusApi.getInstance(getApplicationContext()).registrarColectivo(colectivo, token, edicion, new SaveElementServerCallback() {
                @Override
                public void onSuccess(boolean exito) {
                    if (exito) {
                        cargarColectivos();
                    } else {
                        Toast.makeText(getApplicationContext(), "Se produjo un error!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void cargarColectivos() {
        Intent data = new Intent();
        data.putExtra("cargarColectivos", "true");
        setResult(RESULT_OK, data);
        this.finish();
    }
}
