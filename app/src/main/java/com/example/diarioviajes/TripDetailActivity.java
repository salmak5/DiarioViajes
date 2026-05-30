package com.example.diarioviajes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diarioviajes.adapter.EntradaAdapter;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.EntradaDiario;
import com.example.diarioviajes.models.Viaje;

import java.util.List;


public class TripDetailActivity extends AppCompatActivity {

    // RecyclerView donde se muestran las entradas
    RecyclerView rvEntradas;

    // Botones de la pantalla
    Button btnNuevaEntrada;
    Button btnVerGastos;
    Button btnVolver;

    // Texto donde se muestra el total gastado
    TextView tvTotalGastos;

    // Texto donde se muestra el titulo del viaje
    TextView tvTituloViaje;

    // ID del viaje recibido desde otra activity
    int viajeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta esta clase con el layout XML
        setContentView(R.layout.activity_trip_detail);

        // Obtiene el ID del viaje enviado por intent
        viajeId = getIntent().getIntExtra("viajeId", -1);

        // Inicializacion de componentes
        tvTituloViaje = findViewById(R.id.tvTituloViaje);

        rvEntradas = findViewById(R.id.rvEntradas);

        btnNuevaEntrada = findViewById(R.id.btnNuevaEntrada);

        btnVerGastos = findViewById(R.id.btnVerGastos);

        tvTotalGastos = findViewById(R.id.tvTotalGastos);

        btnVolver = findViewById(R.id.btnVolver);

        // Organiza las entradas en vertical
        rvEntradas.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Carga la informacion inicial
        cargarTituloViaje();
        cargarEntradas();
        cargarTotalGastos();

        // Configura los botones
        configurarBotones();
    }

    //Configura los eventos de los botones

    private void configurarBotones() {

        // Boton para crear una nueva entrada
        btnNuevaEntrada.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            TripDetailActivity.this,
                            AddEntryActivity.class
                    );

            // Envia el ID del viaje
            intent.putExtra("viajeId", viajeId);

            startActivity(intent);
        });

        // Boton para abrir la pantalla de gastos
        btnVerGastos.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            TripDetailActivity.this,
                            GastosActivity.class
                    );

            // Envia el ID del viaje
            intent.putExtra("viajeId", viajeId);

            startActivity(intent);
        });

        // Boton para volver a la pantalla anterior
        btnVolver.setOnClickListener(v -> finish());
    }

    // Carga el titulo del viaje desde la base de datos

    private void cargarTituloViaje() {

        AdminSQLite admin = new AdminSQLite(this);

        // Busca el viaje por ID
        Viaje viaje = admin.obtenerViajePorId(viajeId);

        // Comprueba si el viaje existe
        if (viaje != null) {

            // Muestra el titulo en pantalla
            tvTituloViaje.setText(viaje.getTitulo());

            // Cambia el titulo superior de la activity
            setTitle(viaje.getTitulo());

        } else {

            // Mensaje si el viaje no existe
            tvTituloViaje.setText("Viaje no encontrado");
        }
    }


     // Carga todas las entradas del viaje

    public void cargarEntradas() {

        AdminSQLite admin = new AdminSQLite(this);

        // Obtiene la lista de entradas
        List<EntradaDiario> lista =
                admin.listarEntradas(viajeId);

        // Muestra las entradas en el RecyclerView
        rvEntradas.setAdapter(
                new EntradaAdapter(lista, this)
        );
    }

    //Calcula y muestra el total de gastos del viaje

    private void cargarTotalGastos() {

        AdminSQLite admin = new AdminSQLite(this);

        // Obtiene el total gastado
        double total = admin.totalGastos(viajeId);

        // Muestra el total en pantalla
        tvTotalGastos.setText(
                String.format(
                        "💰 Total gastado: %.2f€",
                        total
                )
        );
    }
// Se ejecuta cuando la pantalla vuelve a primer plano
//Recarga los datos actualizados

    @Override
    protected void onResume() {

        super.onResume();

        cargarEntradas();
        cargarTotalGastos();
    }
}