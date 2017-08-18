package com.proyecto.geobus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.proyecto.geobus.api.GeobusApi;
import com.proyecto.geobus.menuActivities.ChoferesActivity;
import com.proyecto.geobus.menuActivities.ColectivosActivity;
import com.proyecto.geobus.menuActivities.RecorridosActivity;
import com.proyecto.geobus.menuActivities.TurnosActivity;
import com.proyecto.geobus.models.SegmentoDTO;
import com.proyecto.geobus.models.TokenDTO;
import com.proyecto.geobus.models.TrackDTO;
import com.proyecto.geobus.util.Misc;
import com.proyecto.geobus.util.SegmentosServerCallback;
import com.proyecto.geobus.util.TrackServerCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                LocationListener{

    private FirstMapFragment mFirstMapFragment;
    private GoogleMap nMap;
    private List<Marker> marcadoresColectivos = new ArrayList<Marker>();
    private UpdateTrackTask hiloTrack = new UpdateTrackTask();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private List<LatLng> listaParadas = new ArrayList<LatLng>();
    private List<Marker> listaMarcadoresParadas = new ArrayList<Marker>();
    private List<SegmentoDTO> listSegmentos = new ArrayList<SegmentoDTO>();
    private TrackDTO ultimoTrackColectivo;
    private int paradaMasCercana = -1;
    private HashMap<Integer, SegmentoDTO> datosParadas = new HashMap<Integer, SegmentoDTO>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ultimoTrackColectivo==null) {
                    Toast.makeText(getApplicationContext(), "Seleccione un recorrido primero!",
                            Toast.LENGTH_SHORT).show();
                } else if (mLastLocation==null) {
                    Toast.makeText(getApplicationContext(), "Activa el gps primero!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    float tiempo = tiempoFaltantePasada();
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(datosParadas.get(paradaMasCercana).getLatitud(),datosParadas.get(paradaMasCercana).getLongitud()))
                            .title("Parada "+(paradaMasCercana+1))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    listaMarcadoresParadas.get(paradaMasCercana).remove();
                    nMap.addMarker(markerOptions);
                    Snackbar.make(view, "Tiempo restante de pasada: "+Misc.leyendaMinutosSegundos(tiempo), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFirstMapFragment = FirstMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map, mFirstMapFragment)
                .commit();

        mFirstMapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.nav_mapa:
                //Do some thing here
                // add navigation drawer item onclick method here
                break;
            case R.id.nav_recorridos:
                i = new Intent(this, RecorridosActivity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.nav_turnos:
                i = new Intent(this, TurnosActivity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.nav_choferes:
                i = new Intent(this, ChoferesActivity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.nav_colectivos:
                i = new Intent(this, ColectivosActivity.class);
                startActivityForResult(i, 1);
                break;
            case R.id.nav_opciones:
                //Do some thing here
                // add navigation drawer item onclick method here
                break;
            case R.id.nav_cerrar_sesion:
                //Do some thing here
                // add navigation drawer item onclick method here
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        LatLng cali = new LatLng(-32.479446, -58.244766);
        //googleMap.addMarker(new MarkerOptions()
        //        .position(cali)
        //        .title("Cali la Sucursal del cielo"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(cali)
                .zoom(13)
                .build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        nMap.setMyLocationEnabled(true);
        nMap.getUiSettings().setMyLocationButtonEnabled(false); //false to disable
        nMap.getUiSettings().setMapToolbarEnabled(false);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                nMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            nMap.setMyLocationEnabled(true);
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null) {
                Bundle b = data.getExtras();
                //Aca hay que chequear de que activity viene
                String ramalRecorrido = b.getString("ramalRecorridoSeleccionado");
                Integer idRecorrido = Integer.parseInt(b.getString("idRecorridoSeleccionado"));
                Toast.makeText(this, "Recorrido Seleccionado: "+idRecorrido+" "+ramalRecorrido,
                        Toast.LENGTH_SHORT).show();
                //Buscar todos los segmentos del recorrido y dibujarlos en el mapa
                dibujarRecorrido(idRecorrido);
                dibujarTrackColectivo(idRecorrido);
            }
        }
    }

    private void dibujarTrackColectivo(Integer idRecorrido) {
        limpiarMarcadoresColectivos();
        hiloTrack.parar();
        hiloTrack.cancel(true);
        hiloTrack = new UpdateTrackTask();
        hiloTrack.execute(idRecorrido);
    }

    private void dibujarRecorrido(Integer idRecorrido) {
        nMap.clear();
        TokenDTO tokenDTO = new TokenDTO();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
        if (settings.getString("TOKEN", null) == null) {
            Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            finish();
        } else {
            tokenDTO.setToken(settings.getString("TOKEN", null));
            GeobusApi.getInstance(this.getApplicationContext()).obtenerSegmentosTurnoActivo(idRecorrido, tokenDTO, new SegmentosServerCallback(){
                @Override
                public void onSuccess(List<SegmentoDTO> listaSegmentos) {
                    //dibujar segmentos en el mapa
                    listSegmentos = listaSegmentos;
                    PolylineOptions recorrido = new PolylineOptions();
                    listaParadas = new ArrayList<LatLng>();
                    int contadorParadas = 0;
                    for (int i = 0; i < listaSegmentos.size(); i++) {
                        recorrido.add(new LatLng(listaSegmentos.get(i).getLatitud(),listaSegmentos.get(i).getLongitud()));
                        if (listaSegmentos.get(i).isParada()) {
                            contadorParadas++;
                            LatLng parada = new LatLng(listaSegmentos.get(i).getLatitud(),listaSegmentos.get(i).getLongitud());
                            listaParadas.add(parada);
                            datosParadas.put(listaParadas.size()-1, listaSegmentos.get(i));
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(parada)
                                    .title("Parada "+contadorParadas)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            listaMarcadoresParadas.add(nMap.addMarker(markerOptions));
                        }
                    }
                    recorrido.color(Color.parseColor("#009BC8"));
                    nMap.addPolyline(recorrido);
                }
            });
        }
    }

    private void limpiarMarcadoresColectivos() {
        for (int i = 0; i < marcadoresColectivos.size(); i++) {
            marcadoresColectivos.get(i).remove();
        }
        marcadoresColectivos.clear();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = nMap.addMarker(markerOptions);

        //move map camera
        nMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        nMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private int obtenerParadaMasCercana(LatLng latLng) {
        int ultimaPosicion = -1;
        float ultimaDistancia = 999999999;
        for(int i=0;i<listaParadas.size();i++){
           //Calcular la distancia a cada parada para obtener la mas cercana
           if (i==0) {
               ultimaDistancia = getDistance(latLng, listaParadas.get(i));
               ultimaPosicion = i;
           } else {
               float dist = getDistance(latLng,listaParadas.get(i));
                if (dist<ultimaDistancia) {
                    ultimaDistancia = dist;
                    ultimaPosicion = i;
                }
           }
        }
        paradaMasCercana = ultimaPosicion;
        return ultimaPosicion;
    }

    private int obtenerSiguienteParada(LatLng point) {
        int resultado = -1;
        List<LatLng> listaAuxiliar = new ArrayList<>();
        //Iterar todos los segmentos para saber en que segmento esta el track del colectivo
        for (int i = 0; i < listSegmentos.size(); i++) {
            listaAuxiliar.add(new LatLng(listSegmentos.get(i).getLatitud(),listSegmentos.get(i).getLongitud()));
            if (((listaAuxiliar.size()%2)==0) && (i!=0)) {
                boolean estaAhi = PolyUtil.isLocationOnPath(point, listaAuxiliar, false, 10);
                if (estaAhi) {
                    resultado = i;
                    break;
                } else {
                    listaAuxiliar.clear();
                    listaAuxiliar.add(new LatLng(listSegmentos.get(i).getLatitud(),listSegmentos.get(i).getLongitud()));
                }
            }
        }
        return resultado;
    }

    private float distanciaEntreParadas(LatLng parada1, LatLng parada2) {
        float distancia = -1;
        boolean guardarDistancia = false;
        LatLng ubicacionAnterior = null;
        //Si son la misma ubicacion devolver 0
        if (parada1.equals(parada2)) {
            return 0;
        }
        //Si son distintas:
        //hallar donde comienza el segmento de la primer parada y empezar a sumar distancias
        for (int i = 0; i < listSegmentos.size(); i++) {
            LatLng ubicacion = new LatLng(listSegmentos.get(i).getLatitud(),listSegmentos.get(i).getLongitud());
            if (parada1.equals(ubicacion)) {
                //comenzar a sumar distancias entre las ubicaciones
                guardarDistancia = true;
                distancia = 0;
                ubicacionAnterior = ubicacion;
            }
            if (guardarDistancia) {
                distancia = distancia + getDistance(ubicacionAnterior,ubicacion);
            }
            if (parada2.equals(ubicacion)) {
                //salir si llegamos a la parada 2
                break;
            }
            ubicacionAnterior = ubicacion;
        }
        return distancia;
    }

    private boolean yaPasoColectivo(LatLng puntoParada,LatLng puntoColectivo) {
        boolean yaPaso = true;
        List<LatLng> listaAuxiliar = new ArrayList<LatLng>();
        for (int i = 0; i < listSegmentos.size(); i++) {
            listaAuxiliar.clear();
            LatLng inicio = new LatLng(listSegmentos.get(i).getLatitud(),listSegmentos.get(i).getLongitud());
            LatLng fin = new LatLng(listSegmentos.get(i+1).getLatitud(),listSegmentos.get(i+1).getLongitud());
            listaAuxiliar.add(inicio);
            listaAuxiliar.add(fin);
            //Chequear si el colectivo esta en el segmento
            //Si el colectivo esta ahí y el puntoParada no es el inicio del segmento, el colectivo aun no paso
            boolean estaAhi = PolyUtil.isLocationOnPath(puntoColectivo, listaAuxiliar, false, 10);
            if (estaAhi && !puntoParada.equals(inicio)) {
                yaPaso = false;
            } else if (puntoParada.equals(inicio)) {
                yaPaso = true;
            }
            if (i==listSegmentos.size()-2) {
                break;
            }
        }
        return yaPaso;
    }

    private float obtenerDistanciaParadaCercana(LatLng punto) {
        float distanciaFinal = -1;
        LatLng ubicacionPersona = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        LatLng paradaMasCercana = listaParadas.get(obtenerParadaMasCercana(ubicacionPersona));
        LatLng ubicacionColectivo = new LatLng(ultimoTrackColectivo.getLatitud(),ultimoTrackColectivo.getLongitud());
        LatLng siguienteUbicacion = new LatLng(listSegmentos.get(obtenerSiguienteParada(punto)).getLatitud(),listSegmentos.get(obtenerSiguienteParada(punto)).getLongitud());
        //la distancia a la parada mas cercana a la posicion del gps se calcula asi:
        //en caso de que el colectivo no haya pasado aun por la parada mas cercana
        //distancia final = distancia(track,siguienteParada)+distancia(siguienteParada,paradaCercana)
        //en caso de que el colectivo ya haya pasado
        //distancia final = distancia(track,siguienteParada)+distancia(siguienteParada,ultimaParada)+
        //                     +distancia(primerParada,ultimaParada)+distancia(primerParada,paradaCercana)
        //Chequear si el colectivo ya paso
        if (yaPasoColectivo(paradaMasCercana, ubicacionColectivo)) {
            float d1 = getDistance(punto,siguienteUbicacion);
            LatLng lFin = new LatLng(listSegmentos.get(listSegmentos.size()-1).getLatitud(),listSegmentos.get(listSegmentos.size()-1).getLongitud());
            //MIRAR
            float d2 = distanciaEntreParadas(siguienteUbicacion,lFin);
            LatLng lInicio = new LatLng(listSegmentos.get(0).getLatitud(),listSegmentos.get(0).getLongitud());
            float d3 = distanciaEntreParadas(lInicio,lFin);
            float d4 = distanciaEntreParadas(lInicio,paradaMasCercana);
            distanciaFinal = d1+d2+d3+d4;
        } else {
            distanciaFinal = getDistance(punto,siguienteUbicacion)+distanciaEntreParadas(siguienteUbicacion,paradaMasCercana);
        }
        return distanciaFinal;
    }

    private float tiempoFaltantePasada() {
        float tiempoFaltante = 0;
        LatLng ultimaPosicionColectivo = new LatLng(ultimoTrackColectivo.getLatitud(),ultimoTrackColectivo.getLongitud());
        float distancia = obtenerDistanciaParadaCercana(ultimaPosicionColectivo);
        float velocidadColectivo = (float) ultimoTrackColectivo.getVelocidad();
        tiempoFaltante = distancia / velocidadColectivo;
        return tiempoFaltante;
    }

    private float getDistance(LatLng my_latlong,LatLng frnd_latlong){
        Location l1=new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);
        Location l2=new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);
        return l1.distanceTo(l2);
    }

    private class UpdateTrackTask extends AsyncTask<Integer, Integer, Long> {

        private boolean continuar = true;

        public void parar() {
            this.continuar = false;
        }

        @Override
        protected Long doInBackground(Integer... integers) {
            int idRecorrido = integers[0];
            while (continuar) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override
                    public void run() {
                        limpiarMarcadoresColectivos();
                    }
                });
                TokenDTO tokenDTO = new TokenDTO();
                SharedPreferences settings = getApplicationContext().getSharedPreferences("preferencesfile", Context.MODE_PRIVATE);
                if (settings.getString("TOKEN", null) == null) {
                    Intent myIntent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivityForResult(myIntent, 0);
                    finish();
                } else {
                    tokenDTO.setToken(settings.getString("TOKEN", null));
                    GeobusApi.getInstance(getApplicationContext()).obtenerTrack(idRecorrido, tokenDTO, new TrackServerCallback() {
                        @Override
                        public void onSuccess(TrackDTO track) {
                            LatLng posicionColectivo = new LatLng(track.getLatitud(), track.getLongitud());
                            ultimoTrackColectivo = track;
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(posicionColectivo)
                                    .title("Colectivo " + (track.getIdColectivo()))
                                    .snippet(track.getPatente() + " - " + Misc.convertirAKMPorSegundo(track.getVelocidad()) + " km/h")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            Marker aux = nMap.addMarker(markerOptions);
                            aux.showInfoWindow();
                            marcadoresColectivos.add(aux);
                        }
                    });
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
