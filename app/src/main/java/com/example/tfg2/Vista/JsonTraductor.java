package com.example.tfg2.Vista;

import android.widget.TextView;

import com.example.tfg2.Modelo.Modelo;
import com.example.tfg2.Controlador.UrlScan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Esta clase proporciona métodos para traducir y analizar JSON y mostrar los resultados en un TextView.
 */
public class JsonTraductor {

    static TextView textView;

    private static String nombreURL;

    public void setNombreURL(String nombreURL) {
        JsonTraductor.nombreURL = nombreURL;
    }


    public JsonTraductor(TextView textview) {
        this.textView = textview;
    }

/**
 * Obtiene la fecha actual formateada en "dd/MM/yyyy".
 *
 * @return la fecha actual formateada.
 */
    public static String Fecha() {

        Date fechaActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String fechaFormateada = formato.format(fechaActual);

        return fechaFormateada;
    }
/**
 * Obtiene el ID de un análisis a partir de un JSON.
 *
 * @param json el JSON del cual se obtendrá el ID.
 * @return el ID del análisis.
 */

    public String getId(String json) {
        try {
            JSONObject data = new JSONObject(json).getJSONObject("data");
            return data.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error al obtener el ID";
        }
    }

/**
 * Traduce y muestra el análisis de un archivo en el TextView.
 *
 * @param jsonString el JSON que contiene los datos del análisis.
 * @param detalle    el nivel de detalle del análisis (1 para simple, 2 para detallado).
 */
    public void traducirAnalisis(String jsonString, int detalle) {

        //Analisis Simple
        if (detalle == 1) {
            try {
                // Convertir el String JSON en un objeto JSONObject
                JSONObject json = new JSONObject(jsonString);

                // Obtener los valores de stats
                JSONObject data = json.getJSONObject("data");
                JSONObject attributes = data.getJSONObject("attributes");
                JSONObject stats = attributes.getJSONObject("stats");
                int malicious = stats.getInt("malicious");
                int suspicious = stats.getInt("suspicious");
                int undetected = stats.getInt("undetected");
                int harmless = stats.getInt("harmless");
                int timeout = stats.getInt("timeout");
                int confirmedTimeout = stats.getInt("confirmed-timeout");
                int failure = stats.getInt("failure");
                int typeUnsupported = stats.getInt("type-unsupported");

                // Construir el texto a mostrar en el TextView
                StringBuilder sb = new StringBuilder();
                mostrarValor(sb, "malicious", malicious);
                mostrarValor(sb, "suspicious", suspicious);
                mostrarValor(sb, "undetected", undetected);
                mostrarValor(sb, "harmless", harmless);
                mostrarValor(sb, "timeout", timeout);
                mostrarValor(sb, "confirmed-timeout", confirmedTimeout);
                mostrarValor(sb, "failure", failure);
                mostrarValor(sb, "type-unsupported", typeUnsupported);


                // Establecer el texto en el TextView
                textView.setText(sb.toString());


                //creacion de Objeto
                Modelo modelo = new Modelo(MainActivity.fileName);
                modelo.setTipo("Archivo");
                modelo.setFecha(Fecha());
                if (malicious == 0 || suspicious == 0) {
                    modelo.setUltimoAnalisis("Limpio");
                } else if (malicious > 0) {
                    modelo.setUltimoAnalisis("Malicioso");
                } else if (suspicious > 0) {
                    modelo.setUltimoAnalisis("Sospechoso");
                }


                MainActivity.dbh.agregarModelo(modelo);


            } catch (JSONException e) {
                e.printStackTrace();
                // Manejar la excepción de análisis JSON aquí, por ejemplo, mostrar un mensaje de error en el TextView
                textView.setText("Error al analizar el JSON");
            }
        } else {
            //Analisis en detalle de los proveedores
            try {
                JSONObject jsonData = new JSONObject(jsonString);
                JSONObject data = jsonData.getJSONObject("data");
                JSONObject attributes = data.getJSONObject("attributes");
                JSONObject results = attributes.getJSONObject("results");

                StringBuilder message = new StringBuilder();
                message.append("ID: ").append(data.getString("id")).append("\n");
                message.append("Type: ").append(data.getString("type")).append("\n");
                message.append("Self Link: ").append(data.getJSONObject("links").getString("self")).append("\n");
                message.append("Item Link: ").append(data.getJSONObject("links").getString("item")).append("\n");
                message.append("Status: ").append(attributes.getString("status")).append("\n");

                // Iterate over the results of each engine
                for (Iterator<String> it = results.keys(); it.hasNext(); ) {
                    String engineName = it.next();
                    JSONObject engine = results.getJSONObject(engineName);
                    String engineVersion = engine.getString("engine_version");
                    String engineUpdate = engine.optString("engine_update", "N/A");
                    String category = engine.getString("category");
                    String result = engine.optString("result", "N/A");

                    message.append("\n");
                    message.append("Engine Name: ").append(engineName).append("\n");
                    message.append("Engine Version: ").append(engineVersion).append("\n");
                    message.append("Engine Update: ").append(engineUpdate).append("\n");
                    message.append("Category: ").append(category).append("\n");
                    message.append("Result: ").append(result).append("\n");
                }

                textView.setText(message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                textView.setText("Error al procesar el JSON");
            }
        }
    }

/**
 * Traduce y muestra el análisis de una URL en el TextView.
 *
 * @param jsonResponse el JSON que contiene los datos del análisis.
 * @param detalle      el nivel de detalle del análisis (1 para simple, 2 para detallado).
 */
    public static void traducirUrlAnalisis(String jsonResponse, int detalle) {

        JSONObject resultsObject;
        if (detalle == 1) {
            try {
                // Parsea el JSON
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONObject attributesObject = dataObject.getJSONObject("attributes");
                resultsObject = attributesObject.getJSONObject("results");
                JSONObject statsObject = attributesObject.getJSONObject("stats");

                // Extrae la información necesaria
                int malicious = statsObject.getInt("malicious");
                int suspicious = statsObject.getInt("suspicious");
                int undetected = statsObject.getInt("undetected");
                int harmless = statsObject.getInt("harmless");
                int timeout = statsObject.getInt("timeout");

                // Construir el texto a mostrar en el TextView
                StringBuilder sb = new StringBuilder();
                sb.append(("Resultados del análisis:\n"));
                mostrarValor(sb, "Maliciosos: ", malicious);
                mostrarValor(sb, "Sospechosos: ", suspicious);
                mostrarValor(sb, "No detectados: ", undetected);
                mostrarValor(sb, "Inofensivos: ", harmless);
                mostrarValor(sb, "Timeout", timeout);

                //creacion de Objeto
                Modelo modelo = new Modelo(obtenerNombreSitio(UrlScan.nombreURL()));
                modelo.setTipo("URL");

                modelo.setFecha(Fecha());


                if (malicious == 0 || suspicious == 0) {
                    modelo.setUltimoAnalisis("Limpio");
                } else if (malicious > 0) {
                    modelo.setUltimoAnalisis("Malicioso");
                } else if (suspicious > 0) {
                    modelo.setUltimoAnalisis("Sospechoso");
                }


                MainActivity.dbh.agregarModelo(modelo);


                // Muestra el mensaje
                textView.setText(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONObject attributesObject = dataObject.getJSONObject("attributes");
                resultsObject = attributesObject.getJSONObject("results");
                JSONObject statsObject = attributesObject.getJSONObject("stats");

                // Itera sobre los motores de análisis y sus resultados
                JSONArray engineNames = resultsObject.names();
                if (engineNames != null) {
                    for (int i = 0; i < engineNames.length(); i++) {
                        String engineName = engineNames.getString(i);
                        JSONObject engineResult = resultsObject.getJSONObject(engineName);

                        String method = engineResult.getString("method");
                        String category = engineResult.getString("category");
                        String result = engineResult.getString("result");

                        // Construye y muestra el mensaje con los detalles del análisis por motor
                        StringBuilder engineMessage = new StringBuilder();
                        engineMessage.append("Motor: ").append(engineName).append("\n");
                        engineMessage.append("Método: ").append(method).append("\n");
                        engineMessage.append("Categoría: ").append(category).append("\n");
                        engineMessage.append("Resultado: ").append(result).append("\n");

                        // Muestra el mensaje del motor de análisis
                        textView.append("\n\n");
                        textView.append(engineMessage.toString());
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

 /** Muestra un valor en el StringBuilder con el formato "nombre: valor".
  *
  *  @param sb    el StringBuilder en el que se añadirá el valor.
  * @param nombre el nombre del valor.
  * @param valor el valor a mostrar.
  */
    private static void mostrarValor(StringBuilder sb, String nombre, int valor) {
        sb.append(nombre).append(": ").append(valor).append("\n");
    }
/**
 * Obtiene el nombre del sitio a partir de una URL.
 *
 * @param url la URL del sitio.
 * @return el nombre del sitio.
 */
    public static String obtenerNombreSitio(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4); // Eliminar "www." del inicio si está presente
            }
            return host;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}

