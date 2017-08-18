package com.proyecto.geobus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.proyecto.geobus.models.LoginDto;
import com.proyecto.geobus.util.RequestSingleton;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    Button Btnregister;
    EditText inputEmail;
    EditText inputPassword;
    private TextView loginErrorMsg;

    private String host;
    private static String loginApi = "authenticate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        host = getResources().getString(R.string.host);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pword);
        Btnregister = (Button) findViewById(R.id.registerbtn);
        btnLogin = (Button) findViewById(R.id.login);
        loginErrorMsg = (TextView) findViewById(R.id.loginErrorMsg);

        Btnregister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), RegistrationActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }});

        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (  ( !inputEmail.getText().toString().equals("")) && ( !inputPassword.getText().toString().equals("")) )
                {
                    LoginDto loginDto = new LoginDto();
                    loginDto.setUsername(inputEmail.getText().toString());
                    loginDto.setPassword(inputPassword.getText().toString());
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(loginDto);
                    try {
                        final ProgressDialog progressDialog= ProgressDialog.show(LoginActivity.this, "", "Iniciando...", true);
                        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                                (Request.Method.POST, String.format("%s%s", host, loginApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = settings.edit();
                                        //chequear si tuvo exito o no
                                        Boolean success = false;
                                        try {
                                             success = Boolean.parseBoolean(response.getString("success"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if (success) {
                                            //Success save token in shared preferences
                                            try {
                                                editor.putString("TOKEN", response.getString("token"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            editor.commit();
                                            progressDialog.dismiss();
                                            //Load main activity
                                            Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
                                            startActivityForResult(myIntent, 0);
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
                        Toast.makeText(getApplicationContext(), "Ocurrion un error al preparar el request", Toast.LENGTH_LONG).show();
                    }
                }
                else if ( ( !inputEmail.getText().toString().equals("")) )
                {
                    Toast.makeText(getApplicationContext(),
                            "Ingrese un usuario", Toast.LENGTH_SHORT).show();
                }
                else if ( ( !inputPassword.getText().toString().equals("")) )
                {
                    Toast.makeText(getApplicationContext(),
                            "Ingrese un usuario y clave", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Complete el formulario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}