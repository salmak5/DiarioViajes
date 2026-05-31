package com.example.diarioviajes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diarioviajes.adapter.GastoAdapter;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Gasto;

import java.util.List;

/*
 * Pantalla que muestra todos los gastos
 * relacionados con un viaje.
 */
public class GastosActivity extends AppCompatActivity {

    // RecyclerView donde se muestran los gastos
    RecyclerView rvGastos;

    // Boton para añadir nuevos gastos
    Button btnAñadirGasto;

    // Texto donde se muestra el total gastado
    TextView tvTotalGastos;

    // ID del viaje recibido desde otra activity
    int viajeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta esta clase con el layout XML
        setContentView(R.layout.activity_gastos);
        Button btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> {
            finish(); // vuelve a la pantalla anterior
        });

        // Obtiene el ID del viaje enviado por intent
        viajeId = getIntent().getIntExtra(
                "viajeId",
                -1
        );

        // Inicializacion de componentes
        rvGastos = findViewById(R.id.rvGastos);

        btnAñadirGasto =
                findViewById(R.id.btnAñadirGasto);

        tvTotalGastos =
                findViewById(R.id.tvTotalGastos);

        // Organiza la lista en formato vertical
        rvGastos.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Carga la lista de gastos
        cargarGastos();

        // Boton para añadir un nuevo gasto
        btnAñadirGasto.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            GastosActivity.this,
                            AddGastoActivity.class
                    );

            // Envia el ID del viaje
            intent.putExtra("viajeId", viajeId);

            startActivity(intent);
        });
    }

    /*
     * Carga todos los gastos del viaje
     * desde la base de datos
     */
    private void cargarGastos() {

        // Se usa un hilo secundario para evitar
        // bloquear la interfaz
        new Thread(new Runnable() {

            @Override
            public void run() {

                // Conexion con la base de datos
                AdminSQLite db =
                        new AdminSQLite(GastosActivity.this);

                // Obtiene la lista de gastos
                final List<Gasto> lista =
                        db.listarGastos(viajeId);

                // Obtiene el total gastado
                final double total =
                        db.totalGastos(viajeId);

                // Actualiza la interfaz en el hilo principal
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Muestra el total
                        tvTotalGastos.setText(
                                String.format(
                                        "Total: %.2f€",
                                        total
                                )
                        );

                        // Muestra los gastos en el RecyclerView
                        rvGastos.setAdapter(
                                new GastoAdapter(
                                        lista,
                                        GastosActivity.this
                                )
                        );
                    }
                });
            }
        }).start();
    }

    /*
     * Se ejecuta cuando la pantalla vuelve
     * al primer plano
     *
     * Recarga los gastos para mostrar
     * informacion actualizada
     */
    @Override
    protected void onResume() {

        super.onResume();

        cargarGastos();
    }
}