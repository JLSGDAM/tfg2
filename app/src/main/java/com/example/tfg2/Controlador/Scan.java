package com.example.tfg2.Controlador;

import static com.example.tfg2.R.*;
import static com.example.tfg2.Controlador.UrlScan.nombreURL;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.tfg2.Modelo.Modelo;
import com.example.tfg2.R;
import com.example.tfg2.Vista.JsonTraductor;
import com.example.tfg2.Vista.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/**
* Esta clase representa la actividad que muestra los resultados JSON del análisis de archivos o URLs.
*/
public class Scan extends AppCompatActivity {

    private TextView textView;
    private String cuerpoRespuesta, cuerpoRespuestaURL;


    private static ProgressBar pb;

    private static String apiKey = "";
    private ImageView gif;
    private int tiempo= 0;

    private Modelo modelo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fase2);
        apiKey = getResources().getString(R.string.virus_total_api_key);
        textView = findViewById(id.textViewAnalisis);
        textView.setMovementMethod(new ScrollingMovementMethod());
        pb = findViewById(id.pb2);

        gif = findViewById(id.loading);
        Glide.with(this).load(drawable.jtsearch).into(gif);
        gif.setVisibility(View.INVISIBLE);

        cargarDatos(1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarDatos(int detalle){
        Intent intent = getIntent();
        if (intent != null ) {
            cuerpoRespuesta = intent.getStringExtra("respuesta");
            cuerpoRespuestaURL = intent.getStringExtra("respuestaURL");

            if (cuerpoRespuesta != null){
                // Actualizar la interfaz de usuario en el hilo principal
                runOnUiThread(() -> {

                    // traduccion de la respuesta
                    JsonTraductor traductor = new JsonTraductor(textView);
                    //Aislamiento de la id del archivo
                    String idFile = traductor.getId(cuerpoRespuesta);
                    //Log.d("IDFILE", idFile);

                    //Report del archivo
                    fileReportDelay(idFile,detalle);
                });
            }else{
                Log.d("Error", "Extra = URL");
            }

            if (cuerpoRespuestaURL != null){
                // Actualizar la interfaz de usuario en el hilo principal
                runOnUiThread(() -> {

                    // traduccion de la respuesta
                    JsonTraductor traductor = new JsonTraductor(textView);
                    //Aislamiento de la id del archivo
                    String idURL = traductor.getId(cuerpoRespuestaURL);
                    //Log.d("IDURL", idURL);

                    //Report del archivo
                    urlReport(idURL,detalle);
                });
            }else{
                Log.d("Error", "Extra = file ");
            }

        }else{
            Log.d("Error", "Fallo del intent");
        }

    }



/**
 * FileReport con delay que espera una respuesta afirmativa del servidor
 *
 * @param id ID del archivo a escanear
 * @param detalle  Variable de control que indica que tipo de analisis se hace
 *
 * */
    private void fileReportDelay(String id, int detalle) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MainActivity.VT_API_ANALYSIS + id)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("x-apikey", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error en la solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {


                if (response.isSuccessful()) {
                    tiempo ++;
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String status = json.getJSONObject("data")
                                .getJSONObject("attributes")
                                .getString("status");
                        Log.d("STATUS", status);



                        if (status.equals("queued")) {

                            Log.d("TIEMPO", String.valueOf(tiempo));
                            runOnUiThread(() -> {
                                pb.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Petición en cola, esto puede tardar algunos minutos...", Toast.LENGTH_SHORT).show();
                            });

                            // Esperar 5 segundos y volver a comprobar el estado
                            try {
                                Thread.sleep(5000);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Carga de GIF
                            if (tiempo == 2) {
                                runOnUiThread(() -> mostrarGIF());
                            }

                            fileReportDelay(id, detalle);

                        } else {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.GONE);
                                gif.setVisibility(View.GONE);
                                JsonTraductor traductor = new JsonTraductor(textView);
                                traductor.traducirAnalisis(responseBody, detalle);
                                tiempo = 0;
                               // Log.d("simple", responseBody);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error en la solicitud: " + response.code() + " " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

/**
 * Solicita un informe detallado del análisis.
 *
 * @param view La vista que desencadena este método.
 */
    public void informeDetallado(View view) { cargarDatos(2); }

    public void Volver(View view) { finish(); }

    private void mostrarGIF() {
        pb.setVisibility(View.INVISIBLE);
        gif.setVisibility(View.VISIBLE);
    }


    private void urlReport(String id, int detalle) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MainActivity.VT_API_ANALYSIS + id)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("x-apikey", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error en la solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    tiempo++;
                    String responseBody = response.body().string();
                    try {
                        // Parsea la respuesta JSON
                        JSONObject json = new JSONObject(responseBody);
                        String status = json.getJSONObject("data")
                                .getJSONObject("attributes")
                                .getString("status");
                        Log.d("STATUS", status);

                        if (status.equals("queued")) {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Petición en cola, esto puede tardar algunos minutos...", Toast.LENGTH_SHORT).show();
                            });

                            // Esperar 5 segundos y volver a comprobar el estado
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Carga de GIF
                            if (tiempo == 2) {
                                runOnUiThread(() -> mostrarGIF());
                            }

                            // Vuelve a llamar al método
                            urlReport(id, detalle);

                        } else {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.GONE);
                                gif.setVisibility(View.GONE);
                                JsonTraductor traductor = new JsonTraductor(textView);
                                traductor.setNombreURL(getIntent().getStringExtra(nombreURL));
                                traductor.traducirUrlAnalisis(responseBody, detalle);
                                tiempo = 0;
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error en la solicitud: " + response.code() + " " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


}