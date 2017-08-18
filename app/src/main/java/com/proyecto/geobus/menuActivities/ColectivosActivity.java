package com.proyecto.geobus.menuActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.proyecto.geobus.LoginActivity;
import com.proyecto.geobus.R;
import com.proyecto.geobus.models.ColectivoDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.util.RequestSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ColectivosActivity extends AppCompatActivity {

    private String host;
    private static String listarColectivosApi = "getColectivos";
    private List<ColectivoDTO> listaColectivos = new ArrayList<ColectivoDTO>();
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        host = getResources().getString(R.string.host);
        setContentView(R.layout.activity_colectivos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        table = (TableLayout) findViewById(R.id.tl_colectivos);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NuevoColectivoActivity.class);
                startActivityForResult(i, 1);
            }
        });
    }

    private void addCabecera() {
        TableRow tr = new TableRow(this);
        //tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, heightInPixels));
        tr.setBackgroundColor(Color.parseColor("#ffcccccc"));
        TextView tx1 = new TextView(getApplicationContext());
        tx1.setText("Patente");
        tx1.setTextColor(Color.BLACK);
        tr.addView(tx1, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx2 = new TextView(getApplicationContext());
        tx2.setText("Marca");
        tx2.setTextColor(Color.BLACK);
        tr.addView(tx2, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
        TextView tx3 = new TextView(getApplicationContext());
        tx3.setText("Modelo");
        tx3.setTextColor(Color.BLACK);
        tr.addView(tx3, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx4 = new TextView(getApplicationContext());
        tx4.setText("Acciones");
        tx4.setTextColor(Color.BLACK);
        tr.addView(tx4, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void addRow(final ColectivoDTO colectivo) {
        listaColectivos.add(colectivo);
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(Color.TRANSPARENT);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tx1 = new TextView(getApplicationContext());
        tx1.setText(colectivo.getPatente());
        tx1.setTextColor(Color.BLACK);
        tr.addView(tx1, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        TextView tx2 = new TextView(getApplicationContext());
        tx2.setText(colectivo.getMarca());
        tx2.setTextColor(Color.BLACK);
        tr.addView(tx2, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
        TextView tx3 = new TextView(getApplicationContext());
        tx3.setText(colectivo.getModelo());
        tx3.setTextColor(Color.BLACK);
        tr.addView(tx3, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        Button b = new Button(this);
        Drawable myDrawable = resize(getResources().getDrawable(R.drawable.ic_mode_edit_black_36dp));
        b.setCompoundDrawablesWithIntrinsicBounds(null,null,null,myDrawable);
        b.setText("");
        TableRow.LayoutParams params = new TableRow.LayoutParams(5,100, 0.5f);
        b.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),NuevoColectivoActivity.class);
                i.putExtra("EDIT","TRUE");
                i.putExtra("Colectivo",colectivo);
                startActivity(i);
            }
        });
        tr.addView(b, params);
        Button b2 = new Button(this);
        Drawable myDrawable2 = resize(getResources().getDrawable(R.drawable.ic_location_searching_black_36dp));
        b2.setCompoundDrawablesWithIntrinsicBounds(null,null,null,myDrawable2);
        b2.setText("");
        tr.addView(b2, new TableRow.LayoutParams(5, 100, 0.5f));
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
                final ProgressDialog progressDialog= ProgressDialog.show(ColectivosActivity.this, "", "Buscando Colectivos...", true);
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

                                        for(int i=0;i<trade.size();i++){
                                            JsonObject jsonObject= trade.get(i).getAsJsonObject();
                                            ColectivoDTO colectivo = new ColectivoDTO();
                                            colectivo.setIdColectivo(jsonObject.get("idColectivo").getAsInt());
                                            colectivo.setMarca(jsonObject.get("marca").getAsString());
                                            colectivo.setModelo(jsonObject.get("modelo").getAsString());
                                            colectivo.setPatente(jsonObject.get("patente").getAsString());
                                            colectivo.setIdSucursal(jsonObject.get("idsucursal").getAsInt());
                                            addRow(colectivo);
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
                String ok = b.getString("cargarColectivos");
                if (ok=="true") {
                    //Recargar lista de colectivos
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
