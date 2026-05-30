package com.example.diarioviajes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diarioviajes.adapter.ViajeAdapter;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Usuario;
import com.example.diarioviajes.models.Viaje;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView rvViajes;


    Button btnNuevoViaje;


    EditText etBuscador;


    TextView tvNombreUsuario;


    ImageView imgFotoUsuario;


    int usuarioId;


    List<Viaje> todosLosViajes = new ArrayList<>();

    // Launcher para seleccionar una imagen desde la galeria
    private ActivityResultLauncher<String> fotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);

        // Configuracion de la barra superior
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Cambia el titulo de la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Diario de Viajes");
        }

        // Inicializacion de componentes
        rvViajes = findViewById(R.id.rvViajes);
        btnNuevoViaje = findViewById(R.id.btnNuevoViaje);
        etBuscador = findViewById(R.id.etBuscador);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        imgFotoUsuario = findViewById(R.id.imgFotoUsuario);

        // Obtiene el ID del usuario enviado desde otra activity
        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        // Organiza los elementos del RecyclerView en vertical
        rvViajes.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa el selector de imagenes
        inicializarLauncherFoto();

        // Carga informacion del usuario y sus viajes
        cargarDatosUsuario();
        cargarViajes();

        // Configura todos los eventos de botones y buscador
        configurarListeners();
    }

    // Inicializa el launcher para abrir la galeria
     // y seleccionar una imagen

    private void inicializarLauncherFoto() {

        fotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),

                new androidx.activity.result.ActivityResultCallback<Uri>() {

                    @Override
                    public void onActivityResult(Uri uri) {

                        // Comprueba si el usuario selecciono una imagen
                        if (uri != null) {

                            cambiarFotoPerfil(uri);
                        }
                    }
                });
    }

    //Configura todos los listeners de la pantalla

    private void configurarListeners() {

        // Boton para crear un nuevo viaje
        btnNuevoViaje.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent =
                        new Intent(HomeActivity.this,
                                AddTripActivity.class);

                // Envia el ID del usuario
                intent.putExtra("usuarioId", usuarioId);

                startActivity(intent);
            }
        });

        // Buscador en tiempo real
        etBuscador.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                // Filtra los viajes mientras el usuario escribe
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Click sobre la foto de perfil
        imgFotoUsuario.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Abre la galeria
                fotoLauncher.launch("image/*");
            }
        });

        // Click sobre el nombre del usuario
        tvNombreUsuario.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                abrirPerfil();
            }
        });
    }

    //Cambia la foto de perfil del usuario

    private void cambiarFotoPerfil(Uri uri) {

        // Copia la imagen y obtiene la nueva ruta
        String nuevaRuta = copiarImagen(uri);

        if (nuevaRuta != null) {

            AdminSQLite admin = new AdminSQLite(this);

            // Actualiza la foto en la base de datos
            boolean ok =
                    admin.actualizarFotoUsuario(usuarioId, nuevaRuta);

            if (ok) {

                // Muestra la nueva imagen
                imgFotoUsuario.setImageURI(
                        Uri.fromFile(new File(nuevaRuta))
                );

                Toast.makeText(
                        this,
                        "Foto actualizada correctamente",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Error al guardar la foto",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            Toast.makeText(
                    this,
                    "Error al procesar la imagen",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Copia la imagen seleccionada
     // en la carpeta privada de la app

    private String copiarImagen(Uri uri) {

        try {

            // Nombre unico para la imagen
            String nombreArchivo =
                    "perfil_" + System.currentTimeMillis() + ".jpg";

            // Archivo donde se guardara la imagen
            File archivo =
                    new File(getFilesDir(), nombreArchivo);

            // Flujo de lectura
            InputStream input =
                    getContentResolver().openInputStream(uri);

            // Flujo de escritura
            FileOutputStream output =
                    new FileOutputStream(archivo);

            // Buffer para copiar datos
            byte[] buffer = new byte[1024];

            int len;

            // Copia la imagen por bloques
            while ((len = input.read(buffer)) > 0) {

                output.write(buffer, 0, len);
            }

            // Cierra los flujos
            input.close();
            output.close();

            // Devuelve la ruta de la imagen
            return archivo.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    /*
     * Carga el nombre y la foto del usuario
     */
    private void cargarDatosUsuario() {

        // Comprueba si el ID es valido
        if (usuarioId == -1) return;

        AdminSQLite admin = new AdminSQLite(this);

        // Busca el usuario en la base de datos
        Usuario usuario =
                admin.obtenerPorId(usuarioId);

        if (usuario != null) {

            String nombre = usuario.getNombre();

            // Muestra el nombre o "Usuario" por defecto
            tvNombreUsuario.setText(
                    nombre != null && !nombre.isEmpty()
                            ? nombre
                            : "Usuario"
            );

            // Carga la foto si existe
            String fotoPath = usuario.getFoto();

            if (fotoPath != null && !fotoPath.isEmpty()) {

                File file = new File(fotoPath);

                if (file.exists()) {

                    imgFotoUsuario.setImageURI(
                            Uri.fromFile(file)
                    );
                }
            }
        }
    }

    //Abre la pantalla de perfil

    private void abrirPerfil() {

        Intent intent =
                new Intent(HomeActivity.this,
                        ProfileActivity.class);

        intent.putExtra("usuarioId", usuarioId);

        startActivity(intent);
    }

    // Filtra los viajes segun el texto escrito
     // Busca coincidencias en titulo o destino

    private void filtrar(String texto) {

        // Lista temporal con los viajes filtrados
        List<Viaje> filtrados = new ArrayList<>();

        for (Viaje v : todosLosViajes) {

            // Comprueba si coincide el titulo
            boolean coincideTitulo =
                    v.getTitulo()
                            .toLowerCase()
                            .contains(texto.toLowerCase());

            // Comprueba si coincide el destino
            boolean coincideDestino =
                    v.getDestino()
                            .toLowerCase()
                            .contains(texto.toLowerCase());

            // Si coincide alguno se añade a la lista
            if (coincideTitulo || coincideDestino) {

                filtrados.add(v);
            }
        }

        // Actualiza el RecyclerView con la lista filtrada
        rvViajes.setAdapter(
                new ViajeAdapter(filtrados, this)
        );
    }

    // Carga todos los viajes del usuario

    public void cargarViajes() {

        AdminSQLite admin = new AdminSQLite(this);

        // Obtiene los viajes desde la base de datos
        todosLosViajes =
                admin.listarViajes(usuarioId);

        // Muestra los viajes en pantalla
        rvViajes.setAdapter(
                new ViajeAdapter(todosLosViajes, this)
        );
    }

    // Se ejecuta cuando la activity vuelve a primer plano
     // Recarga la informacion actualizada

    @Override
    protected void onResume() {

        super.onResume();

        cargarViajes();
        cargarDatosUsuario();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(
                R.menu.menu_home,
                menu
        );

        return true;
    }

    // Gestiona las opciones seleccionadas del menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Opcion cerrar sesion
        if (item.getItemId() == R.id.menuCerrarSesion) {

            cerrarSesion();

            return true;
        }

        // Opcion estadisticas
        if (item.getItemId() == R.id.menuEstadisticas) {

            Intent intent =
                    new Intent(HomeActivity.this,
                            EstadisticasActivity.class);

            intent.putExtra("usuarioId", usuarioId);

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Cierra la sesion y vuelve al login

    private void cerrarSesion() {

        Intent intent =
                new Intent(HomeActivity.this,
                        MainActivity.class);

        // Elimina las pantallas anteriores
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }
}