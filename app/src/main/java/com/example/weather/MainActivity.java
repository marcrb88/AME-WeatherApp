package com.example.weather;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;

    RequestQueue requestQueue;
    TextView textView;
    TextView textView2;
    EditText cityText;
    TextView resultText;
    Button open;
    Button close;
    BluetoothAdapter blueAdapter;
    TextView luminosity;
    TextView temperature;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueAdapter.isEnabled()) {
            showToast("Encenent blueetoth");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(intent, REQUEST_ENABLE_BT);
                return;
            }
        } else {
            showToast("Blueethoth ja ences");
        }

        requestQueue = Volley.newRequestQueue(this);
        stringRequest();


    }

    public void listenClick(View target) {
        ServerClass serverClass = new ServerClass();
        serverClass.start();
    }


    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClass() {
            try {
                    serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    Receive receive = new Receive(socket);
                    receive.start();

                    break;
                }
            }
        }
    }

    private class Receive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;

        public Receive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    luminosity.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

