/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thecongers.gardenthing;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dexterind.grovepi.Grovepi;
import com.dexterind.grovepi.GrovepiListener;
import com.dexterind.grovepi.events.SensorEvent;
import com.dexterind.grovepi.events.StatusEvent;
import com.dexterind.grovepi.sensors.CO2Sensor;
import com.dexterind.grovepi.sensors.DHTDigitalSensor;
import com.dexterind.grovepi.sensors.MoistureSensor;
import com.dexterind.grovepi.sensors.TSL2561Sensor;
import org.thecongers.gardenthing.utils.GardenDatabase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements GrovepiListener {

    private static final String TAG = "GardenThing";
    private static Grovepi grovepi = null;

    private SharedPreferences sharedPrefs;
    private static final int SETTINGS_RESULT = 1;

    private TextView mTemperature;
    private TextView mHumidity;
    private TextView mAmbientLight;
    private TextView mIrLight;
    private TextView mLux;
    private TextView mCo2;
    private TextView mSoilMoistureLabel1;
    private TextView mSoilMoistureLabel2;
    private TextView mSoilMoistureLabel3;
    private TextView mSoilMoisture1;
    private TextView mSoilMoisture2;
    private TextView mSoilMoisture3;

    private GardenDatabase database;

    private WebServer webServer;
    private static final int WEBSERVER_PORT = 8080;

    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if ( sharedPrefs.getBoolean("prefTemperatureHumiditySensor",true) ){
                    getDHTValue();
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorOne",false) ){
                    getMoistureValue(0);
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorTwo",false) ){
                    getMoistureValue(1);
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorThree",false) ){
                    getMoistureValue(2);
                }
                if ( sharedPrefs.getBoolean("prefLightSensor",false) ){
                    getLightValue();
                }
                if ( sharedPrefs.getBoolean("prefCO2Sensor",false) ){
                    getCO2Value();
                }
            } catch ( IOException | InterruptedException e) {
                Log.d(TAG, "Err:" + e.getMessage() );
            }

            int prefinterval = Integer.parseInt(sharedPrefs.getString("prefSensorInterval","1"));
            int interval = prefinterval * 60000;
            timerHandler.postDelayed(this, interval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTemperature = (TextView) findViewById(R.id.tv_temperature);
        mHumidity = (TextView) findViewById(R.id.tv_humidity);
        mAmbientLight = (TextView) findViewById(R.id.tv_ambient);
        mIrLight = (TextView) findViewById(R.id.tv_ir);
        mLux = (TextView) findViewById(R.id.tv_lux);
        mCo2 = (TextView) findViewById(R.id.tv_co2);
        mSoilMoistureLabel1 = (TextView) findViewById(R.id.tv_soil_moisture1_label);
        mSoilMoistureLabel2 = (TextView) findViewById(R.id.tv_soil_moisture2_label);
        mSoilMoistureLabel3 = (TextView) findViewById(R.id.tv_soil_moisture3_label);
        mSoilMoisture1 = (TextView) findViewById(R.id.tv_soil_moisture1);
        mSoilMoisture2 = (TextView) findViewById(R.id.tv_soil_moisture2);
        mSoilMoisture3 = (TextView) findViewById(R.id.tv_soil_moisture3);

        database = new GardenDatabase(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            webServer = new WebServer(this, WEBSERVER_PORT);
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setTimeZone("America/Denver");

        grovepi = Grovepi.getInstance();
        grovepi.addListener(this);
        grovepi.init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        webServer.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                // Info was selected
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Information")
                        .setMessage("Grove Garden Version: 1.0\n\nCopyright Keith Conger 2017\n\nURL: http://" + getIpAddress() + ":" + WEBSERVER_PORT + "/")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info);

                AlertDialog dialog = alertDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    }
                });

                dialog.show();
                return true;
            case R.id.action_settings:
                // Settings was selected
                Intent startSettingsActivityIntent = new Intent(MainActivity.this,  SettingsActivity.class);
                startActivityForResult(startSettingsActivityIntent, SETTINGS_RESULT);
                return true;
            case R.id.action_adv_settings:
                // Adv Settings was selected
                Intent startAdvSettingsActivityIntent = new Intent(MainActivity.this,  AdvSettingsActivity.class);
                startActivityForResult(startAdvSettingsActivityIntent, SETTINGS_RESULT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Runs when settings are updated
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SETTINGS_RESULT)
        {
            try {
                if ( sharedPrefs.getBoolean("prefTemperatureHumiditySensor",false) ){
                    getDHTValue();
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorOne",false) ){
                    getMoistureValue(0);
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorTwo",false) ){
                    getMoistureValue(1);
                }
                if ( sharedPrefs.getBoolean("prefMoistureSensorThree",false) ){
                    getMoistureValue(2);
                }
                if ( sharedPrefs.getBoolean("prefLightSensor",false) ){
                    getLightValue();
                }
                if ( sharedPrefs.getBoolean("prefCO2Sensor",false) ){
                    getCO2Value();
                }
            } catch ( IOException | InterruptedException e) {
                Log.d(TAG, "Err:" + e.getMessage() );
            }
        }
    }

    public void onStatusEvent(StatusEvent event) {
        if (event.status == 2) {
            try {
                Log.d(TAG, "GrovePi " + grovepi.board.version()+ " initialized");
            } catch( IOException e) {
                Log.d(TAG, e.getMessage() );
            }
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }
    public void onSensorEvent(SensorEvent event) {
        Log.d(TAG, event.value);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    public void onClickTemperature(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = TemperatureActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickHumidity(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = HumidityActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickAmbient(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = AmbientActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickIr(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = IrActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickLux(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = LuxActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickCo2(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = Co2Activity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startActivity(startChildActivityIntent);

    }
    public void onClickSoilMoisture1(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = SoilMoistureActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        Bundle b = new Bundle();
        b.putInt("key", 1);
        startChildActivityIntent.putExtras(b);
        startActivity(startChildActivityIntent);

    }
    public void onClickSoilMoisture2(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = SoilMoistureActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        Bundle b = new Bundle();
        b.putInt("key", 2);
        startChildActivityIntent.putExtras(b);
        startActivity(startChildActivityIntent);

    }
    public void onClickSoilMoisture3(View v) {

        Context context = MainActivity.this;
        Class destinationActivity = SoilMoistureActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        Bundle b = new Bundle();
        b.putInt("key", 3);
        startChildActivityIntent.putExtras(b);
        startActivity(startChildActivityIntent);
    }

    public void getDHTValue() throws IOException, InterruptedException {
        int pin = Integer.parseInt(sharedPrefs.getString("prefTemperatureHumiditySensorPort", "7"));
        DHTDigitalSensor sensor = new DHTDigitalSensor(
                pin,
                DHTDigitalSensor.MODULE_DHT22,
                DHTDigitalSensor.SCALE_C
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String datetime = sdf.format(new Date());

        float[] output = sensor.read();
        if (output[0] > 0) {
            String temperature = Integer.toString((int) Math.round(output[0]));
            String temperatureLocalized = "";
            String unit = "";
            if ( sharedPrefs.getBoolean("prefTemperatureUnit",true) ){
                unit = "F";
                temperatureLocalized = Integer.toString((int) Math.round(convertCtoF(output[0])));
            } else {
                unit = "C";
                temperatureLocalized = temperature;
            }
            mTemperature.setText(temperatureLocalized + unit);
            Log.d(TAG, "Temperature: " + temperatureLocalized + unit);
            database.createRecord("Temperature", datetime, temperature);
        }
        if (output[1] > 0) {
            String humidity = Integer.toString((int) Math.round(output[1]));
            mHumidity.setText(humidity + "%");
            Log.d(TAG, "Humidity: " + humidity + "%");
            database.createRecord("Humidity", datetime, humidity);
        }
        String heatindex = Float.toString(output[2]);
        Log.d(TAG, "Heat Index: " + heatindex);
    }

    public void getMoistureValue(int pin) throws IOException, InterruptedException {
        MoistureSensor sensor = new MoistureSensor(pin);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String datetime = sdf.format(new Date());

        int moisture = sensor.read();
        if (moisture >= 0) {
            if (pin == 0) {
                String label = sharedPrefs.getString("prefMoistureSensorOneLabel", "Plant One");
                mSoilMoistureLabel1.setText(label);
                mSoilMoisture1.setText(Integer.toString(moisture));
                database.createRecord("Soil_Moisture1", datetime, Integer.toString(moisture));
            } else if (pin == 1) {
                String label = sharedPrefs.getString("prefMoistureSensorTwoLabel", "Plant Two");
                mSoilMoistureLabel2.setText(label);
                mSoilMoisture2.setText(Integer.toString(moisture));
                database.createRecord("Soil_Moisture2", datetime, Integer.toString(moisture));
            } else if (pin == 2) {
                String label = sharedPrefs.getString("prefMoistureSensorThreeLabel", "Plant Three");
                mSoilMoistureLabel3.setText(label);
                mSoilMoisture3.setText(Integer.toString(moisture));
                database.createRecord("Soil_Moisture3", datetime, Integer.toString(moisture));
            }
            Log.d(TAG, "Moisture: " + moisture);
        }
    }

    public void getLightValue() throws IOException, InterruptedException {
        TSL2561Sensor sensor = new TSL2561Sensor();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String datetime = sdf.format(new Date());

            DecimalFormat df = new DecimalFormat("#.####");
            double lux = Double.parseDouble(df.format(sensor.readLux()));
            if (lux >= 0) {
                Log.d(TAG, "LUX: " + lux);
                mLux.setText(Double.toString(lux));
                database.createRecord("Light_LUX", datetime, Double.toString(lux));
            }
            int ir = sensor.readIR();
            if (ir >= 0) {
                Log.d(TAG, "Infared: " + ir);
                mIrLight.setText(Integer.toString(ir));
                database.createRecord("Light_IR", datetime, Integer.toString(ir));
            }
            int full = sensor.readFull();
            if (full >= 0) {
                Log.d(TAG, "Ambient Light: " + full);
                mAmbientLight.setText(Integer.toString(full));
                database.createRecord("Light_Ambient", datetime, Integer.toString(full));
            }
            sensor.close();
        } catch (Exception e) {
            Log.d(TAG, "Err:" + e.getMessage() );
        }
    }

    public void getCO2Value() throws IOException, InterruptedException {
        CO2Sensor sensor = new CO2Sensor();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String datetime = sdf.format(new Date());

            int co2 = sensor.read();
            sensor.close();
            if (co2 >= 0) {
                Log.d(TAG, "CO2(PPM): " + co2);
                mCo2.setText(Integer.toString(co2));
                //database.createSensorRecords("CO2", datetime, Integer.toString(co2));
            }
        } catch (Exception e) {
            Log.d(TAG, "Err:" + e.getMessage() );
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
    private float convertCtoF(double temp) {
        return (float) temp * 9 / 5 + 32;
    }
}
