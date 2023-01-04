package com.example.weather;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;

import android.os.ParcelUuid;

import android.view.View;

import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private TextView textView, textView2,resultText, luminosity, temperature;
    private EditText cityText;
    private ScrollView scrollView;
    private InputStream inStream;
    boolean activeSocket = false;
    private String cityKey;
    final private String apiKey = "w3enrWrq74eqbREYTdcgsIme21unVk4U";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = findViewById(R.id.text_view);
        luminosity = (TextView) findViewById(R.id.luminosity);
        temperature = (TextView) findViewById(R.id.temperature);

        requestQueue = Volley.newRequestQueue(this);

    }
        public void refresh(View view) {
            currentConditions();
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        @SuppressLint("MissingPermission")
        public void bluetooth (View view) throws IOException {
            BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
            if (blueAdapter != null) {
                if (blueAdapter.isEnabled()) {
                        Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
                        if (bondedDevices.size() > 0) {
                            Object[] devices = (Object[]) bondedDevices.toArray();
                            BluetoothDevice device = (BluetoothDevice) devices[1];
                            ParcelUuid[] uuids = device.getUuids();
                            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                            socket.connect();
                            activeSocket = true;
                            inStream = socket.getInputStream();
                            DataInputStream mmInStream = new DataInputStream(inStream);
                            byte[] buffer = new byte[18];

                            luminosity.setText("");
                            while (inStream.available() != 18) {}
                            int bytes = mmInStream.read(buffer);

                            String readLuminosity= new String(buffer, 0, bytes);
                            String [] array = readLuminosity.split(",");
                            luminosity.setText("Lux: " + array[0]);
                            temperature.setText("Temperature: " + array[1] + "°C");

                            socket.getInputStream().close();
                            socket.close();

                        }
                    } else
                        Toast.makeText(this, "Bluetooth disable", Toast.LENGTH_LONG).show();
                }

        }

        public void citySearch (View view){
            cityText = (EditText) findViewById(R.id.cityText);
            resultText = findViewById(R.id.text_result);
            textView2 = findViewById(R.id.text_view2);
            scrollView = findViewById(R.id.scrollView4);
            String key = "";

            StringRequest request1 = new StringRequest(Request.Method.GET,
                    "https://dataservice.accuweather.com/locations/v1/cities/search?apikey=" + apiKey + "&q=" + cityText.getText(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String key = "";
                            String type = "";
                            String localizedName = "";
                            String regionId = "";
                            String countryId = "";
                            String latitude = "";
                            String longitude = "";
                            cityKey = null;

                            try {
                                JSONArray jsonarray = new JSONArray(response);
                                ArrayList<City> cityArrayList = new ArrayList<>();

                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                                    type = jsonObject.getString("Type");
                                    key = jsonObject.getString("Key");
                                    if (cityKey == null)  cityKey = key;
                                    localizedName = jsonObject.getString("LocalizedName");
                                    JSONObject region = jsonObject.getJSONObject("Region");
                                    regionId = region.getString("ID");
                                    JSONObject country = jsonObject.getJSONObject("Country");
                                    countryId = country.getString("ID");
                                    JSONObject position = jsonObject.getJSONObject("GeoPosition");
                                    float flatitude = BigDecimal.valueOf(position.getDouble("Latitude")).floatValue();
                                    latitude = Float.toString(flatitude);
                                    float flongitude = BigDecimal.valueOf(position.getDouble("Longitude")).floatValue();
                                    longitude = Float.toString(flongitude);

                                    City city = new City(type, key, localizedName, regionId, countryId, latitude, longitude);
                                    cityArrayList.add(city);
                                }
                                textView2.setText("");
                                scrollView.fullScroll(ScrollView.FOCUS_UP);
                                boolean currentConditions = false;
                                if (jsonarray.length() > 0) {
                                    for (City city : cityArrayList)
                                        textView2.append(city.toString());
                                        currentConditions = true;
                                } else {
                                    textView2.setText("");
                                    textView2.setText("The city introduced doesn't exist");
                                }
                                if (currentConditions) currentConditions();
                                else textView.setText("");
                            } catch (Exception e) {
                                textView2.setText("");
                                textView2.setText("An error has ocurred");
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }

                    }
            );


            requestQueue.add(request1);

        }

        public void currentConditions() {
            if (cityKey == null) textView.setText("Can't refresh without a city name");
            StringRequest request2 = new StringRequest(Request.Method.GET,
                    "https://dataservice.accuweather.com/currentconditions/v1/" + cityKey + "?apikey=" + apiKey,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String state = "";
                            double value = 0;
                            boolean hasPrecipitation = false;
                            try {
                                JSONArray jsonarray = new JSONArray(response);
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                                    hasPrecipitation = jsonObject.getBoolean("HasPrecipitation");
                                    state = jsonObject.getString("WeatherText");
                                    JSONObject temperature = jsonObject.getJSONObject("Temperature");
                                    JSONObject metric = temperature.getJSONObject("Metric");
                                    value = metric.getDouble("Value");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            textView.setText("Current State: " + state + "\nValue: " + value + "\nPrecipitation: " + hasPrecipitation);

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }

                    }
            );
            requestQueue.add(request2);
    }

}


