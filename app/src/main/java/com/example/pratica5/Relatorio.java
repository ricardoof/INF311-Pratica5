package com.example.pratica5;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Relatorio extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout layoutConteudoRelatorio, layoutDeletarRelatorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        toolbar = findViewById(R.id.toolbarRelatorio);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Relatório");
        layoutConteudoRelatorio = findViewById(R.id.layoutConteudoRelatorio);
        layoutDeletarRelatorio = findViewById(R.id.layoutDeletarRelatorio);
        carregarRelatorio();
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

    private void carregarRelatorio() {
        layoutConteudoRelatorio.removeAllViews();
        layoutDeletarRelatorio.removeAllViews();
        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        Cursor cursor = bd.buscar("Checkin", new String[]{"Local", "qtdVisitas"}, null, "qtdVisitas DESC");
        if (cursor.moveToFirst()) {
            do {
                String nomeLocal = cursor.getString(cursor.getColumnIndexOrThrow("Local"));
                int visitas = cursor.getInt(cursor.getColumnIndexOrThrow("qtdVisitas"));

                TextView txtLocal = new TextView(this);
                txtLocal.setText(nomeLocal);
                txtLocal.setPadding(50, 15, 10, 10);
                layoutConteudoRelatorio.addView(txtLocal);

                TextView txtVisitas = new TextView(this);
                txtVisitas.setText(String.valueOf(visitas));
                txtVisitas.setTextSize(16f);
                txtVisitas.setPadding(10, 10, 70, 10);
                layoutDeletarRelatorio.addView(txtVisitas);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}