package com.proyecto.geobus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import com.proyecto.geobus.util.RequestSingleton;
import com.proyecto.geobus.models.RegistroDto;

public class RegistrationActivity extends AppCompatActivity {

    EditText inputFirstName;
    EditText inputLastName;
    EditText inputUsername;
    EditText inputEmail;
    EditText inputPassword;
    Button btnRegister;
    TextView registerErrorMsg;

    private String host;
    private static String registrarApi = "registrar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        RequestSingleton.getInstance(getApplicationContext()).getRequestQueue().start();
        host = getResources().getString(R.string.host);
        inputFirstName = (EditText) findViewById(R.id.fname);
        inputLastName = (EditText) findViewById(R.id.lname);
        inputUsername = (EditText) findViewById(R.id.uname);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pword);
        btnRegister = (Button) findViewById(R.id.register);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);

        Button login = (Button) findViewById(R.id.bktologin);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }

        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (  ( !inputUsername.getText().toString().equals("")) && ( !inputPassword.getText().toString().equals("")) && ( !inputFirstName.getText().toString().equals("")) && ( !inputLastName.getText().toString().equals("")) && ( !inputEmail.getText().toString().equals("")) )
                {
                    if ( inputUsername.getText().toString().length() > 4 ){
                        RegistroDto registro = new RegistroDto();
                        registro.setApellido(inputLastName.getText().toString());
                        registro.setDeviceId(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                        registro.setEmail(inputEmail.getText().toString());
                        registro.setNombre(inputFirstName.getText().toString());
                        registro.setPassword(inputPassword.getText().toString());
                        registro.setUsername(inputUsername.getText().toString());

                        registrarUsuarioEnServer(registro);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "El nombre de usuario debe tener al menos 5 caracteres", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Uno o mas campos estan vacios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registrarUsuarioEnServer(RegistroDto registro) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(registro);
        try {
            final ProgressDialog progressDialog= ProgressDialog.show(RegistrationActivity.this, "", "Registrando usuario...", true);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, String.format("%s%s", host, registrarApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Usuario registrado correctamente", Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error de comunicacion", Toast.LENGTH_LONG).show();
                        }
                    });
            RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest, null);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Ocurrion un error al preparar el request", Toast.LENGTH_LONG).show();
        }
    }
}
