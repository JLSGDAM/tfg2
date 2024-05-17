package com.example.tfg2;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //Implementar funcion que avise de que se necesita conexion a internet para funcionar


    // Historial en SQLITE3??
    public static final String VT_API_ANALYSIS = "https://www.virustotal.com/api/v3/analyses/";
    private ActivityResultLauncher<String> filePickerLauncher;
    private static  String apiKey ="";
    //URL subida
    private static final String VIRUS_TOTAL_API_URL = "https://www.virustotal.com/api/v3/files";
    //URL analisis
    private Button b1,b2,b3;
    private ImageView iv;
    protected static String fileName;

    protected static DatabaseHelper dbh;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Animacion de los botones
        b1 = findViewById(R.id.bScan);
        b2 = findViewById(R.id.button);
        b3 = findViewById(R.id.button2);
        iv = findViewById(R.id.animationView);


        b1.setAlpha(0f);
        b1.setTranslationY(50);
        b2.setAlpha(0f);
        b2.setTranslationY(50);
        b3.setAlpha(0f);
        b3.setTranslationY(50);
        iv.setAlpha(0f);
        iv.setTranslationY(50);

        b1.animate().alpha(1f).translationYBy(-50).setDuration(1500);
        b2.animate().alpha(1f).translationYBy(-50).setDuration(1500);
        b3.animate().alpha(1f).translationYBy(-50).setDuration(1500);
        iv.animate().alpha(1f).translationYBy(-50).setDuration(3000);


        //API KEY
         apiKey = getResources().getString(R.string.virus_total_api_key);

        // Inicializar el launcher para seleccionar archivos
            filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    this::onFilePicked);

            //DATABASE
        dbh = new DatabaseHelper(this);




    }


    public void EscanearArchivo(View view) {

        filePickerLauncher.launch("*/*"); // Selecciona todos los tipos de archivos
    }
/**
 * Metodo para el tratamiento del archivo seleccionado
 * @param fileUri URI del archivo
 *
 * */
    private void onFilePicked(Uri fileUri){
        if (fileUri != null) {
            // Obtencion de URI del archivo seleccionado
            fileName = getFileNameFromUri(fileUri);
            // captura y lanzamiento
            capturarArchivo(fileUri);

        } else {
            Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show();
        }

    }
/**
 * Devuelve el nombre del archivo de la URI seleccionada
 * @param uri URI del archivo seleccionado
 *
 * */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            fileName = cursor.getString(columnIndex);
            cursor.close();
        }
        return fileName;
    }
/**
 * Metodo de captura del archivo, subida a la API y control de la respuesta
 *
 * @param fileUri Uri del archivo seleccionado
 * */
    private void capturarArchivo(Uri fileUri) {
        // Verificar que la uri del archivo no esté vacía
        if (fileUri != null) {
            // Crear un cliente OkHttp en un hilo de fondo
            new Thread(() -> {
                OkHttpClient client = new OkHttpClient();

                // Crear el cuerpo de la solicitud multipart para enviar el archivo
                RequestBody requestBody = null;
                try {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", getFileNameFromUri(fileUri),
                                    RequestBody.create(String.valueOf(getContentResolver().openInputStream(fileUri)), MediaType.parse("application/octet-stream")))
                            .build();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // Crear la solicitud POST para enviar el archivo a VirusTotal
                Request request = new Request.Builder()
                        .url(VIRUS_TOTAL_API_URL)
                        .post(requestBody)
                        .addHeader("accept", "application/json")
                        .addHeader("x-apikey", apiKey)
                        .addHeader("content-type", "multipart/form-data")
                        .build();

                try {
                    // Ejecutar la solicitud y obtener la respuesta en un hilo de fondo
                    Response response = client.newCall(request).execute();

                    // Verificar si la solicitud fue exitosa
                    if (response.isSuccessful()) {
                        // Obtener el cuerpo de la respuesta como texto en un hilo de fondo
                        String responseBody = response.body().string();
                        Intent intent = new Intent(this, Scan.class);
                        intent.putExtra("respuesta", responseBody);
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
            // Si la uri del archivo está vacía, mostrar un mensaje de error en el hilo principal
            runOnUiThread(() -> Toast.makeText(this, "No se ha seleccionado ningún archivo", Toast.LENGTH_SHORT).show());
        }
    }


    public void EscanearURL(View view) {
Intent intent = new Intent(this, UrlScan.class);
startActivity(intent);

    }

    public void AbrirBDD(View view) {

        Intent intent = new Intent(this, Historico.class);
        startActivity(intent);
    }

    public void helpPopUp(View view) {
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }


    public void animacion(View view) {

        LottieAnimationView animaView = findViewById(R.id.animationView);
        animaView.playAnimation();
    }
}
