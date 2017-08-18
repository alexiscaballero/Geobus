package com.proyecto.geobus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
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
import com.google.gson.reflect.TypeToken;
import com.proyecto.geobus.menuActivities.RecorridosActivity;
import com.proyecto.geobus.models.ToDoCreationDto;
import com.proyecto.geobus.models.ToDoEntry;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.util.RequestSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public final static int NUEVATODO = 0;
    public final static int VERTODO = 1;
    ArrayAdapter<ToDoEntry> adapter;
    ListView listview;

    private static MainActivity mInstance;

    public static String FILENAME = "todo_file";

    private String host;

    ArrayList<ToDoEntry> list = new ArrayList<>();

    public static MainActivity getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        host = getResources().getString(R.string.host);

        //Cargar settings
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);

        if (settings.getString("TOKEN", null) == null){
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        }else {
            //Chequear que el token siga siendo valido
            chequearToken(settings.getString("TOKEN", null));
        }
        mInstance = this;
    }

    private void chequearToken(String token) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        Gson gson = new Gson();
        String jsonString = gson.toJson(tokenDTO);
        final ProgressDialog progressDialog= ProgressDialog.show(MainActivity.this, "", "Iniciando...", true);
        try {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, ""), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            //chequear si tuvo exito o no
                            Boolean success = false;
                            try {
                                success = Boolean.parseBoolean(response.getString("success"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!success) {
                                progressDialog.dismiss();
                                try {
                                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                                    Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
                                    startActivityForResult(myIntent, 0);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Intent intent = new Intent(MainActivity.getInstance(), PrincipalActivity.class);
                                startActivity(intent);
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
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
        }

    }
}
