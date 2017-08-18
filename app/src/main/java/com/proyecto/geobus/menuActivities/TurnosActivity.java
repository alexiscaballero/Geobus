package com.proyecto.geobus.menuActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.proyecto.geobus.models.ColectivoDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.models.TurnoDTO;
import com.proyecto.geobus.util.RequestSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TurnosActivity extends AppCompatActivity {

    private String host;
    private static String listarTurnosApi = "getTurnos";
    private List<TurnoDTO> listaTurnos = new ArrayList<TurnoDTO>();
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        host = getResources().getString(R.string.host);
        setContentView(R.layout.activity_turnos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        table = (TableLayout) findViewById(R.id.tl_turnos);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NuevoTurnoActivity.class);
                startActivityForResult(i, 1);
            }
        });
    }

    private void addCabecera() {
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(Color.parseColor("#ffcccccc"));
        TextView tx1 = new TextView(getApplicationContext());
        tx1.setText("Recorrido");
        tx1.setTextColor(Color.BLACK);
        tr.addView(tx1, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.25f));
        TextView tx2 = new TextView(getApplicationContext());
        tx2.setText("Colectivo");
        tx2.setTextColor(Color.BLACK);
        tr.addView(tx2, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx3 = new TextView(getApplicationContext());
        tx3.setText("Chofer");
        tx3.setTextColor(Color.BLACK);
        tr.addView(tx3, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx4 = new TextView(getApplicationContext());
        tx4.setText("Horarios");
        tx4.setTextColor(Color.BLACK);
        tr.addView(tx4, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.25f));
        TextView tx5 = new TextView(getApplicationContext());
        tx5.setText("Acciones");
        tx5.setTextColor(Color.BLACK);
        tr.addView(tx5, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addRow(final TurnoDTO turno) {
        listaTurnos.add(turno);
        TableRow tr = new TableRow(this);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        tr.setBackgroundColor(Color.TRANSPARENT);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tx1 = new TextView(getApplicationContext());
        tx1.setText(turno.getRamalRecorrido());
        tx1.setTextColor(Color.BLACK);
        tr.addView(tx1, new TableRow.LayoutParams(0, height, 1.25f));
        TextView tx2 = new TextView(getApplicationContext());
        tx2.setText(turno.getPatenteColectivo());
        tx2.setTextColor(Color.BLACK);
        tr.addView(tx2, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx3 = new TextView(getApplicationContext());
        tx3.setText(turno.getNombreChofer());
        tx3.setTextColor(Color.BLACK);
        tr.addView(tx3, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx4 = new TextView(getApplicationContext());
        tx4.setText(turno.getHoraInicio().substring(0,5)+'-'+turno.getHoraFin().substring(0,5));
        tx4.setTextColor(Color.BLACK);
        tr.addView(tx4, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.25f));
        Button b = new Button(this);
        Drawable myDrawable = resize(getResources().getDrawable(R.drawable.ic_mode_edit_black_36dp));
        b.setCompoundDrawablesWithIntrinsicBounds(null,null,null,myDrawable);
        b.setText("");
        TableRow.LayoutParams params = new TableRow.LayoutParams(5,100, 1f);
        b.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),NuevoTurnoActivity.class);
                i.putExtra("EDIT","TRUE");
                i.putExtra("Turno",turno);
                startActivity(i);
            }
        });
        tr.addView(b, params);
        table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onStart() {
        super.onStart();
        table.removeAllViews();;
        addCabecera();
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
                final ProgressDialog progressDialog= ProgressDialog.show(TurnosActivity.this, "", "Buscando Turnos...", true);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, String.format("%s%s", host, listarTurnosApi), new JSONObject(jsonString), new Response.Listener<JSONObject>() {

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
                                        JsonElement tradeElement = parser.parse(response.getString("turnos"));
                                        JsonArray trade = tradeElement.getAsJsonArray();

                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            TurnoDTO turno = new TurnoDTO();
                                            turno.setIdTurno(jsonObject.get("idTurno").getAsInt());
                                            turno.setIdColectivo(jsonObject.get("idColectivo").getAsInt());
                                            turno.setIdRecorrido(jsonObject.get("idRecorrido").getAsInt());
                                            turno.setIdUsuario(jsonObject.get("idUsuario").getAsInt());
                                            turno.setHoraInicio(jsonObject.get("horaInicio").getAsString());
                                            turno.setHoraFin(jsonObject.get("horaFin").getAsString());
                                            turno.setModeloColectivo(jsonObject.get("modelo").getAsString());
                                            turno.setPatenteColectivo(jsonObject.get("patente").getAsString());
                                            turno.setNombreChofer(jsonObject.get("chofer").getAsString());
                                            turno.setRamalRecorrido(jsonObject.get("ramal").getAsString());
                                            addRow(turno);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                Bundle b = data.getExtras();
                //Aca hay que chequear de que activity viene
                String ok = b.getString("cargarTurnos");
                if (ok=="true") {
                    //Recargar lista de turnos
                }
            }
        }
    }

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

}
