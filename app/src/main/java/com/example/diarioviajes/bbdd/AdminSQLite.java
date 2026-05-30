package com.example.diarioviajes.bbdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.diarioviajes.models.EntradaDiario;
import com.example.diarioviajes.models.Gasto;
import com.example.diarioviajes.models.Usuario;
import com.example.diarioviajes.models.Viaje;

import java.util.ArrayList;
import java.util.List;

public class AdminSQLite extends SQLiteOpenHelper {

    private static final String DB_NAME = "diarioviajes.db";
    private static final int DB_VERSION = 2;

    public AdminSQLite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ======================================================
    // ==================== TABLAS ==========================
    // ======================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "correo TEXT UNIQUE," +
                "password TEXT," +
                "foto TEXT)");

        db.execSQL("CREATE TABLE viajes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuarioId INTEGER," +
                "titulo TEXT," +
                "destino TEXT," +
                "descripcion TEXT," +
                "fechaInicio TEXT," +
                "fechaFin TEXT," +
                "estado TEXT," +
                "fotoPortada TEXT)");

        db.execSQL("CREATE TABLE entradas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "viajeId INTEGER," +
                "titulo TEXT," +
                "descripcion TEXT," +
                "fecha TEXT," +
                "foto TEXT)");

        db.execSQL("CREATE TABLE gastos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "viajeId INTEGER," +
                "nombre TEXT," +
                "cantidad REAL," +
                "categoria TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS viajes");
        db.execSQL("DROP TABLE IF EXISTS entradas");
        db.execSQL("DROP TABLE IF EXISTS gastos");
        onCreate(db);
    }

    // ======================================================
    // ==================== USUARIOS ========================
    // ======================================================

    public Usuario login(String correo, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Usuario u = null;

        Cursor c = db.rawQuery(
                "SELECT * FROM usuarios WHERE correo=? AND password=?",
                new String[]{correo, password}
        );

        if (c.moveToFirst()) {
            u = new Usuario();
            u.setId(c.getInt(0));
            u.setNombre(c.getString(1));
            u.setCorreo(c.getString(2));
            u.setContraseña(c.getString(3));
            u.setFoto(c.getString(4));
        }

        c.close();
        return u;
    }
    public boolean registrarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id FROM usuarios WHERE correo=?",
                new String[]{u.getCorreo()}
        );

        if (c.moveToFirst()) {
            c.close();
            return false; // ya existe el correo
        }

        c.close();

        db.execSQL("INSERT INTO usuarios (nombre, correo, password, foto) VALUES (?,?,?,?)",
                new Object[]{
                        u.getNombre(),
                        u.getCorreo(),
                        u.getContraseña(),
                        u.getFoto()
                });

        return true;
    }
    public Usuario obtenerPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Usuario u = null;

        Cursor c = db.rawQuery(
                "SELECT * FROM usuarios WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        if (c.moveToFirst()) {
            u = new Usuario();
            u.setId(c.getInt(0));
            u.setNombre(c.getString(1));
            u.setCorreo(c.getString(2));
            u.setContraseña(c.getString(3));
            u.setFoto(c.getString(4));
        }

        c.close();
        return u;
    }
    public int obtenerIdPorCorreo(String correo) {
        SQLiteDatabase db = getReadableDatabase();
        int id = -1;

        Cursor c = db.rawQuery(
                "SELECT id FROM usuarios WHERE correo = ?",
                new String[]{correo}
        );

        if (c.moveToFirst()) {
            id = c.getInt(0);
        }

        c.close();
        return id;
    }

    // ======================================================
    // ===================== VIAJES =========================
    // ======================================================

    public boolean agregarViaje(Viaje v) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("INSERT INTO viajes (usuarioId, titulo, destino, descripcion, fechaInicio, fechaFin, estado, fotoPortada) VALUES (?,?,?,?,?,?,?,?)",
                new Object[]{
                        v.getUsuarioId(),
                        v.getTitulo(),
                        v.getDestino(),
                        v.getDescripcion(),
                        v.getFechaInicio(),
                        v.getFechaFin(),
                        v.getEstado(),
                        v.getFotoPortada()
                });

        return true;
    }

    public Viaje obtenerViajePorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Viaje v = null;

        Cursor c = db.rawQuery(
                "SELECT * FROM viajes WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        if (c.moveToFirst()) {
            v = new Viaje();
            v.setId(c.getInt(0));
            v.setUsuarioId(c.getInt(1));
            v.setTitulo(c.getString(2));
            v.setDestino(c.getString(3));
            v.setDescripcion(c.getString(4));
            v.setFechaInicio(c.getString(5));
            v.setFechaFin(c.getString(6));
            v.setEstado(c.getString(7));
            v.setFotoPortada(c.getString(8));   // ← NUEVO
        }

        c.close();
        return v;
    }

    public List<Viaje> listarViajes(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Viaje> lista = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT * FROM viajes WHERE usuarioId=?",
                new String[]{String.valueOf(usuarioId)}
        );

        while (c.moveToNext()) {
            Viaje v = new Viaje();
            v.setId(c.getInt(0));
            v.setUsuarioId(c.getInt(1));
            v.setTitulo(c.getString(2));
            v.setDestino(c.getString(3));
            v.setDescripcion(c.getString(4));
            v.setFechaInicio(c.getString(5));
            v.setFechaFin(c.getString(6));
            v.setEstado(c.getString(7));
            v.setFotoPortada(c.getString(8));   // ← NUEVO
            lista.add(v);
        }

        c.close();
        return lista;
    }

    public boolean actualizarViaje(Viaje v) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE viajes SET titulo=?, destino=?, descripcion=?, fechaInicio=?, fechaFin=?, estado=?, fotoPortada=? WHERE id=?",
                new Object[]{
                        v.getTitulo(),
                        v.getDestino(),
                        v.getDescripcion(),
                        v.getFechaInicio(),
                        v.getFechaFin(),
                        v.getEstado(),
                        v.getFotoPortada(),
                        v.getId()
                });

        return true;
    }

    public boolean eliminarViaje(int id) {
        SQLiteDatabase db = getWritableDatabase();

        // borrar entradas del viaje primero
        db.execSQL(
                "DELETE FROM entradas WHERE viajeId=?",
                new Object[]{id}
        );

        // borrar gastos del viaje
        db.execSQL(
                "DELETE FROM gastos WHERE viajeId=?",
                new Object[]{id}
        );

        // borrar viaje
        db.execSQL(
                "DELETE FROM viajes WHERE id=?",
                new Object[]{id}
        );

        return true;
    }
    public boolean actualizarFotoUsuario(int usuarioId, String nuevaRutaFoto) {
        if (usuarioId <= 0) {
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("foto", nuevaRutaFoto);

            int filasActualizadas = db.update(
                    "usuarios",
                    valores,
                    "id = ?",
                    new String[]{String.valueOf(usuarioId)}
            );

            return filasActualizadas > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // ======================================================
    // ==================== ENTRADAS ========================
    // ======================================================

    public int contarEntradasPorViaje(int viajeId) {
        SQLiteDatabase db = getReadableDatabase();
        int total = 0;

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM entradas WHERE viajeId=?",
                new String[]{String.valueOf(viajeId)}
        );

        if (c.moveToFirst()) {
            total = c.getInt(0);
        }

        c.close();
        return total;
    }

    public boolean eliminarEntrada(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(
                "DELETE FROM entradas WHERE id=?",
                new Object[]{id}
        );

        return true;
    }
    public boolean agregarEntrada(EntradaDiario e) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("INSERT INTO entradas (viajeId, titulo, descripcion, fecha, foto) VALUES (?,?,?,?,?)",
                new Object[]{
                        e.getViajeId(),
                        e.getTitulo(),
                        e.getDescripcion(),
                        e.getFecha(),
                        e.getFoto()
                });

        return true;
    }
    public List<EntradaDiario> listarEntradas(int viajeId) {
        SQLiteDatabase db = getReadableDatabase();
        List<EntradaDiario> lista = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT * FROM entradas WHERE viajeId=?",
                new String[]{String.valueOf(viajeId)}
        );

        while (c.moveToNext()) {
            EntradaDiario e = new EntradaDiario();
            e.setId(c.getInt(0));
            e.setViajeId(c.getInt(1));
            e.setTitulo(c.getString(2));
            e.setDescripcion(c.getString(3));
            e.setFecha(c.getString(4));
            e.setFoto(c.getString(5));

            lista.add(e);
        }

        c.close();
        return lista;
    }
    public boolean actualizarEntrada(EntradaDiario e) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE entradas SET titulo=?, descripcion=?, fecha=?, foto=? WHERE id=?",
                new Object[]{
                        e.getTitulo(),
                        e.getDescripcion(),
                        e.getFecha(),
                        e.getFoto(),
                        e.getId()
                });

        return true;
    }

    public List<EntradaDiario> obtenerEntradasPorViaje(int viajeId) {
        SQLiteDatabase db = getReadableDatabase();
        List<EntradaDiario> lista = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT * FROM entradas WHERE viajeId=?",
                new String[]{String.valueOf(viajeId)}
        );

        while (c.moveToNext()) {
            EntradaDiario e = new EntradaDiario();
            e.setId(c.getInt(0));
            e.setViajeId(c.getInt(1));
            e.setTitulo(c.getString(2));
            e.setDescripcion(c.getString(3));
            e.setFecha(c.getString(4));
            e.setFoto(c.getString(5));
            lista.add(e);
        }

        c.close();
        return lista;
    }

    // ======================================================
    // ===================== GASTOS =========================
    // ======================================================

    public boolean agregarGasto(Gasto g) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("INSERT INTO gastos (viajeId, nombre, cantidad, categoria) VALUES (?,?,?,?)",
                new Object[]{
                        g.getViajeId(),
                        g.getNombre(),
                        g.getCantidad(),
                        g.getCategoria()
                });

        return true;
    }

    public List<Gasto> listarGastos(int viajeId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Gasto> lista = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT * FROM gastos WHERE viajeId=?",
                new String[]{String.valueOf(viajeId)}
        );

        while (c.moveToNext()) {
            Gasto g = new Gasto();
            g.setId(c.getInt(0));
            g.setViajeId(c.getInt(1));
            g.setNombre(c.getString(2));
            g.setCantidad(c.getDouble(3));
            g.setCategoria(c.getString(4));

            lista.add(g);
        }

        c.close();
        return lista;
    }
    public double totalGastos(int viajeId) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;

        Cursor c = db.rawQuery(
                "SELECT SUM(cantidad) FROM gastos WHERE viajeId=?",
                new String[]{String.valueOf(viajeId)}
        );

        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }

        c.close();
        return total;
    }
    public boolean eliminarGasto(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM gastos WHERE id=?", new Object[]{id});
        return true;
    }

    public boolean actualizarGasto(Gasto g) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE gastos SET nombre=?, cantidad=?, categoria=? WHERE id=?",
                new Object[]{
                        g.getNombre(),
                        g.getCantidad(),
                        g.getCategoria(),
                        g.getId()
                });

        return true;
    }

    // ======================================================
    // =================== ESTADÍSTICAS =====================
    // ======================================================

    public int contarViajesPorEstado(int usuarioId, String estado) {
        SQLiteDatabase db = getReadableDatabase();
        int total = 0;

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM viajes WHERE usuarioId=? AND estado=?",
                new String[]{String.valueOf(usuarioId), estado}
        );

        if (c.moveToFirst()) {
            total = c.getInt(0);
        }

        c.close();
        return total;
    }

    public int contarTotalEntradas(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        int total = 0;

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM entradas e " +
                        "INNER JOIN viajes v ON e.viajeId = v.id " +
                        "WHERE v.usuarioId=?",
                new String[]{String.valueOf(usuarioId)}
        );

        if (c.moveToFirst()) {
            total = c.getInt(0);
        }

        c.close();
        return total;
    }

    public double totalGastadoGeneral(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;

        Cursor c = db.rawQuery(
                "SELECT SUM(g.cantidad) FROM gastos g " +
                        "INNER JOIN viajes v ON g.viajeId = v.id " +
                        "WHERE v.usuarioId=?",
                new String[]{String.valueOf(usuarioId)}
        );

        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }

        c.close();
        return total;
    }

    public String destinoMasVisitado(int usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        String destino = "Sin datos";

        Cursor c = db.rawQuery(
                "SELECT destino, COUNT(destino) as total " +
                        "FROM viajes WHERE usuarioId=? " +
                        "GROUP BY destino " +
                        "ORDER BY total DESC LIMIT 1",
                new String[]{String.valueOf(usuarioId)}
        );

        if (c.moveToFirst()) {
            destino = c.getString(0);
        }

        c.close();
        return destino;
    }


}