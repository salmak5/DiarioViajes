package com.example.diarioviajes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diarioviajes.bbdd.AdminSQLite;

/*
 * Pantalla de estadisticas del usuario.
 * Muestra informacion general sobre
 * viajes, entradas y gastos.
 */
public class EstadisticasActivity extends AppCompatActivity {

    // TextViews para mostrar estadisticas
    TextView tvTotalViajes;
    TextView tvFinalizados;
    TextView tvEnCurso;
    TextView tvPlanificados;

    TextView tvEntradas;
    TextView tvGastado;
    TextView tvDestinoTop;

    // ID del usuario recibido desde otra activity
    int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Conecta esta clase con el layout XML
        setContentView(R.layout.activity_estadisticas);

        // Obtiene el ID del usuario enviado por intent
        usuarioId = getIntent().getIntExtra(
                "usuarioId",
                -1
        );

        // Inicializacion de componentes
        tvTotalViajes =
                findViewById(R.id.tvTotalViajes);

        tvFinalizados =
                findViewById(R.id.tvViajesFinalizados);

        tvEnCurso =
                findViewById(R.id.tvViajesEnCurso);

        tvPlanificados =
                findViewById(R.id.tvViajesPlanificados);

        tvEntradas =
                findViewById(R.id.tvTotalEntradas);

        tvGastado =
                findViewById(R.id.tvTotalGastado);

        tvDestinoTop =
                findViewById(R.id.tvDestinoTop);

        // Carga las estadisticas
        cargarEstadisticas();
    }

    /*
     * Obtiene las estadisticas desde
     * la base de datos y las muestra
     * en pantalla
     */
    private void cargarEstadisticas() {

        // Conexion con la base de datos
        AdminSQLite db = new AdminSQLite(this);

        // Total de viajes del usuario
        int total =
                db.listarViajes(usuarioId).size();

        // Viajes finalizados
        int finalizados =
                db.contarViajesPorEstado(
                        usuarioId,
                        "Finalizado"
                );

        // Viajes en curso
        int enCurso =
                db.contarViajesPorEstado(
                        usuarioId,
                        "En curso"
                );

        // Viajes planificados
        int planificados =
                db.contarViajesPorEstado(
                        usuarioId,
                        "Planificado"
                );

        // Total de entradas del diario
        int entradas =
                db.contarTotalEntradas(usuarioId);

        // Total gastado en todos los viajes
        double gastado =
                db.totalGastadoGeneral(usuarioId);

        // Destino mas visitado
        String destino =
                db.destinoMasVisitado(usuarioId);

        // Mostrar datos en pantalla
        tvTotalViajes.setText(
                String.valueOf(total)
        );

        tvFinalizados.setText(
                String.valueOf(finalizados)
        );

        tvEnCurso.setText(
                String.valueOf(enCurso)
        );

        tvPlanificados.setText(
                String.valueOf(planificados)
        );

        tvEntradas.setText(
                String.valueOf(entradas)
        );

        tvGastado.setText(
                String.format("%.2f€", gastado)
        );

        // Si no hay datos se muestra texto por defecto
        tvDestinoTop.setText(
                destino != null
                        ? destino
                        : "Sin datos"
        );
    }
}