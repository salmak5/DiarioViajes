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

import com.example.diarioviajes.EditTripActivity;
import com.example.diarioviajes.HomeActivity;
import com.example.diarioviajes.R;
import com.example.diarioviajes.TripDetailActivity;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Viaje;

import java.io.File;
import java.util.List;


public class ViajeAdapter extends RecyclerView.Adapter<ViajeAdapter.ViewHolder> {


    private List<Viaje> viajes;


    private Context contexto;



// Recibe la lista de viajes y el contexto de la actividad.
    public ViajeAdapter(List<Viaje> viajes, Context contexto) {
        this.viajes = viajes;
        this.contexto = contexto;
    }

    @NonNull
    @Override

// Crea una nueva fila del RecyclerView utilizando el layout item_viaje.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_viaje, parent, false);
        return new ViewHolder(vista);
    }

    @Override

// Asigna los datos de un viaje a la fila correspondiente.
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Viaje viaje = viajes.get(position);

        // Muestra el titulo del viaje.
        holder.titulo.setText(viaje.getTitulo());

        // Muestra el destino del viaje.
        holder.destino.setText(viaje.getDestino());

        // Configura el estado y su color.
        configurarEstado(holder, viaje.getEstado());

        // Carga la imagen de portada.
        cargarFotoPortada(holder, viaje.getFotoPortada());

        // Configura los eventos de los botones.
        configurarListeners(holder, viaje, position);

        // Carga el numero de entradas y gastos.
        cargarContadores(holder, viaje.getId());
    }

    // Muestra el estado del viaje y cambia el color segun su valor.
    private void configurarEstado(ViewHolder holder, String estado) {

        // Estado por defecto si no existe uno guardado.
        String estadoFinal = "Planificado";

        if (estado != null) {
            estadoFinal = estado;
        }

        holder.estado.setText(estadoFinal);

        // Cambia el color dependiendo del estado.
        switch (estadoFinal) {
            case "Planificado":
                holder.estado.setTextColor(android.graphics.Color.parseColor("#1565C0"));
                break;

            case "En curso":
                holder.estado.setTextColor(android.graphics.Color.parseColor("#E65100"));
                break;

            case "Finalizado":
                holder.estado.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                break;
        }
    }

    // Carga la imagen de portada almacenada en el dispositivo.
    private void cargarFotoPortada(ViewHolder holder, String rutaFoto) {

        // Comprueba que existe una ruta valida.
        if (rutaFoto != null && !rutaFoto.isEmpty()) {

            File archivo = new File(rutaFoto);

            // Verifica que el archivo existe.
            if (archivo.exists()) {

                // Muestra la imagen encontrada.
                holder.imgPortada.setImageURI(Uri.fromFile(archivo));
                return;
            }
        }

        // Muestra una imagen por defecto si no existe foto.
        holder.imgPortada.setImageResource(R.drawable.travel);
    }

    // Configura los eventos de los botones y la fila.
    private void configurarListeners(final ViewHolder holder,
                                     final Viaje viaje,
                                     final int position) {

        // Al pulsar la fila se abre el detalle del viaje.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Crea el Intent para abrir la pantalla de detalle.
                Intent intent = new Intent(contexto, TripDetailActivity.class);

                // Envia el id del viaje seleccionado.
                intent.putExtra("viajeId", viaje.getId());

                // Abre la actividad.
                contexto.startActivity(intent);
            }
        });

        // Boton para modificar el viaje.
        holder.btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abre la pantalla de edicion.
                Intent intent = new Intent(contexto, EditTripActivity.class);

                // Envia todos los datos del viaje.
                intent.putExtra("viajeId", viaje.getId());
                intent.putExtra("titulo", viaje.getTitulo());
                intent.putExtra("destino", viaje.getDestino());
                intent.putExtra("descripcion", viaje.getDescripcion());
                intent.putExtra("fechaInicio", viaje.getFechaInicio());
                intent.putExtra("fechaFin", viaje.getFechaFin());
                intent.putExtra("estado", viaje.getEstado());
                intent.putExtra("fotoPortada", viaje.getFotoPortada());

                // Abre la actividad de edicion.
                contexto.startActivity(intent);
            }
        });

        // Boton para eliminar el viaje.
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Muestra una confirmacion antes de eliminar.
                mostrarDialogoEliminar(viaje, position);
            }
        });
    }

    // Muestra un dialogo para confirmar la eliminacion.
    private void mostrarDialogoEliminar(final Viaje viaje, final int position) {

        new AlertDialog.Builder(contexto)
                .setTitle("Eliminar viaje")
                .setMessage("¿Estas seguro de que quieres eliminar este viaje? Se borraran tambien todas sus entradas y gastos.")
                .setPositiveButton("Si, eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Elimina el viaje si el usuario confirma.
                        eliminarViaje(viaje, position);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Elimina el viaje de la base de datos.
    private void eliminarViaje(final Viaje viaje, final int position) {

        // Ejecuta la operacion en segundo plano.
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Conexion con la base de datos.
                AdminSQLite admin = new AdminSQLite(contexto);

                // Elimina el viaje usando su id.
                final boolean ok = admin.eliminarViaje(viaje.getId());

                // Actualiza la interfaz desde el hilo principal.
                ((android.app.Activity) contexto).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Si la eliminacion fue correcta.
                        if (ok) {

                            if (position != RecyclerView.NO_POSITION) {

                                // Elimina el elemento de la lista.
                                viajes.remove(position);

                                // Actualiza el RecyclerView.
                                notifyItemRemoved(position);
                            }

                            // Muestra un mensaje de exito.
                            Toast.makeText(
                                    contexto,
                                    "Viaje eliminado correctamente",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Recarga la lista de viajes.
                            if (contexto instanceof HomeActivity) {
                                ((HomeActivity) contexto).cargarViajes();
                            }

                        } else {

                            // Muestra un mensaje de error.
                            Toast.makeText(
                                    contexto,
                                    "Error al eliminar el viaje",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
            }
        }).start();
    }

    // Obtiene el numero de entradas y el gasto total del viaje.
    private void cargarContadores(final ViewHolder holder, final int viajeId) {

        // Ejecuta la consulta en segundo plano.
        new Thread(new Runnable() {
            @Override
            public void run() {

                AdminSQLite admin = new AdminSQLite(contexto);

                // Cuenta las entradas asociadas al viaje.
                final int totalEntradas =
                        admin.contarEntradasPorViaje(viajeId);

                // Calcula el gasto total del viaje.
                final double totalGastos =
                        admin.totalGastos(viajeId);

                // Actualiza los datos en pantalla.
                ((android.app.Activity) contexto).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        holder.contadorEntradas.setText(
                                "📖 " + totalEntradas + " entradas"
                        );

                        holder.totalGastos.setText(
                                String.format(
                                        "💰 %.2f€ gastados",
                                        totalGastos
                                )
                        );
                    }
                });
            }
        }).start();
    }

    @Override

// Devuelve el numero total de viajes almacenados.
    public int getItemCount() {
        if (viajes != null) {
            return viajes.size();
        } else {
            return 0;
        }
    }

    /**
     * ViewHolder: Guarda las referencias de las vistas de cada item.
     */

// Guarda las referencias a los componentes visuales
// para mejorar el rendimiento del RecyclerView.
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Imagen de portada del viaje.
        ImageView imgPortada;

        // Textos mostrados en la tarjeta.
        TextView titulo, destino, estado, contadorEntradas, totalGastos;

        // Botones de accion.
        Button btnModificar, btnEliminar;

        // Obtiene las referencias de los elementos del layout.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPortada       = itemView.findViewById(R.id.imgPortadaViaje);
            titulo           = itemView.findViewById(R.id.tvTituloViaje);
            destino          = itemView.findViewById(R.id.tvDestinoViaje);
            estado           = itemView.findViewById(R.id.tvEstadoViaje);
            contadorEntradas = itemView.findViewById(R.id.tvContadorEntradas);
            totalGastos      = itemView.findViewById(R.id.tvTotalGastosViaje);
            btnModificar     = itemView.findViewById(R.id.btnModificar);
            btnEliminar      = itemView.findViewById(R.id.btnEliminar);
        }
    }
}