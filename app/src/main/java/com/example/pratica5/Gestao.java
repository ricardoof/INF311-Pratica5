package com.example.pratica5;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Gestao extends AppCompatActivity {

    private Toolbar toolbar;
    LinearLayout layoutConteudo, layoutDeletar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao);

        toolbar = findViewById(R.id.toolbarGestao);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GestãoCheckin");

        layoutConteudo = findViewById(R.id.layoutConteudoGestao);
        layoutDeletar = findViewById(R.id.layoutDeletarGestao);

        carregarCheckins();
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

    private void carregarCheckins() {
        layoutConteudo.removeAllViews();
        layoutDeletar.removeAllViews();

        BancoDadosSingleton bd = BancoDadosSingleton.getInstance();
        Cursor cursor = bd.buscar("Checkin", new String[]{"Local"}, null, null);

        if (cursor.moveToFirst()) {
            do {
                String nomeLocal = cursor.getString(cursor.getColumnIndexOrThrow("Local"));

                // TextView do nome do local
                TextView txtLocal = new TextView(this);
                txtLocal.setText(nomeLocal);
                txtLocal.setTextSize(16f);
                txtLocal.setPadding(20, 20, 20, 20);
                layoutConteudo.addView(txtLocal);

                // Botão de exclusão
                ImageButton btnExcluir = new ImageButton(this);
                btnExcluir.setImageResource(android.R.drawable.ic_delete);
                btnExcluir.setBackground(null);
                btnExcluir.setTag(nomeLocal);
                btnExcluir.setContentDescription("Excluir " + nomeLocal);
                btnExcluir.setPadding(20, 20, 20, 20);
                layoutDeletar.addView(btnExcluir);

                // Evento de clique
                btnExcluir.setOnClickListener(view -> {
                    String localParaExcluir = (String) view.getTag();
                    new AlertDialog.Builder(Gestao.this)
                            .setTitle("Exclusão")
                            .setMessage("Tem certeza que deseja excluir " + localParaExcluir + "?")
                            .setNegativeButton("NÃO", null)
                            .setPositiveButton("SIM", (dialog, which) -> {
                                bd.deletar("Checkin", "Local = ?", new String[]{localParaExcluir});

                                finish();
                                startActivity(getIntent());
                            })
                            .show();
                });
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}