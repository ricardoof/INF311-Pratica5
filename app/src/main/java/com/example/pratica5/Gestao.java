package com.example.pratica5;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Gestao extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao);

        // Toolbar
        toolbar = findViewById(R.id.toolbarGestao);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GestãoCheckin");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gestao, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.voltarGestao) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}