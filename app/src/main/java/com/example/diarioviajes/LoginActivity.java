package com.example.diarioviajes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Usuario;

public class LoginActivity extends AppCompatActivity {

    EditText etCorreo, etPassword;
    Button btnLogin, btnRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);

        // Botón Iniciar Sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        // Botón Registrarse
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrarActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLite admin = new AdminSQLite(this);
        Usuario usuario = admin.login(correo, password);

        if (usuario != null) {
            // Guardar sesión del usuario
            SharedPreferences prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("usuarioId", usuario.getId());
            editor.putString("nombre", usuario.getNombre());
            editor.apply();

            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("usuarioId", usuario.getId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}