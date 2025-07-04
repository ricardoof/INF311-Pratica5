package com.example.pratica5;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

        Log.d("MainActivity", "entrei no onCreate");

        // Toolbar
        toolbar = findViewById(R.id.toolbarTelaPrincipal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CheckInLocais");

        // Autocomplete
        nomeDoLocal = findViewById(R.id.edtNomeDoLocal);
        List<String> locaisSalvos = buscarLocaisSalvos();
        ArrayAdapter<String> adapterLocais = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locaisSalvos);
        nomeDoLocal.setThreshold(2);
        nomeDoLocal.setAdapter(adapterLocais);

        // Menu de categorias
        spinner = findViewById(R.id.spinnerCategoriaDoLocal);
        List<String> categorias = buscarCategorias();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Posicionamento global
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
            Intent mapa = new Intent(getBaseContext(), Mapa.class);
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

        // Validação: Nome do local
        if (Local.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome do local.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação: Categoria
        if (spinner.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Por favor, selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação: Latitude e Longitude
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

        // Se o local já existe no bd atualiza o qtdVisitas
        if (cursor.moveToFirst()){
            int qtdVisitasAtual = cursor.getInt(cursor.getColumnIndexOrThrow("qtdVisitas"));
            valores.put("qtdVisitas", qtdVisitasAtual + 1);
            bd.atualizar("Checkin", valores, "Local = '" + Local + "'");
            Toast.makeText(this, "Check-in atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            // -20.756130344704516, -42.87517139116986 fazendiha
            //-20.757293764873097, -42.87511449290152  ufv
            // -20.76061471367571, -42.86852732141032 ru1
            // -20.761858702321916, -42.86985769702954 mu
            // -20.760559731284303, -42.86758498853464 pva
            // -20.763560397840216, -42.86630288141098 pvb
            valores.put("Local", Local);
            valores.put("qtdVisitas", 1);
            valores.put("cat", categoriaId);
            valores.put("latitude", -20.760559731284303);
            valores.put("longitude", -42.86758498853464);
            bd.inserir("Checkin", valores);
            Toast.makeText(this, "Check-in realizado!", Toast.LENGTH_SHORT).show();
        }
        cursor.close();

        // Recarregar a tela principal
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