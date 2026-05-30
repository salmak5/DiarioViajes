package com.example.diarioviajes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Pantalla para editar un viaje existente.
 */
public class EditTripActivity extends AppCompatActivity {

    // Campos del formulario del viaje
    EditText etTitulo, etDestino, etDescripcion, etFechaInicio, etFechaFin;

    // Selector de estado del viaje
    Spinner spinnerEstado;

    // Botones de la pantalla
    Button btnGuardar, btnSeleccionarFoto;

    // Imagen de vista previa de la portada
    ImageView imgPortadaPreview;

    // ID del viaje que se va a editar
    int viajeId;

    // Ruta de la imagen de portada
    private String fotoPortadaPath = null;

    // Launcher para seleccionar imagen desde galeria
    private ActivityResultLauncher<String> fotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta la activity con su layout
        setContentView(R.layout.activity_add_trip);

        // Inicializacion de vistas
        etTitulo = findViewById(R.id.etTitulo);
        etDestino = findViewById(R.id.etDestino);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        imgPortadaPreview = findViewById(R.id.imgPortadaPreview);

        // Recibe el id del viaje desde otra pantalla
        viajeId = getIntent().getIntExtra("viajeId", -1);

        // Inicializa componentes
        inicializarLauncherFoto();
        cargarDatosViaje();
        configurarListeners();
    }

    // Inicializa launcher para seleccionar imagen
    private void inicializarLauncherFoto() {

        fotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        // Copia la imagen al almacenamiento interno
                        fotoPortadaPath = copiarFoto(uri);

                        if (fotoPortadaPath != null) {

                            imgPortadaPreview.setImageURI(
                                    Uri.fromFile(new File(fotoPortadaPath))
                            );

                        } else {

                            Toast.makeText(
                                    this,
                                    "Error al procesar la imagen",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    // Configura los eventos de la pantalla
    private void configurarListeners() {

        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));
        btnSeleccionarFoto.setOnClickListener(v -> fotoLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // Carga los datos actuales del viaje
    private void cargarDatosViaje() {

        AdminSQLite admin = new AdminSQLite(this);
        Viaje v = admin.obtenerViajePorId(viajeId);

        if (v != null) {

            etTitulo.setText(v.getTitulo());
            etDestino.setText(v.getDestino());
            etDescripcion.setText(v.getDescripcion());
            etFechaInicio.setText(v.getFechaInicio());
            etFechaFin.setText(v.getFechaFin());

            fotoPortadaPath = v.getFotoPortada();

            // Configura el spinner de estados
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

            if (v.getEstado() != null) {
                int position = adapter.getPosition(v.getEstado());
                spinnerEstado.setSelection(position);
            }

            // Carga la foto si existe
            if (fotoPortadaPath != null && !fotoPortadaPath.isEmpty()) {

                File file = new File(fotoPortadaPath);

                if (file.exists()) {
                    imgPortadaPreview.setImageURI(Uri.fromFile(file));
                }
            }
        }
    }

    // Muestra calendario para seleccionar fecha
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

    // Valida que la fecha inicio no sea mayor que la fin
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

    // Guarda los cambios del viaje
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

        Viaje v = new Viaje();
        v.setId(viajeId);
        v.setTitulo(titulo);
        v.setDestino(destino);
        v.setDescripcion(descripcion);
        v.setFechaInicio(fechaInicio);
        v.setFechaFin(fechaFin);
        v.setEstado(estado);
        v.setFotoPortada(fotoPortadaPath);

        AdminSQLite db = new AdminSQLite(this);
        boolean ok = db.actualizarViaje(v);

        if (ok) {

            Toast.makeText(
                    this,
                    "Viaje actualizado correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Error al actualizar el viaje",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Copia la imagen seleccionada al almacenamiento interno
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
}