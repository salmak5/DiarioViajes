package com.example.diarioviajes.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Componentes de AndroidX para RecyclerView.
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.diarioviajes.R;
import com.example.diarioviajes.bbdd.AdminSQLite;
import com.example.diarioviajes.models.Gasto;

import java.util.List;


 //Adaptador encargado de mostrar la lista de gastos dentro de un RecyclerView.

 //Su funcion es conectar los datos de los gastos con la interfaz grafica.

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.ViewHolder> {

    private List<Gasto> gastos;

    private Context contexto;


    public GastoAdapter(List<Gasto> gastos, Context contexto) {
        this.gastos = gastos;
        this.contexto = contexto;
    }
     // Se ejecuta cuando RecyclerView necesita crear una nueva fila de la lista.

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Cargar el layout item_gasto.xml.
        View vista = LayoutInflater.from(contexto)
                .inflate(R.layout.item_gasto, parent, false);

        return new ViewHolder(vista);
    }


     // Asocia los datos de un gasto con una fila concreta.

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Obtener el gasto correspondiente a la posicion actual.
        Gasto gasto = gastos.get(position);

        // Mostrar nombre del gasto.
        holder.nombre.setText(gasto.getNombre());

        // Mostrar categoria del gasto.
        holder.categoria.setText(gasto.getCategoria());

        // Mostrar cantidad con dos decimales y simbolo de euro.
        holder.cantidad.setText(
                String.format("%.2f€", gasto.getCantidad())
        );

        // Configurar acciones de los botones.
        configurarListeners(holder, gasto, position);
    }

     // Configura los eventos de los botones de cada fila.

    private void configurarListeners(final ViewHolder holder,
                                     final Gasto gasto,
                                     final int position) {

        // Boton para eliminar un gasto.
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Mostrar ventana de confirmacion.
                mostrarDialogoEliminar(gasto, position);
            }
        });
    }

    // Muestra un dialogo para confirmar la eliminacion.

    private void mostrarDialogoEliminar(final Gasto gasto,
                                        final int position) {

        new AlertDialog.Builder(contexto)

                // Titulo del dialogo.
                .setTitle("Eliminar gasto")

                // Mensaje de confirmacion.
                .setMessage("¿Estas seguro de que quieres eliminar este gasto?")

                // Boton para confirmar la eliminacion.
                .setPositiveButton("Si, eliminar",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                eliminarGasto(gasto, position);
                            }
                        })

                // Boton para cancelar.
                .setNegativeButton("Cancelar", null)

                // Mostrar dialogo.
                .show();
    }


     //Elimina el gasto de la base de datos.
     // Se ejecuta en un hilo secundario para evitar
     // bloquear la interfaz de usuario.

    private void eliminarGasto(final Gasto gasto,
                               final int position) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Crear conexion con la base de datos.
                AdminSQLite admin = new AdminSQLite(contexto);

                // Intentar eliminar el gasto.
                final boolean ok = admin.eliminarGasto(gasto.getId());

                // Volver al hilo principal para actualizar la interfaz.
                ((android.app.Activity) contexto).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Si la eliminacion fue correcta.
                        if (ok) {

                            // Comprobar que la posicion sigue siendo valida.
                            if (position != RecyclerView.NO_POSITION) {

                                // Eliminar gasto de la lista local.
                                gastos.remove(position);

                                // Actualizar RecyclerView.
                                notifyItemRemoved(position);
                            }

                            // Mostrar mensaje de exito.
                            Toast.makeText(
                                    contexto,
                                    "Gasto eliminado",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            // Mostrar mensaje de error.
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
     // que se mostraran en la lista.

    @Override
    public int getItemCount() {

        if (gastos != null) {
            return gastos.size();
        } else {
            return 0;
        }
    }


     // ViewHolder:
     // Guarda las referencias a los componentes visuales
     // de cada fila del RecyclerView.

     // Esto mejora el rendimiento porque evita llamar


    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Componentes visuales de cada gasto.
        TextView nombre;
        TextView categoria;
        TextView cantidad;
        Button btnEliminar;

        //Obtiene las referencias de los elementos visuales.

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            nombre = itemView.findViewById(R.id.tvNombreGasto);
            categoria = itemView.findViewById(R.id.tvCategoriaGasto);
            cantidad = itemView.findViewById(R.id.tvCantidadGasto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarGasto);
        }
    }
}