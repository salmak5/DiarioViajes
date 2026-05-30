package com.example.diarioviajes;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
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

/*
 * Pantalla para editar una entrada del diario.
 * Permite modificar titulo, descripcion, fecha y foto.
 */
public class EditEntryActivity extends AppCompatActivity {

    // Campos de texto donde el usuario edita la entrada.
    EditText etTitulo;
    EditText etDescripcion;
    EditText etFecha;

    // Imagen de la entrada.
    ImageView imgFoto;

    // Botones de la pantalla.
    Button btnGuardar;
    Button btnFoto;

    // Texto que muestra el nombre del viaje.
    TextView tvNombreViaje;

    // ID de la entrada a editar.
    int entradaId;

    // ID del viaje asociado.
    int viajeId;

    // Ruta de la foto original.
    String rutaFotoActual = null;

    // Ruta de la nueva foto seleccionada.
    String rutaFotoNueva = null;

    // Objeto viaje para validaciones.
    private Viaje viajeActual;

    // Launcher para seleccionar imagenes.
    private ActivityResultLauncher<String> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta la activity con su layout.
        setContentView(R.layout.activity_add_entry);

        // Recibe datos desde la pantalla anterior.
        entradaId = getIntent().getIntExtra("entradaId", -1);
        viajeId = getIntent().getIntExtra("viajeId", -1);
        rutaFotoActual = getIntent().getStringExtra("foto");

        // Inicializa vistas.
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        imgFoto = findViewById(R.id.imgFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnFoto = findViewById(R.id.btnSeleccionarFoto);
        tvNombreViaje = findViewById(R.id.tvNombreViaje);

        // Configura componentes.
        inicializarLauncher();
        cargarDatosExistentes();
        cargarTituloDelViaje();
        configurarListeners();
    }

    // Inicializa launcher para seleccionar imagen.
    private void inicializarLauncher() {

        launcher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),

                uri -> {

                    if (uri != null) {

                        // Copia la imagen al almacenamiento interno.
                        rutaFotoNueva = copiarImagenALmacenamientoInterno(uri);

                        if (rutaFotoNueva != null) {

                            // Muestra la imagen seleccionada.
                            imgFoto.setImageURI(
                                    Uri.fromFile(new File(rutaFotoNueva))
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

    // Configura los eventos de la pantalla.
    private void configurarListeners() {

        etFecha.setOnClickListener(v -> mostrarCalendario());
        btnFoto.setOnClickListener(v -> launcher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // Carga los datos existentes de la entrada.
    private void cargarDatosExistentes() {

        etTitulo.setText(getIntent().getStringExtra("titulo"));
        etDescripcion.setText(getIntent().getStringExtra("descripcion"));
        etFecha.setText(getIntent().getStringExtra("fecha"));

        // Carga la foto actual si existe.
        if (rutaFotoActual != null && !rutaFotoActual.isEmpty()) {

            File archivo = new File(rutaFotoActual);

            if (archivo.exists()) {

                imgFoto.setImageURI(Uri.fromFile(archivo));
            }
        }
    }

    // Carga el titulo del viaje asociado.
    private void cargarTituloDelViaje() {

        AdminSQLite db = new AdminSQLite(this);

        viajeActual = db.obtenerViajePorId(viajeId);

        if (viajeActual != null) {

            tvNombreViaje.setText("Viaje: " + viajeActual.getTitulo());
        }
    }

    // Valida que la fecha este dentro del rango del viaje.
    private boolean esFechaEnRango(String fechaStr) {

        if (viajeActual == null) return true;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {

            Date fechaEntrada = sdf.parse(fechaStr);
            Date fechaInicio = sdf.parse(viajeActual.getFechaInicio());
            Date fechaFin = sdf.parse(viajeActual.getFechaFin());

            return !fechaEntrada.before(fechaInicio)
                    && !fechaEntrada.after(fechaFin);

        } catch (ParseException e) {

            e.printStackTrace();
            return false;
        }
    }

    // Muestra calendario para seleccionar fecha.
    private void mostrarCalendario() {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, y, m, d) -> {

                    String fecha =
                            y + "-"
                                    + String.format("%02d", m + 1)
                                    + "-"
                                    + String.format("%02d", d);

                    etFecha.setText(fecha);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Copia la imagen al almacenamiento interno.
    private String copiarImagenALmacenamientoInterno(Uri uri) {

        try {

            String nombreArchivo =
                    "entrada_" + System.currentTimeMillis() + ".jpg";

            File archivoDestino =
                    new File(getFilesDir(), nombreArchivo);

            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            FileOutputStream outputStream =
                    new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return archivoDestino.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    // Guarda la entrada actualizada en la base de datos.
    private void guardar() {

        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();

        if (titulo.isEmpty() || fecha.isEmpty()) {

            Toast.makeText(
                    this,
                    "Titulo y fecha son obligatorios",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Valida rango de fechas del viaje.
        if (!esFechaEnRango(fecha)) {

            Toast.makeText(
                    this,
                    "Esta fecha esta fuera del rango de este viaje",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        AdminSQLite db = new AdminSQLite(this);

        EntradaDiario e = new EntradaDiario();

        e.setId(entradaId);
        e.setTitulo(titulo);
        e.setDescripcion(descripcion);
        e.setFecha(fecha);

        // Usa la nueva foto o mantiene la anterior.
        String fotoFinal =
                (rutaFotoNueva != null)
                        ? rutaFotoNueva
                        : rutaFotoActual;

        e.setFoto(fotoFinal);

        boolean ok = db.actualizarEntrada(e);

        if (ok) {

            Toast.makeText(
                    this,
                    "Entrada actualizada correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Error al actualizar",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}