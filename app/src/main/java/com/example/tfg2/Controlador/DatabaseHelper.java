package com.example.tfg2.Controlador;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tfg2.Modelo.Modelo;

import java.util.ArrayList;
import java.util.List;
/**
* Clase auxiliar para interactuar con la base de datos SQLite.
*/
public class DatabaseHelper extends SQLiteOpenHelper {
    // Nombre de la base de datos
    private static final String DATABASE_NAME = "VTDB.db";
    // Versión de la base de datos
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla
    private static final String TABLE_NAME = "resultados";

    // Nombre de las columnas
    private static final String KEY_ID = "_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_ULTIMO_ANALISIS = "ultimo_analisis";
    private static final String KEY_FECHA = "fecha";

    private static final String KEY_TIPO = "tipo";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
/**
 * Método llamado cuando se crea la base de datos por primera vez.
 * @param db La base de datos.
 */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Se ejecuta al crear la base de datos
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NOMBRE + " TEXT,"
                + KEY_TIPO + " TEXT,"
                + KEY_ULTIMO_ANALISIS + " TEXT,"
                + KEY_FECHA + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }
/**
 * Método UPDATE
 * @param db La base de datos.
 * @param oldVersion La antigua versión de la base de datos.
 * @param newVersion La nueva versión de la base de datos.
 */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se ejecuta al actualizar la base de datos (cambiar la versión)
        // Aquí puedes realizar cambios en la estructura de la base de datos si es necesario
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

/**
 * Agrega un nuevo modelo a la base de datos.
 * @param modelo El modelo a agregar.
 * @return El ID de la fila recién insertada.
 */
    public long agregarModelo(Modelo modelo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOMBRE, modelo.getNombre());
        values.put(KEY_TIPO, modelo.getTipo());
        values.put(KEY_ULTIMO_ANALISIS, modelo.getUltimoAnalisis());
        values.put(KEY_FECHA, modelo.getFecha());

        long id = db.insert(TABLE_NAME, null, values);
        db.close();

        return id;
    }

    // Método para obtener un modelo por su ID, implementado por si acaso. SIN USO
    public Modelo obtenerModelo(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_ID,
                        KEY_NOMBRE, KEY_TIPO, KEY_ULTIMO_ANALISIS, KEY_FECHA}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Modelo modelo = new Modelo(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        cursor.close();
        return modelo;
    }

    /**
     * Recupera todos los registros
     *
     * @return Una lista con todos los registros
     * */
    public List<Modelo> obtenerTodosLosModelos() {
        List<Modelo> modeloList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Modelo modelo = new Modelo(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                modeloList.add(modelo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return modeloList;
    }

    // Método UPDATE. SIN USO
    public int actualizarModelo(Modelo modelo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOMBRE, modelo.getNombre());
        values.put(KEY_ULTIMO_ANALISIS, modelo.getUltimoAnalisis());
        values.put(KEY_FECHA, modelo.getFecha());

        return db.update(TABLE_NAME, values, KEY_ID + " = ?",
                new String[]{String.valueOf(modelo.getNombre())});
    }

    // Método DELETE. SIN USO
    public void eliminarModelo(Modelo modelo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[]{String.valueOf(modelo.getNombre())});
        db.close();
    }


/**
 * Limpia la BDD
 *
 * */
    public void eliminarTodosLosRegistros() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
