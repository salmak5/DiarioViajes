package com.example.diarioviajes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Gasto;
import com.example.diarioviajes.models.Viaje;

public class AddGastoActivity extends AppCompatActivity {

    EditText etNombre, etCantidad;
    Spinner spinnerCategoria;
    TextView tvViaje;
    Button btnGuardar;

    int viajeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gasto);

        viajeId = getIntent().getIntExtra("viajeId", -1);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etCantidad = findViewById(R.id.etCantidad);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        tvViaje = findViewById(R.id.tvNombreViaje);
        btnGuardar = findViewById(R.id.btnGuardar);
        Button btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> {
            finish(); // vuelve a la pantalla anterior
        });

        // Cargar datos
        cargarSpinnerCategoria();
        cargarViaje();

        // Configurar listener del botón
        configurarListeners();
    }

    private void configurarListeners() {
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarGasto();
            }
        });
    }

    private void cargarSpinnerCategoria() {
        // Como el array está en arrays2.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_gasto,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        // Seleccionar "Otros" por defecto
        spinnerCategoria.setSelection(4);
    }

    private void cargarViaje() {
        AdminSQLite admin = new AdminSQLite(this);
        Viaje v = admin.obtenerViajePorId(viajeId);

        if (v != null) {
            tvViaje.setText("Gasto del viaje: " + v.getTitulo());
        }
    }

    private void guardarGasto() {

        String nombre = etNombre.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Introduce un nombre para el gasto");
            return;
        }
        if (TextUtils.isEmpty(cantidadStr)) {
            etCantidad.setError("Introduce la cantidad");
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantidadStr);
            if (cantidad <= 0) {
                etCantidad.setError("La cantidad debe ser mayor que 0");
                return;
            }
        } catch (NumberFormatException e) {
            etCantidad.setError("Introduce un número válido");
            return;
        }

        // Validación del Spinner
        if (spinnerCategoria.getSelectedItem() == null) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLite admin = new AdminSQLite(this);

        Gasto g = new Gasto();
        g.setViajeId(viajeId);
        g.setNombre(nombre);
        g.setCantidad(cantidad);
        g.setCategoria(spinnerCategoria.getSelectedItem().toString());

        boolean ok = admin.agregarGasto(g);

        if (ok) {
            Toast.makeText(this, "Gasto añadido correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar el gasto", Toast.LENGTH_SHORT).show();
        }
    }
}