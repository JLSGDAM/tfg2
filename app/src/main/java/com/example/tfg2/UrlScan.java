package com.example.tfg2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UrlScan extends AppCompatActivity {


    public static String nombreURL;
    private static EditText et ;
    private String apiKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_url_scan);
        apiKey = getResources().getString(R.string.virus_total_api_key);
        et =findViewById(R.id.urlText);
        nombreURL = nombreURL();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected static String nombreURL(){
            String url = String.valueOf(et.getText());

        return url;
    }

    private void capturarURL(String url) {
        // Verificar que la URL no esté vacía
        if (url != null && !url.isEmpty()) {
            // Crear un cliente OkHttp en un hilo de fondo
            new Thread(() -> {
                OkHttpClient client = new OkHttpClient();

                // Crear el cuerpo de la solicitud para enviar la URL a VirusTotal
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                String requestBodyString = "url=" + url;
                RequestBody requestBody = RequestBody.create(mediaType, requestBodyString);

                // Crear la solicitud POST para enviar la URL a VirusTotal
                Request request = new Request.Builder()
                        .url("https://www.virustotal.com/api/v3/urls")
                        .post(requestBody)
                        .addHeader("accept", "application/json")
                        .addHeader("x-apikey", apiKey)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build();

                try {
                    // Ejecutar la solicitud y obtener la respuesta en un hilo de fondo
                    Response response = client.newCall(request).execute();

                    // Verificar si la solicitud fue exitosa
                    if (response.isSuccessful()) {
                        // Obtener el cuerpo de la respuesta como texto en un hilo de fondo
                        String responseBody = response.body().string();
                        Intent intent = new Intent(this, Scan.class);
                        intent.putExtra("respuestaURL", responseBody);
                        intent.putExtra("nombreURL", nombreURL());
                        startActivity(intent);
                    } else {
                        // Si la solicitud no fue exitosa, mostrar un mensaje de error en el hilo principal
                        runOnUiThread(() -> Toast.makeText(this, "Error en la solicitud: " + response.code() + " " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    // Capturar cualquier excepción de E/S en un hilo de fondo
                    e.printStackTrace();
                }
            }).start();
        } else {
            // Si la URL está vacía, mostrar un mensaje de error en el hilo principal
            runOnUiThread(() -> Toast.makeText(this, "La URL está vacía", Toast.LENGTH_SHORT).show());
        }
    }


    public void mandarURL(View view) {
        capturarURL(nombreURL());
    }

    public void volver(View view) { finish();}
}


