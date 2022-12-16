package com.example.weather;



import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    RequestQueue requestQueue;
    TextView textView;
    TextView textView2;
    EditText cityText;
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        stringRequest();

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

