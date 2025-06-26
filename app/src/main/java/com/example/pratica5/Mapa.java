package com.example.pratica5;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

        Log.d("MAPA", "Entrei na tela do mapa");

        // Toolbar
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
        if (id == R.id.voltar) {
            Toast.makeText(this, "Mapa de check-in", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.gestaoDeCheckIn) {
            Toast.makeText(this, "Gestão de check-in", Toast.LENGTH_SHORT).show();
            Intent gestao = new Intent(getBaseContext(), Mapa.class);
            startActivity(gestao);
            return true;
        } else if (id == R.id.lugaresMaisVisitados) {
            Toast.makeText(this, "Lugares mais visitados", Toast.LENGTH_SHORT).show();
            Intent lugares = new Intent(getBaseContext(), Mapa.class);
            startActivity(lugares);
            return true;
        } else if (id == R.id.tiposDeMapa) {
            Toast.makeText(this, "Tipos de mapas", Toast.LENGTH_SHORT).show();
            Intent tipos = new Intent(getBaseContext(), Mapa.class);
            startActivity(tipos);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d("MAPA", "MAPA");
        map = googleMap;
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        //locais = bd.buscar("Checkin", new String[]{"Local", "qtdVisitas", "cat", "latitude", "longitude"}, null, null);
        locais = bd.buscar("Checkin ch, Categoria cat", new String[]{"ch.Local Local",
                        "ch.latitude lat",
                        "ch.longitude lng",
                        "ch.qtdVisitas visitas",
                        "cat.nome nomeCategoria"
                }, "ch.cat = cat.idCategoria", null);

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

        if (locais.getCount() > 0) {
            locais.moveToFirst();
            double firstLat = locais.getDouble(locais.getColumnIndexOrThrow("lat"));
            double firstLong = locais.getDouble(locais.getColumnIndexOrThrow("lng"));
            LatLng firstCoord = new LatLng(firstLat, firstLong);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstCoord, 16));
        }

        locais.close();
    }
}