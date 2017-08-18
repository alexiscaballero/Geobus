package com.proyecto.geobus.menuActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.proyecto.geobus.models.SucursalDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.util.RequestSingleton;
import com.proyecto.geobus.util.SaveElementServerCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NuevoChoferActivity extends AppCompatActivity {

    private String host;
    private EditText inputDni;
    private EditText inputNombre;
    private EditText inputUsuario;
    private boolean edicion = false;
    private ChoferDTO choferEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_chofer);
        host = getResources().getString(R.string.host);
        inputDni = (EditText) findViewById(R.id.fdni);
        inputNombre = (EditText) findViewById(R.id.fNombre);
        inputUsuario = (EditText) findViewById(R.id.fUsuario);

        Button registrar = (Button) findViewById(R.id.bt_registrar_chofer);
        if (getIntent().getExtras()!=null) {
            ChoferDTO chofer = (ChoferDTO) getIntent().getSerializableExtra("Chofer");
            inputDni.setText(chofer.getDni()+"");
            inputNombre.setText(chofer.getNombre());
            inputUsuario.setText(chofer.getUsuario());
            registrar.setText("Guardar Cambios");
            TextView titulo = (TextView) findViewById(R.id.textViewTitulo);
            titulo.setText("Editar Chofer");
            choferEditar = chofer;
            edicion = true;
        }

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (
                        ( !inputDni.getText().toString().equals("")) &&
                                ( !inputNombre.getText().toString().equals("")) &&
                                ( !inputUsuario.getText().toString().equals("") &&
                                        (isLong(inputDni.getText().toString())))
                        )
                {
                        ChoferDTO chofer = new ChoferDTO();
                        chofer.setDni(Long.parseLong(inputDni.getText().toString()));
                        chofer.setNombre(inputNombre.getText().toString());
                        chofer.setUsuario(inputUsuario.getText().toString());
                        guardarChofer(chofer);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Uno o mas campos estan vacios, o no son válidos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void guardarChofer(ChoferDTO chofer) {
        if (edicion) {
            chofer.setIdPersona(choferEditar.getIdPersona());
        }
        TokenDTO token = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            token.setToken(settings.getString("TOKEN", null));
            GeobusApi.getInstance(getApplicationContext()).registrarChofer(chofer, token, edicion, new SaveElementServerCallback() {
                @Override
                public void onSuccess(boolean exito) {
                    if (exito) {
                        cargarChoferes();
                    } else {
                        Toast.makeText(getApplicationContext(), "Se produjo un error!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void cargarChoferes() {
        Intent data = new Intent();
        data.putExtra("cargarChoferes", "true");
        setResult(RESULT_OK, data);
        this.finish();
    }

    public boolean isLong(String str) {
        boolean res = false;
        try {
            Long.parseLong(str);
            res = true;
        } catch (NumberFormatException ex) {
        }
        return res;
    }
}
