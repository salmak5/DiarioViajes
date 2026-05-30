package com.example.diarioviajes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Viaje;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 * Pantalla para crear un nuevo viaje.
 */
public class AddTripActivity extends AppCompatActivity {

    // Campos del formulario de viaje.
    EditText etTitulo, etDestino, etDescripcion, etFechaInicio, etFechaFin;

    // Selector de estado del viaje.
    Spinner spinnerEstado;

    // Botones de acciones principales.
    Button btnGuardar, btnBuscarLugar, btnSeleccionarFoto;

    // Imagen de vista previa del viaje.
    ImageView imgPortadaPreview;

    // ID del usuario que crea el viaje.
    int usuarioId;

    // Ruta de la imagen seleccionada.
    private String fotoPortadaPath = null;

    // Launchers para resultados externos.
    private ActivityResultLauncher<Intent> placesLauncher;
    private ActivityResultLauncher<String> fotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta la actividad con su layout.
        setContentView(R.layout.activity_add_trip);

        // Inicializa Google Places si no esta activo.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "TU_API_KEY");
        }

        // Obtiene el id del usuario.
        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        // Inicializa vistas del layout.
        etTitulo = findViewById(R.id.etTitulo);
        etDestino = findViewById(R.id.etDestino);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscarLugar = findViewById(R.id.btnBuscarLugar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        imgPortadaPreview = findViewById(R.id.imgPortadaPreview);

        // Inicializa componentes auxiliares.
        inicializarLaunchers();
        cargarSpinnerEstados();
        configurarListeners();
    }

    // Inicializa los launchers para galeria y Google Places.
    private void inicializarLaunchers() {

        // Launcher de Google Places.
        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        try {

                            Place place = Autocomplete.getPlaceFromIntent(result.getData());

                            StringBuilder destino = new StringBuilder();

                            if (place.getName() != null) {
                                destino.append(place.getName());
                            }

                            if (place.getAddress() != null) {
                                if (destino.length() > 0) destino.append(" - ");
                                destino.append(place.getAddress());
                            }

                            etDestino.setText(destino.toString());

                        } catch (Exception e) {

                            Toast.makeText(this, "Error al obtener el lugar", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Launcher para seleccionar imagen.
        fotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        // Copia la imagen al almacenamiento interno.
                        fotoPortadaPath = copiarFoto(uri);

                        if (fotoPortadaPath != null) {

                            // Muestra la imagen seleccionada.
                            imgPortadaPreview.setImageURI(
                                    Uri.fromFile(new File(fotoPortadaPath))
                            );

                        } else {

                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Configura los eventos de la pantalla.
    private void configurarListeners() {

        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));
        btnBuscarLugar.setOnClickListener(v -> abrirGooglePlaces());
        btnSeleccionarFoto.setOnClickListener(v -> fotoLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // Carga las opciones del spinner de estados.
    private void cargarSpinnerEstados() {

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.estados_viaje,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerEstado.setAdapter(adapter);
        spinnerEstado.setSelection(0);
    }

    // Muestra calendario para seleccionar fecha.
    private void mostrarCalendario(final EditText campo) {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, y, m, d) ->
                        campo.setText(
                                y + "-"
                                        + String.format("%02d", m + 1)
                                        + "-"
                                        + String.format("%02d", d)
                        ),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Valida que la fecha inicio no sea mayor que la fecha fin.
    private boolean esRangoFechasValido(String fechaInicio, String fechaFin) {

        if (TextUtils.isEmpty(fechaInicio) || TextUtils.isEmpty(fechaFin)) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {

            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaFin);

            return !inicio.after(fin);

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }
    }

    // Guarda el viaje en la base de datos.
    private void guardar() {

        String titulo = etTitulo.getText().toString().trim();
        String destino = etDestino.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fechaInicio = etFechaInicio.getText().toString().trim();
        String fechaFin = etFechaFin.getText().toString().trim();

        String estado = spinnerEstado.getSelectedItem() != null
                ? spinnerEstado.getSelectedItem().toString()
                : "Planificado";

        if (TextUtils.isEmpty(titulo)
                || TextUtils.isEmpty(destino)
                || TextUtils.isEmpty(fechaInicio)) {

            Toast.makeText(
                    this,
                    "Titulo, destino y fecha de inicio son obligatorios",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!esRangoFechasValido(fechaInicio, fechaFin)) {

            Toast.makeText(
                    this,
                    "La fecha de ida no puede ser posterior a la vuelta",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (usuarioId == -1) {

            Toast.makeText(
                    this,
                    "Error: Usuario no identificado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        AdminSQLite admin = new AdminSQLite(this);

        Viaje v = new Viaje();
        v.setUsuarioId(usuarioId);
        v.setTitulo(titulo);
        v.setDestino(destino);
        v.setDescripcion(descripcion);
        v.setFechaInicio(fechaInicio);
        v.setFechaFin(fechaFin);
        v.setEstado(estado);
        v.setFotoPortada(fotoPortadaPath);

        boolean ok = admin.agregarViaje(v);

        if (ok) {

            Toast.makeText(
                    this,
                    "Viaje creado correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Error al guardar el viaje",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Copia la imagen seleccionada al almacenamiento interno.
    private String copiarFoto(Uri uri) {

        try {

            InputStream input =
                    getContentResolver().openInputStream(uri);

            File file =
                    new File(
                            getFilesDir(),
                            "portada_" + System.currentTimeMillis() + ".jpg"
                    );

            FileOutputStream output =
                    new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            input.close();
            output.close();

            return file.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    // Abre el selector de Google Places.
    private void abrirGooglePlaces() {

        try {

            List<Place.Field> fields =
                    Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS);

            Intent intent =
                    new Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.FULLSCREEN,
                            fields
                    ).build(this);

            placesLauncher.launch(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Error al abrir Google Places",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}