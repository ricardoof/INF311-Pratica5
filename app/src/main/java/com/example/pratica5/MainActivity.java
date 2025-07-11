package com.example.pratica5;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private FusedLocationProviderClient posicionamentoGlobal;
    private TextView latitude, longitude;
    private LocationCallback locationCallback;
    private double latitudeAtual, longitudeAtual;
    private Toolbar toolbar;
    private AutoCompleteTextView nomeDoLocal;
    private ContentValues valores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbarTelaPrincipal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CheckInLocais");

        nomeDoLocal = findViewById(R.id.edtNomeDoLocal);
        List<String> locaisSalvos = buscarLocaisSalvos();
        ArrayAdapter<String> adapterLocais = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locaisSalvos);
        nomeDoLocal.setThreshold(2);
        nomeDoLocal.setAdapter(adapterLocais);

        spinner = findViewById(R.id.spinnerCategoriaDoLocal);
        List<String> categorias = buscarCategorias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        posicionamentoGlobal = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    latitudeAtual = location.getLatitude();
                    longitudeAtual = location.getLongitude();
                    latitude.setText("Latitude:     " + latitudeAtual);
                    longitude.setText("Longitude:   " + longitudeAtual);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            posicionamentoGlobal.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    public List<String> buscarCategorias() {
        List<String> categorias = new ArrayList<>();
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        Cursor cursor = bd.buscar("Categoria", new String[]{"nome"}, null, "nome");
        if (cursor.moveToFirst()) {
            do {
                categorias.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categorias;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tela_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mapaDeCheckIn) {
            if (latitudeAtual == 0.0 && longitudeAtual == 0.0) {
                Toast.makeText(this, "Aguardando localização atual...", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent mapa = new Intent(getBaseContext(), Mapa.class);
            mapa.putExtra("latitudeAtual", latitudeAtual);
            mapa.putExtra("longitudeAtual", longitudeAtual);
            startActivity(mapa);
            return true;
        } else if (id == R.id.gestaoDeCheckIn) {
            Intent gestao = new Intent(getBaseContext(), Gestao.class);
            startActivity(gestao);
            return true;
        } else if (id == R.id.lugaresMaisVisitados) {
            Intent lugares = new Intent(getBaseContext(), Relatorio.class);
            startActivity(lugares);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkIn(View v) {
        nomeDoLocal = findViewById(R.id.edtNomeDoLocal);
        String Local = nomeDoLocal.getText().toString();
        spinner.getSelectedItem().toString();

        if (Local.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome do local.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinner.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Por favor, selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitudeAtual == 0.0 && longitudeAtual == 0.0) {
            Toast.makeText(this, "Aguarde o GPS obter sua localização.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        String nomeCategoria = spinner.getSelectedItem().toString();
        Cursor c = bd.buscar("Categoria", new String[]{"idCategoria"}, "nome = '" + nomeCategoria + "'", null);
        int categoriaId = -1;
        if (c.moveToFirst()) {
            categoriaId = c.getInt(c.getColumnIndexOrThrow("idCategoria"));
        }
        c.close();

        Cursor cursor = bd.buscar("Checkin", new String[]{"Local", "qtdVisitas", "cat", "latitude", "longitude"}, "Local = '" + Local + "'", null);
        valores = new ContentValues();

        if (cursor.moveToFirst()){
            int qtdVisitasAtual = cursor.getInt(cursor.getColumnIndexOrThrow("qtdVisitas"));
            valores.put("qtdVisitas", qtdVisitasAtual + 1);
            bd.atualizar("Checkin", valores, "Local = '" + Local + "'");
            Toast.makeText(this, "Check-in atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            valores.put("Local", Local);
            valores.put("qtdVisitas", 1);
            valores.put("cat", categoriaId);
            valores.put("latitude", latitudeAtual);
            valores.put("longitude", longitudeAtual);
            bd.inserir("Checkin", valores);
            Toast.makeText(this, "Check-in realizado!", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        finish();
        startActivity(getIntent());
    }

    private List<String> buscarLocaisSalvos() {
        List<String> locais = new ArrayList<>();
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        Cursor cursor = bd.buscar("Checkin", new String[]{"Local"}, null, "Local");
        if (cursor.moveToFirst()) {
            do {
                locais.add(cursor.getString(cursor.getColumnIndexOrThrow("Local")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return locais;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        posicionamentoGlobal.removeLocationUpdates(locationCallback);
    }
}