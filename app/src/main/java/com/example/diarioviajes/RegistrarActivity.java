package com.example.diarioviajes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/*
 * Pantalla encargada del registro de nuevos usuarios
 */
public class RegistrarActivity extends AppCompatActivity {

    EditText etNombre, etCorreo, etPassword;
    Button btnRegistrar, btnSeleccionarFoto, btnVolver;
    ImageView imgFotoPerfil;

    String rutaFoto = null;

    ActivityResultLauncher<String> seleccionarImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnVolver = findViewById(R.id.btnVolver);

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);

        inicializarLauncherImagen();

        btnSeleccionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarImagen.launch("image/*");
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrar();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrarActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void inicializarLauncherImagen() {
        seleccionarImagen = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new androidx.activity.result.ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            rutaFoto = copiarImagen(uri);
                            if (rutaFoto != null) {
                                imgFotoPerfil.setImageURI(
                                        Uri.fromFile(new File(rutaFoto))
                                );
                            }
                        }
                    }
                });
    }

    private String copiarImagen(Uri uri) {
        try {
            String nombreArchivo = "perfil_" + System.currentTimeMillis() + ".jpg";
            File archivo = new File(getFilesDir(), nombreArchivo);

            InputStream input = getContentResolver().openInputStream(uri);
            FileOutputStream output = new FileOutputStream(archivo);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }

            input.close();
            output.close();

            return archivo.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación del correo
        if (!correo.contains("@") || !correo.contains(".")) {
            Toast.makeText(this, "El correo debe contener un '@' y un '.'", Toast.LENGTH_LONG).show();
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContraseña(password);
        usuario.setFoto(rutaFoto);

        AdminSQLite admin = new AdminSQLite(this);

        boolean registrado = admin.registrarUsuario(usuario);

        if (registrado) {
            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
        }
    }
}