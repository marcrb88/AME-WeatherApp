package com.example.weather;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    RequestQueue requestQueue;
    TextView textView;
    TextView textView2;
    EditText cityText;
    TextView resultText, luminosity;
    private InputStream inStream;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        luminosity = findViewById(R.id.luminosity);

        requestQueue = Volley.newRequestQueue(this);
        stringRequest();

        try {
            bluetooth();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void bluetooth() throws IOException {
        String [] bluePermision = {Manifest.permission.BLUETOOTH_CONNECT};
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            bluePermision,
                            1
                    );
                    Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
                    if (bondedDevices.size() > 0) {
                        Object[] devices = (Object[]) bondedDevices.toArray();
                        BluetoothDevice device = (BluetoothDevice) devices[1];
                        ParcelUuid[] uuids = device.getUuids();
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        socket.connect();

                        inStream = socket.getInputStream();
                        DataInputStream mmInStream = new DataInputStream(inStream);
                        byte[] luminosityBuffer = new byte[16];
                        //byte[] temperatureBuffer = new byte[20];

                        int luminosityBytes = mmInStream.read(luminosityBuffer);
                        boolean message = true;
                        while (luminosityBytes != 16) {
                            if (message){
                                Toast toast = Toast.makeText(this, "Loading application data ...", Toast.LENGTH_LONG);
                                toast.show();
                                message = false;
                            }
                            luminosityBytes = mmInStream.read(luminosityBuffer);

                        }
                        String readMessage = new String(luminosityBuffer, 0, 16);
                        luminosity.setText(readMessage);

                    }
                }

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }




    private void stringRequest() {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://dataservice.accuweather.com/currentconditions/v1/304358?apikey=q0ANEWlKMqCujZ4oIxZwCRbbbbSMpAdl",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        textView = findViewById(R.id.text_view);

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
        requestQueue.add(request);
    }

    public void citySearch(View view) {
        cityText = (EditText) findViewById(R.id.cityText);
        resultText = findViewById(R.id.text_result);
        textView2 = findViewById(R.id.text_view2);

        StringRequest request = new StringRequest(Request.Method.GET,
                "https://dataservice.accuweather.com/locations/v1/cities/search?apikey=q0ANEWlKMqCujZ4oIxZwCRbbbbSMpAdl&q="+cityText.getText(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String key = "";
                        String type = "";
                        String localizedName = "";
                        String regionId = "";
                        String countryId = "";

                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            ArrayList<City> cityArrayList= new ArrayList<>();

                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonObject = jsonarray.getJSONObject(i);
                                type = jsonObject.getString("Type");
                                key = jsonObject.getString("Key");
                                localizedName = jsonObject.getString("LocalizedName");
                                JSONObject region = jsonObject.getJSONObject("Region");
                                regionId = region.getString("ID");
                                JSONObject country = jsonObject.getJSONObject("Country");
                                countryId = country.getString("ID");

                                City city = new City (type, key,localizedName, regionId, countryId);
                                cityArrayList.add(city);
                            }
                            resultText.setText("Resultats de la búsqueda");
                            textView2.setText("");
                            if (jsonarray.length() > 0) {
                                for (City city : cityArrayList)
                                    textView2.append(city.toString());
                            } else {
                                textView2.setText("");
                                textView2.setText("La ciutat introduida no s'ha trobat");
                            }
                        } catch (Exception e) {
                            textView2.setText("");
                            textView2.setText("Hi ha hagut un error");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }

                }
        );
        requestQueue.add(request);

        //textView.setText("Click");
    }
}

