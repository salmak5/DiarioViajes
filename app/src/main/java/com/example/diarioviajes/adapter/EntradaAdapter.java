package com.example.diarioviajes.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.diarioviajes.EditEntryActivity;
import com.example.diarioviajes.R;
import com.example.diarioviajes.TripDetailActivity;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.EntradaDiario;


import java.io.File;
import java.util.List;


//Adaptador encargado de mostrar las entradas del diario dentro de un RecyclerView.
//Su funcion principal es conectar los datos de las entradas con la interfaz grafica que ve el usuario.

public class EntradaAdapter extends RecyclerView.Adapter<EntradaAdapter.ViewHolder> {

    // Lista que contiene todas las entradas del diario.
    private List<EntradaDiario> entradas;
    private Context contexto;

    public EntradaAdapter(List<EntradaDiario> entradas, Context contexto) {
        this.entradas = entradas;
        this.contexto = contexto;
    }


    //Se ejecuta cuando RecyclerView necesita crear una nueva fila.
    //Infla el layout item_entrada.xml.

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_entrada, parent, false);

        return new ViewHolder(vista);
    }


     // Se ejecuta para asociar los datos de una entrada
     //con una fila concreta del RecyclerView.

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Obtener la entrada correspondiente a la posicion actual.
        EntradaDiario entrada = entradas.get(position);

        // Mostrar titulo y fecha.
        holder.titulo.setText(entrada.getTitulo());
        holder.fecha.setText(entrada.getFecha());

        // Mostrar la imagen asociada a la entrada.
        cargarFotoEntrada(holder, entrada.getFoto());

        // Configurar acciones de los botones.
        configurarListeners(holder, entrada, position);
    }

      //Carga la foto almacenada en el dispositivo.
      //Si no existe una imagen valida, se muestra una por defecto.

    private void cargarFotoEntrada(ViewHolder holder, String rutaFoto) {

        // Comprobar que la ruta existe y no esta vacia.
        if (rutaFoto != null && !rutaFoto.isEmpty()) {

            File archivo = new File(rutaFoto);

            // Verificar que el archivo realmente existe.
            if (archivo.exists()) {

                // Mostrar la imagen en el ImageView.
                holder.imgFoto.setImageURI(Uri.fromFile(archivo));

                return;
            }
        }

        // Imagen por defecto cuando no hay foto disponible.
        holder.imgFoto.setImageResource(R.drawable.ic_launcher_foreground);
    }


     //Configura los eventos de los botones de cada fila.
    private void configurarListeners(final ViewHolder holder, final EntradaDiario entrada, final int position) {

        // Boton para modificar la entrada.
        holder.btnModificar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Crear un Intent para abrir la pantalla de edicion.
                Intent intent = new Intent(contexto, EditEntryActivity.class);

                // Enviar los datos de la entrada seleccionada.
                intent.putExtra("entradaId", entrada.getId());
                intent.putExtra("titulo", entrada.getTitulo());
                intent.putExtra("descripcion", entrada.getDescripcion());
                intent.putExtra("fecha", entrada.getFecha());
                intent.putExtra("foto", entrada.getFoto());

                // Abrir la actividad.
                contexto.startActivity(intent);
            }
        });

        // Boton para eliminar la entrada.
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Mostrar ventana de confirmacion.
                mostrarDialogoEliminar(entrada, position);
            }
        });
    }


     //Muestra un dialogo para confirmar la eliminacion.

    private void mostrarDialogoEliminar(final EntradaDiario entrada, final int position) {

        new AlertDialog.Builder(contexto)

                // Titulo del dialogo.
                .setTitle("Eliminar entrada")

                // Mensaje de confirmacion.
                .setMessage("¿Estas seguro de que quieres eliminar esta entrada?")

                // Boton para confirmar.
                .setPositiveButton("Si, eliminar",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                eliminarEntrada(entrada, position);
                            }
                        })

                // Boton para cancelar.
                .setNegativeButton("Cancelar", null)

                // Mostrar dialogo.
                .show();
    }


     // Elimina la entrada de la base de datos.
     // Se ejecuta en un hilo secundario para no bloquear la interfaz.

    private void eliminarEntrada(final EntradaDiario entrada, final int position) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Crear conexion con la base de datos.
                AdminSQLite admin = new AdminSQLite(contexto);

                // Intentar eliminar la entrada.
                final boolean ok = admin.eliminarEntrada(entrada.getId());

                // Volver al hilo principal para actualizar la interfaz.
                ((android.app.Activity) contexto).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Si la eliminacion fue correcta.
                        if (ok) {

                            // Eliminar el elemento de la lista local.
                            if (position != RecyclerView.NO_POSITION) {

                                entradas.remove(position);

                                // Actualizar RecyclerView.
                                notifyItemRemoved(position);
                            }

                            // Mostrar mensaje de exito.
                            Toast.makeText(
                                    contexto,
                                    "Entrada eliminada correctamente",
                                    Toast.LENGTH_SHORT
                            ).show();


                             //Si estamos dentro de TripDetailActivity,
                             //recargar todas las entradas para mantener
                             //la informacion actualizada.

                            if (contexto instanceof TripDetailActivity) {

                                ((TripDetailActivity) contexto)
                                        .cargarEntradas();
                            }

                        } else {

                            // Mostrar error si no se pudo eliminar.
                            Toast.makeText(
                                    contexto,
                                    "Error al eliminar",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
            }
        }).start();
    }


     // Devuelve el numero total de elementos
     // que tiene el RecyclerView.

    @Override
    public int getItemCount() {

        if (entradas != null) {
            return entradas.size();
        } else {
            return 0;
        }
    }

     // ViewHolder:
     // Guarda referencias a los componentes visuales de cada fila.

     // Esto mejora el rendimiento porque evita llamar
     // constantemente a findViewById().

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Componentes visuales de cada item.
        TextView titulo, fecha;
        ImageView imgFoto;
        Button btnModificar, btnEliminar;


         // Constructor del ViewHolder.
         // Obtiene las referencias de los elementos del layout.

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            titulo = itemView.findViewById(R.id.tvTituloEntrada);
            fecha = itemView.findViewById(R.id.tvFechaEntrada);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            btnModificar = itemView.findViewById(R.id.btnModificarEntrada);
            btnEliminar = itemView.findViewById(R.id.btnEliminarEntrada);
        }
    }
}