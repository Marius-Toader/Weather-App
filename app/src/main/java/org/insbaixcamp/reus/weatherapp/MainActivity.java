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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
//    String CITY = "madrid,es";
    String API = "ba23cfe0e83fe50b775f37242b85b415";
    double latitude;
    double longitude;

    String lat;
    String lng;
    TextView addressTxt, updated_atTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt;

    TextView tvstatus1, tvtemp1, tvtemp_min1, tvtemp_max1, tvstatus2, tvtemp2, tvtemp_min2, tvtemp_max2,
             tvstatus3, tvtemp3, tvtemp_min3, tvtemp_max3, tvday , tvday2, tvday3;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Actual

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

        // Forecast

        tvstatus1 = findViewById(R.id.status1);
        tvtemp1 = findViewById(R.id.temp1);
        tvtemp_min1 = findViewById(R.id.temp_min1);
        tvtemp_max1 = findViewById(R.id.temp_max1);
        tvstatus2 = findViewById(R.id.status2);
        tvtemp2 = findViewById(R.id.temp2);
        tvtemp_min2 = findViewById(R.id.temp_min2);
        tvtemp_max2 = findViewById(R.id.temp_max2);
        tvstatus3 = findViewById(R.id.status3);
        tvtemp3 = findViewById(R.id.temp3);
        tvtemp_min3 = findViewById(R.id.temp_min3);
        tvtemp_max3 = findViewById(R.id.temp_max3);
        tvday = findViewById(R.id.day);
        tvday2 = findViewById(R.id.day2);
        tvday3 = findViewById(R.id.day3);

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
                    // Obtiene la ubicación actual y la almacena en las variables
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

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

            String response = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/weather?lat=" + latitude
                    + "&lon=" + longitude + "&units=metric&appid=" + API);


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

                // Aquí llamamos al método execute de la clase forecastTask
                new forecastTask().execute();

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                //findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }
    }

    class forecastTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            String response = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude
                    + "&lon=" + longitude + "&units=metric&appid=" + API);

            String a = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude
                    + "&lon=" + longitude + "&units=metric&appid=" + API;

            Log.d("hola",a);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray list = jsonObj.getJSONArray("list");

                // Day 1
                JSONObject day1 = list.getJSONObject(0);

                JSONObject main = day1.getJSONObject("main");

                JSONObject weather1 = day1.getJSONArray("weather").getJSONObject(0);
                String temp1 = Math.round(main.getInt("temp")) + "ºC";

                String status1 = weather1.getString("description");


                String tempMin = Math.round(main.getInt("temp_min")) + "ºC";
                String tempMax = Math.round(main.getInt("temp_max")) + "ºC";

                // Day 2
                JSONObject day2 = list.getJSONObject(1);
                JSONObject main2 = day2.getJSONObject("main");
                JSONObject weather2 = day2.getJSONArray("weather").getJSONObject(0);
                String temp2 = Math.round(main2.getInt("temp")) + "ºC";

                String status2 = weather2.getString("description");

                String tempMin2 = Math.round(main2.getInt("temp_min")) + "ºC";
                String tempMax2 = Math.round(main2.getInt("temp_max")) + "ºC";

//                int temperature2 = Math.round(temp2.getInt("day"));
//                int temperaturemin2 = Math.round(temp2.getInt("min"));
//                int temperaturemax2 = Math.round(temp2.getInt("max"));
//
                 //Day 3
                JSONObject day3 = list.getJSONObject(2);
                JSONObject main3 = day3.getJSONObject("main");
                JSONObject weather3 = day3.getJSONArray("weather").getJSONObject(0);
                String temp3 = Math.round(main3.getInt("temp")) + "ºC";

                String status3 = weather3.getString("description");

                String tempMin3 = Math.round(main3.getInt("temp_min")) + "ºC";
                String tempMax3 = Math.round(main3.getInt("temp_max")) + "ºC";

//                JSONObject day3 = list.getJSONObject(2);
//                JSONObject weather3 = day3.getJSONArray("weather").getJSONObject(0);
//                JSONObject temp3 = day3.getJSONObject("temp");

//                String status3 = weather3.getString("description");
//                int temperature3 = Math.round(temp3.getInt("day"));
//                int temperaturemin3 = Math.round(temp3.getInt("min"));
//                int temperaturemax3 = Math.round(temp3.getInt("max"));

                // Obtener la fecha actual
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);

                // Obtener el día de la semana como un número (1-7)
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Convertir el número del día de la semana a una cadena de caracteres
                // (Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo)
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                String dayOfWeekString = sdf.format(calendar.getTime());


                tvday.setText(dayOfWeekString.substring(0, 1).toUpperCase() + dayOfWeekString.substring(1));

                calendar.add(Calendar.DAY_OF_YEAR, 1);

                SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE", Locale.getDefault());
                String dayOfWeekString2 = sdf2.format(calendar.getTime());

                tvday2.setText(dayOfWeekString2.substring(0, 1).toUpperCase() + dayOfWeekString2.substring(1));

                calendar.add(Calendar.DAY_OF_YEAR, 1);

                SimpleDateFormat sdf3 = new SimpleDateFormat("EEEE", Locale.getDefault());
                String dayOfWeekString3 = sdf3.format(calendar.getTime());

                tvday3.setText(dayOfWeekString3.substring(0, 1).toUpperCase() + dayOfWeekString3.substring(1));


                // Update UI
                tvstatus1.setText(status1.toUpperCase());
                tvstatus2.setText(status2.toUpperCase());
                tvstatus3.setText(status3.toUpperCase());
                tvtemp1.setText(temp1);
                tvtemp2.setText(temp2);
                tvtemp3.setText(temp3);

                tvtemp_max1.setText("Max Temp: " + tempMax);
                tvtemp_min1.setText("Min Temp: " + tempMin);

                tvtemp_max2.setText("Max Temp: " + tempMax2);
                tvtemp_min2.setText("Min Temp: " + tempMin2);

                tvtemp_max3.setText("Max Temp: " + tempMax3);
                tvtemp_min3.setText("Min Temp: " + tempMin3);

            } catch (JSONException e) {
                Log.e("forecastTask", "JSON exception", e);
            }
        }
    }

}