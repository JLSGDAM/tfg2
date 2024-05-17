package com.example.tfg2;

import static com.example.tfg2.R.*;

import android.database.Cursor;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class Historico extends AppCompatActivity {


    private DatabaseHelper mDatabaseHelper;
    private TableLayout tabla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);

        tabla = findViewById(id.tabla);
       // tabla.setMovementMethod(new ScrollingMovementMethod());
        mDatabaseHelper = new DatabaseHelper(this);

        cargarBasedeDatos();


    }

    private void cargarBasedeDatos() {
        List<Modelo> modelos = mDatabaseHelper.obtenerTodosLosModelos(); // Obtener todos los modelos de la base de datos
        for (Modelo modelo : modelos) {
            // Obtener los datos de cada modelo
            String nombre = modelo.getNombre();
            String tipo = modelo.getTipo();
            String ultimoAnalisis = modelo.getUltimoAnalisis();
            String fecha = modelo.getFecha();

            // Crear una nueva fila para la tabla
            TableRow row = new TableRow(this);

            // Crear TextViews para cada celda y establecer los datos
            TextView textViewNombre = new TextView(this);
            textViewNombre.setText(nombre);
            textViewNombre.setMaxWidth(150); // Establece un ancho máximo para evitar que el texto se desborde
            textViewNombre.setGravity(Gravity.CENTER); // Alinea el texto al centro de la celda

            TextView txTipo = new TextView(this);
            txTipo.setText(tipo);
            txTipo.setMaxWidth(150);
            txTipo.setGravity(Gravity.CENTER);

            TextView textViewUltimoAnalisis = new TextView(this);
            textViewUltimoAnalisis.setText(ultimoAnalisis);
            textViewUltimoAnalisis.setMaxWidth(150);
            textViewUltimoAnalisis.setGravity(Gravity.CENTER);

            TextView textViewFecha = new TextView(this);
            textViewFecha.setText(fecha);
            textViewFecha.setMaxWidth(150);
            textViewFecha.setGravity(Gravity.CENTER);

            // Agregar los TextViews a la fila
            row.addView(textViewNombre);
            row.addView(txTipo);
            row.addView(textViewUltimoAnalisis);
            row.addView(textViewFecha);

            // Obtener la tabla del layout
            TableLayout tabla = findViewById(R.id.tabla);

            // Agregar la fila a la tabla
            tabla.addView(row);
        }
    }

    public void limpiarRegistros(View view) {
        DatabaseHelper dbh = new DatabaseHelper(this);
        dbh.eliminarTodosLosRegistros();
        finish();

    }

    public void volver(View view) {finish();}
}