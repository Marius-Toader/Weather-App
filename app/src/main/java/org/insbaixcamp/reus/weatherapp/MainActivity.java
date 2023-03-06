package org.insbaixcamp.reus.weatherapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//    String CITY = "madrid,es";
    String API = "ba23cfe0e83fe50b775f37242b85b415";
    double latitude;
    double longitude;

    String lat;
    String lng;
    TextView addressTxt, updated_atTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressTxt = findViewById(R.id.address);
        updated_atTxt = findViewById(R.id.updated_at);
        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.temp_min);
        temp_maxTxt = findViewById(R.id.temp_max);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);

        // Inicializa el objeto FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Comprueba si el usuario ha otorgado permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no ha otorgado permisos, solicítalos
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            // Si ha otorgado permisos, obtén la ubicación actual
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Obtiene la ubicación actual y la almacena en la variable CITY
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    //Log.d("CITY", latitude + " " + longitude);
                   // CITY = String.format(Locale.US, "%.2f,%.2f", latitude, longitude);

                    lat = String.format(Locale.US, "%.2f", latitude);
                    lng = String.format(Locale.US, "%.2f", longitude);

                    Log.d("CITY", lat + " " + lng);

                    // Llama al método weatherTask para obtener el clima actual de la ubicación actual
                    new weatherTask().execute();
                } else {
                    // Si la ubicación actual no se puede obtener, muestra un mensaje de error
                    addressTxt.setText(R.string.errorUbicacion);
                }
            });
        }


        new weatherTask().execute();
    }

    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Showing the ProgressBar, Making the main design GONE */
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            //https://api.openweathermap.org/data/2.5/weather?q=
//            Log.d("CITY", CITY);
            String response = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/weather?lat=" + lat
                    + "&lon=" + lng + "&units=metric&appid=" + API);

            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat
                    + "&lon=" + lng + "&units=metric&appid=" + API;

            Log.d("CITY", url);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Actualizado el: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES"))
                        .format(new Date(updatedAt * 1000));

                int temperature = Math.round(main.getInt("temp"));
                int temperaturemin = Math.round(main.getInt("temp_min"));
                int temperaturemax = Math.round(main.getInt("temp_max"));


                String temp = temperature + "°C";
                String tempMin = "Min Temp: " + temperaturemin + "°C";
                String tempMax = "Max Temp: " + temperaturemax + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");



                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");

                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                /* Populating extracted data into our views */
                addressTxt.setText(address);
                updated_atTxt.setText(updatedAtText);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-ES")).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("HH:mm", Locale.forLanguageTag("es-ES")).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                //findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }
    }
}