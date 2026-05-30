package com.example.diarioviajes;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Usuario;

public class ProfileActivity extends AppCompatActivity {

    // Textos donde se muestran los datos del usuario.
    TextView tvNombre, tvCorreo;

    // Imagen de perfil del usuario.
    ImageView imgFotoPerfil;

    // Boton para volver atras.
    Button btnVolver;

    // ID del usuario recibido desde otra pantalla.
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta la actividad con su layout XML.
        setContentView(R.layout.activity_profile);

        // Obtiene el ID del usuario enviado por Intent.
        usuarioId = getIntent().getIntExtra("usuarioId", -1);

        // Inicializa los componentes de la interfaz.
        tvNombre = findViewById(R.id.tvNombre);
        tvCorreo = findViewById(R.id.tvCorreo);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        btnVolver = findViewById(R.id.btnVolver);

        // Boton para volver a la pantalla anterior.
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Cierra la pantalla actual.
                finish();
            }
        });

        // Carga los datos del usuario al abrir la pantalla.
        cargarDatosUsuario();
    }

    /**
     * Carga los datos del usuario desde la base de datos.
     */
    private void cargarDatosUsuario() {

        // Comprueba que el ID del usuario sea valido.
        if (usuarioId == -1) return;

        // Conexion con la base de datos.
        AdminSQLite admin = new AdminSQLite(this);

        // Busca el usuario por su ID.
        Usuario usuario = admin.obtenerPorId(usuarioId);

        // Comprueba que el usuario exista.
        if (usuario != null) {

            // Muestra el nombre del usuario o un texto por defecto.
            if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
                tvNombre.setText(usuario.getNombre());
            } else {
                tvNombre.setText("Sin nombre");
            }

            // Muestra el correo del usuario.
            tvCorreo.setText(usuario.getCorreo());

            // Obtiene la foto del usuario.
            String foto = usuario.getFoto();

            // Si existe una foto, la muestra en pantalla.
            if (foto != null && !foto.isEmpty()) {
                imgFotoPerfil.setImageURI(Uri.parse(foto));
            }
        }
    }
}