package com.proyecto.geobus.menuActivities;

import android.app.LauncherActivity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.proyecto.geobus.LoginActivity;
import com.proyecto.geobus.MainActivity;
import com.proyecto.geobus.PrincipalActivity;
import com.proyecto.geobus.R;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.util.RequestSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Iterator;

public class RecorridosActivity extends ListActivity {

    private RecorridosActivity instance;

    private String host;
    private static String listarRecorridosApi = "getRecorridos";
    private ArrayAdapter<String> adapter;
    private ArrayList<String> items;
    private ArrayList<String> idItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        host = getResources().getString(R.string.host);
        final ListView listView = getListView();
        items = new ArrayList<String>();
        idItems = new ArrayList<String>();
        adapter = new ArrayAdapter(this, R.layout.list_recorridos,R.id.tv_recorridos,items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ramalRecorridoSeleccionado = (String) listView.getItemAtPosition(position);
                String idRecorridoSeleccionado = idItems.get(position);
                cargarRecorrido(idRecorridoSeleccionado, ramalRecorridoSeleccionado);
            }
        });
    }

    public void cargarRecorrido(String idRecorrido, String ramalRecorrido) {
        Intent data = new Intent();
        data.putExtra("idRecorridoSeleccionado", idRecorrido);
        data.putExtra("ramalRecorridoSeleccionado", ramalRecorrido);
        setResult(RESULT_OK, data);
        this.finish();
    }

    @Override
    public void onStart() {
        super.onStart();
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
            //obtener lista de recorridos de la api
            //final ProgressDialog progressDialog= ProgressDialog.show(RecorridosActivity.this, "", "Buscando Recorridos...", true);
            try {
                final ProgressDialog progressDialog= ProgressDialog.show(RecorridosActivity.this, "", "Buscando Recorridos...", true);
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

                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            String idRecorrido = jsonObject.get("idRecorrido").getAsString();
                                            String ramalRecorrido = jsonObject.get("ramal").getAsString();
                                            items.add(ramalRecorrido);
                                            idItems.add(idRecorrido);
                                        }
                                        adapter.notifyDataSetChanged();
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

}
