package com.example.diarioviajes;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.EntradaDiario;
import com.example.diarioviajes.models.Viaje;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddEntryActivity extends AppCompatActivity {

    EditText etTitulo, etDescripcion, etFecha;
    ImageView imgFoto;
    Button btnGuardar, btnFoto;
    TextView tvViaje;

    int viajeId;
    String rutaFoto = null;
    Viaje viajeActual;                    // ← Nuevo

    private ActivityResultLauncher<String> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        viajeId = getIntent().getIntExtra("viajeId", -1);

        // Inicializar vistas
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        imgFoto = findViewById(R.id.imgFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnFoto = findViewById(R.id.btnSeleccionarFoto);
        tvViaje = findViewById(R.id.tvNombreViaje);
        Button btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> {
            finish(); // vuelve a la pantalla anterior
        });

        // Configura el selector de imagenes.
        inicializarLauncher();

        // Carga los datos del viaje seleccionado.
        cargarViaje();

        // Configura los eventos de los botones.
        configurarListeners();
    }

    // Inicializa el launcher para seleccionar imagenes.
    private void inicializarLauncher() {

        launcher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),

                // Se ejecuta cuando el usuario selecciona una imagen.
                uri -> {

                    if (uri != null) {

                        // Guarda una copia de la imagen en el almacenamiento interno.
                        rutaFoto = copiarImagenALmacenamientoInterno(uri);

                        if (rutaFoto != null) {

                            // Muestra la imagen seleccionada.
                            imgFoto.setImageURI(
                                    Uri.fromFile(new File(rutaFoto))
                            );

                        } else {

                            // Muestra un error si no se pudo guardar.
                            Toast.makeText(
                                    this,
                                    "Error al guardar la imagen",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    // Configura los eventos de los componentes de la pantalla.
    private void configurarListeners() {

        // Abre el calendario al pulsar el campo fecha.
        etFecha.setOnClickListener(v -> calendario());

        // Abre la galeria para seleccionar una imagen.
        btnFoto.setOnClickListener(v -> launcher.launch("image/*"));

        // Guarda la entrada cuando se pulsa el boton.
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // Obtiene los datos del viaje desde la base de datos.
    private void cargarViaje() {

        AdminSQLite admin = new AdminSQLite(this);

        // Busca el viaje usando su identificador.
        viajeActual = admin.obtenerViajePorId(viajeId);

        if (viajeActual != null) {

            // Muestra el nombre del viaje en pantalla.
            tvViaje.setText("Viaje: " + viajeActual.getTitulo());
        }
    }

    // Comprueba que la fecha introducida este dentro
    // del rango de fechas del viaje.
    private boolean esFechaEnRango(String fechaStr) {

        // Si no existe un viaje cargado se considera valida.
        if (viajeActual == null) {
            return true;
        }

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd");

        try {

            // Convierte las fechas de texto a objetos Date.
            Date fechaEntrada = sdf.parse(fechaStr);
            Date fechaInicio = sdf.parse(viajeActual.getFechaInicio());
            Date fechaFin = sdf.parse(viajeActual.getFechaFin());

            // Comprueba que la fecha este entre el inicio y el fin del viaje.
            return !fechaEntrada.before(fechaInicio)
                    && !fechaEntrada.after(fechaFin);

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }
    }

    // Muestra un calendario para seleccionar una fecha.
    private void calendario() {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,

                // Se ejecuta cuando el usuario selecciona una fecha.
                (view, y, m, d) -> {

                    String fecha =
                            y + "-"
                                    + String.format("%02d", m + 1)
                                    + "-"
                                    + String.format("%02d", d);

                    // Muestra la fecha seleccionada.
                    etFecha.setText(fecha);
                },

                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)

        ).show();
    }

    // Guarda la entrada en la base de datos.
    private void guardar() {

        // Obtiene los datos introducidos por el usuario.
        String titulo =
                etTitulo.getText().toString().trim();

        String descripcion =
                etDescripcion.getText().toString().trim();

        String fecha =
                etFecha.getText().toString().trim();

        // Comprueba que los campos obligatorios esten completos.
        if (titulo.isEmpty() || fecha.isEmpty()) {

            Toast.makeText(
                    this,
                    "Titulo y fecha son obligatorios",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Comprueba que la fecha pertenece al viaje.
        if (!esFechaEnRango(fecha)) {

            Toast.makeText(
                    this,
                    "Esta fecha esta fuera del rango de este viaje",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        AdminSQLite admin = new AdminSQLite(this);

        // Crea una nueva entrada del diario.
        EntradaDiario e = new EntradaDiario();

        e.setViajeId(viajeId);
        e.setTitulo(titulo);
        e.setDescripcion(descripcion);
        e.setFecha(fecha);
        e.setFoto(rutaFoto);

        // Guarda la entrada en la base de datos.
        boolean ok = admin.agregarEntrada(e);

        if (ok) {

            // Muestra un mensaje de exito.
            Toast.makeText(
                    this,
                    "Entrada guardada correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            // Cierra la pantalla actual.
            finish();

        } else {

            // Muestra un mensaje de error.
            Toast.makeText(
                    this,
                    "Error al guardar",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Copia la imagen seleccionada al almacenamiento interno.
    private String copiarImagenALmacenamientoInterno(Uri uri) {

        try {

            // Genera un nombre unico para la imagen.
            String nombreArchivo =
                    "entrada_"
                            + System.currentTimeMillis()
                            + ".jpg";

            // Crea el archivo donde se guardara la imagen.
            File archivoDestino =
                    new File(getFilesDir(), nombreArchivo);

            // Abre la imagen seleccionada.
            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            // Crea el flujo de salida para guardar la imagen.
            FileOutputStream outputStream =
                    new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[1024];
            int length;

            // Copia el contenido de la imagen.
            while ((length = inputStream.read(buffer)) > 0) {

                outputStream.write(buffer, 0, length);
            }

            // Cierra los flujos utilizados.
            inputStream.close();
            outputStream.close();

            // Devuelve la ruta donde se ha guardado la imagen.
            return archivoDestino.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();

            // Devuelve null si ocurre algun error.
            return null;
        }
    }
}