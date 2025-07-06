package com.example.pratica5;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private GoogleMap map;
    private Cursor locais;
    public LatLng coordenada;
    private String local, categoria;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        toolbar = findViewById(R.id.toolbarMapa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MapaCheckin");
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa)).getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.voltarMapa) {
            finish();
            return true;
        } else if (id == R.id.gestaoDeCheckIn) {
            Intent gestao = new Intent(getBaseContext(), Gestao.class);
            startActivity(gestao);
            return true;
        } else if (id == R.id.lugaresMaisVisitados) {
            Intent lugares = new Intent(getBaseContext(), Relatorio.class);
            startActivity(lugares);
            return true;
        } else if (id == R.id.mapaNormal) {
            if (map != null) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            return true;
        } else if (id == R.id.mapaHibrido) {
            if (map != null) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        locais = bd.buscar("Checkin ch, Categoria cat", new String[]{"ch.Local Local", "ch.latitude lat", "ch.longitude lng", "ch.qtdVisitas visitas", "cat.nome nomeCategoria"}, "ch.cat = cat.idCategoria", null);

        if(locais.moveToFirst()) {
            do {
                local = locais.getString(locais.getColumnIndexOrThrow("Local"));
                categoria = locais.getString(locais.getColumnIndexOrThrow("nomeCategoria"));
                latitude = locais.getDouble(locais.getColumnIndexOrThrow("lat"));
                longitude = locais.getDouble(locais.getColumnIndexOrThrow("lng"));
                int visitas = locais.getInt(locais.getColumnIndexOrThrow("visitas"));
                coordenada = new LatLng(latitude, longitude);
                map.addMarker(new MarkerOptions().position(coordenada).title(local).snippet("Categoria: " + categoria + " Visitas: " + visitas));
            } while (locais.moveToNext());
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitudeAtual") && intent.hasExtra("longitudeAtual")) {
            double latAtual = intent.getDoubleExtra("latitudeAtual", 0.0);
            double lngAtual = intent.getDoubleExtra("longitudeAtual", 0.0);
            LatLng localAtual = new LatLng(latAtual, lngAtual);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 16));
        } else if (locais.getCount() > 0) {
            locais.moveToFirst();
            double firstLat = locais.getDouble(locais.getColumnIndexOrThrow("lat"));
            double firstLong = locais.getDouble(locais.getColumnIndexOrThrow("lng"));
            LatLng firstCoord = new LatLng(firstLat, firstLong);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstCoord, 16));
        }
        locais.close();
    }
}