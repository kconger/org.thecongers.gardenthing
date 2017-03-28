package org.thecongers.gardenthing;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.thecongers.gardenthing.utils.GardenDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
    private GardenDatabase database;
    private Context callingContext;

    private static final String TAG = "GardenThingWeb";

    public WebServer(Context context, int port) {
        super(port);
        callingContext = context;
    }

    public WebServer(Context context, String hostname, int port) {
        super(hostname, port);
        callingContext = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(callingContext);
        String basicAuth;
        int test = -1;
        String user = "gardener";
        String password = sharedPrefs.getString("prefWebPassword", "secret");

        basicAuth = user != null ? "Basic " + android.util.Base64.encodeToString((user + ":" + password).getBytes(), 16) : null;

        if (session.getHeaders().get("authorization") != null) {
            test = basicAuth.compareTo(session.getHeaders().get("authorization"));
        }

        if (basicAuth != null && !(test == 1)) {
            NanoHTTPD.Response response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html", "Needs authorization");
            response.addHeader("WWW-Authenticate", "Basic realm=\"Grove Garden\"");
            return response;
        }

        StringBuilder sb = new StringBuilder();
        String uri = String.valueOf(session.getUri());
        database = new GardenDatabase(callingContext);

        Log.d(TAG, "URI: " + uri);
        if (uri.startsWith("/json")) {
            Map<String, String> parms = session.getParms();
            String query = parms.get("q");
            Log.d(TAG, "Query: " + query);
            if (query.equals("overview")){
                Cursor cursor;

                cursor = database.selectRecordLast("Temperature");
                String temperature = "";
                if (cursor.moveToFirst()) {
                    temperature = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Humidity");
                String humidity = "";
                if (cursor.moveToFirst()) {
                    humidity = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Light_Ambient");
                String ambient = "";
                if (cursor.moveToFirst()) {
                    ambient = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Light_IR");
                String ir = "";
                if (cursor.moveToFirst()) {
                    ir = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Light_LUX");
                String lux = "";
                if (cursor.moveToFirst()) {
                    lux = cursor.getString(2);
                }

                cursor = database.selectRecordLast("CO2");
                String co2 = "";
                if (cursor.moveToFirst()) {
                    co2 = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Soil_Moisture1");
                String moisture1 = "";
                if (cursor.moveToFirst()) {
                    moisture1 = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Soil_Moisture2");
                String moisture2 = "";
                if (cursor.moveToFirst()) {
                    moisture2 = cursor.getString(2);
                }

                cursor = database.selectRecordLast("Soil_Moisture3");
                String moisture3 = "";
                if (cursor.moveToFirst()) {
                    moisture3 = cursor.getString(2);
                }

                String moisture1label = sharedPrefs.getString("prefMoistureSensorOneLabel", "");
                String moisture2label = sharedPrefs.getString("prefMoistureSensorTwoLabel", "");
                String moisture3label = sharedPrefs.getString("prefMoistureSensorThreeLabel", "");

                cursor.close();
                sb.append("{");
                sb.append("\n");
                sb.append("'ambient': '" + ambient + "',");
                sb.append("\n");
                sb.append("'co2': '" + co2 + "',");
                sb.append("\n");
                sb.append("'humidity': '" + humidity + "',");
                sb.append("\n");
                sb.append("'ir': '" + ir + "',");
                sb.append("\n");
                sb.append("'lux': '" + lux + "',");
                sb.append("\n");
                sb.append("'moisturesensor1': '" + moisture1 + "',");
                sb.append("\n");
                sb.append("'moisturesensor1label': '" + moisture1label + "',");
                sb.append("\n");
                sb.append("'moisturesensor2': '" + moisture2 + "',");
                sb.append("\n");
                sb.append("'moisturesensor2label': '" + moisture2label + "',");
                sb.append("\n");
                sb.append("'moisturesensor3': '" + moisture3 + "',");
                sb.append("\n");
                sb.append("'moisturesensor3label': '" + moisture3label + "',");
                sb.append("\n");
                sb.append("'temperature': '" + temperature + "'");
                sb.append("\n");
                sb.append("}");
            } else if (query.equals("temperature")){
                Cursor cursor = database.selectRecords("Temperature");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'temperature': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("humidity")){
                Cursor cursor = database.selectRecords("Humidity");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'humidity': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("soilmoisture1")){
                Cursor cursor = database.selectRecords("Soil_Moisture1");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'soilmoisture1': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("soilmoisture2")){
                Cursor cursor = database.selectRecords("Soil_Moisture2");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'soilmoisture2': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("soilmoisture3")){
                Cursor cursor = database.selectRecords("Soil_Moisture3");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'soilmoisture3': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("lux")){
                Cursor cursor = database.selectRecords("Light_LUX");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'lux': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("ir")){
                Cursor cursor = database.selectRecords("Light_IR");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'ir': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("ambientlight")){
                Cursor cursor = database.selectRecords("Light_Ambient");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'ambient': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("co2")){
                Cursor cursor = database.selectRecords("CO2");
                StringBuilder jsonValues = new StringBuilder();
                sb.append("{'co2': { 'values': [");

                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        jsonValues.append("{ 'date': '" + cursor.getString(1) + "', 'value': '" + cursor.getString(2) + "'}");
                        if (i < (cursor.getCount() - 1))
                            jsonValues.append(",");
                        i = i + 1;
                    } while (cursor.moveToNext());
                }
                cursor.close();
                sb.append(jsonValues);
                sb.append("] } }");
            } else if (query.equals("settings")){
                if (session.getMethod().toString().equals("POST")) {
                    try {
                        StringBuilder annotations = new StringBuilder();
                        StringBuilder graph = new StringBuilder();
                        session.parseBody(new HashMap<String, String>());
                        Map<String, String> postparms = session.getParms();
                        Log.d(TAG, session.getMethod() + " " + session.getParms());

                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        //TODO json settings post
                        editor.commit();

                    } catch (IOException | ResponseException e) {
                        e.printStackTrace();
                    }
                } else {
                    String interval = sharedPrefs.getString("prefSensorInterval","1");

                    String co2sensor = "false";
                    if (sharedPrefs.getBoolean("prefCO2Sensor",false)){
                        co2sensor = "true";
                    }

                    String lightsensor = "false";
                    if (sharedPrefs.getBoolean("prefLightSensor",false)){
                        lightsensor = "true";
                    }

                    String temperaturehumiditysensor = "false";
                    if (sharedPrefs.getBoolean("prefTemperatureHumiditySensor",false)){
                        temperaturehumiditysensor = "true";
                    }

                    String temperaturehumiditysensorport = sharedPrefs.getString("prefTemperatureHumiditySensorPort","7");

                    String temperatureunit = "false";
                    if (sharedPrefs.getBoolean("prefTemperatureUnit",false)){
                        temperatureunit = "true";
                    }

                    String moisturesensor1 = "false";
                    if (sharedPrefs.getBoolean("prefMoistureSensorOne",false)){
                        moisturesensor1 = "true";
                    }
                    String moisture1label = sharedPrefs.getString("prefMoistureSensorOneLabel","");

                    String moisturesensor2 = "false";
                    if (sharedPrefs.getBoolean("prefMoistureSensorTwo",false)){
                        moisturesensor2 = "true";
                    }
                    String moisture2label = sharedPrefs.getString("prefMoistureSensorTwoLabel","");


                    String moisturesensor3 = "false";
                    if (sharedPrefs.getBoolean("prefMoistureSensorThree",false)){
                        moisturesensor3 = "true";
                    }
                    String moisture3label = sharedPrefs.getString("prefMoistureSensorThreeLabel","");

                    String co2alerts = "false";
                    if (sharedPrefs.getBoolean("prefAlertsCO2",false)){
                        co2alerts = "true";
                    }
                    String co2alertlow = sharedPrefs.getString("prefAlertsCO2Low","");
                    String co2alerthigh = sharedPrefs.getString("prefAlertsCO2High","");

                    String humidityalerts = "false";
                    if (sharedPrefs.getBoolean("prefAlertsHumidity",false)){
                        humidityalerts = "true";
                    }
                    String humidityalertlow = sharedPrefs.getString("prefAlertsHumidityLow","");
                    String humidityalerthigh = sharedPrefs.getString("prefAlertsHumidityHigh","");

                    String lightalerts = "false";
                    if (sharedPrefs.getBoolean("prefAlertsLight",false)){
                        lightalerts = "true";
                    }
                    String lightalertlow = sharedPrefs.getString("prefAlertsLightLow","");
                    String lightalerthigh = sharedPrefs.getString("prefAlertsLightHigh","");

                    String moisturealerts = "false";
                    if (sharedPrefs.getBoolean("prefAlertsMoisture",false)){
                        moisturealerts = "true";
                    }
                    String moisturealertlow = sharedPrefs.getString("prefAlertsMoistureLow","");
                    String moisturealerthigh = sharedPrefs.getString("prefAlertsMoistureHigh","");

                    String temperaturealerts = "false";
                    if (sharedPrefs.getBoolean("prefAlertsTemperature",false)){
                        temperaturealerts = "true";
                    }
                    String temperaturealertlow = sharedPrefs.getString("prefAlertsTemperatureLow","");
                    String temperaturealerthigh = sharedPrefs.getString("prefAlertsTemperatureHigh","");

                    String webpassword = sharedPrefs.getString("prefWebPassword","secret");

                    sb.append("{\n");
                    sb.append("'interval': '" + interval + "',\n");
                    sb.append("'co2sensor': '" + co2sensor + "',\n");
                    sb.append("'thsensor': '" + temperaturehumiditysensor + "',\n");
                    sb.append("'thsensorport': '" + temperaturehumiditysensorport + "',\n");
                    sb.append("'temperatureunit': '" + temperatureunit + "',\n");
                    sb.append("'lightsensor': '" + lightsensor + "',\n");
                    sb.append("'moisturesensor1': '" + moisturesensor1 + "',\n");
                    sb.append("'moisturesensor1label': '" + moisture1label + "',\n");
                    sb.append("'moisturesensor2': '" + moisturesensor2 + "',\n");
                    sb.append("'moisturesensor2label': '" + moisture2label + "',\n");
                    sb.append("'moisturesensor3': '" + moisturesensor3 + "',\n");
                    sb.append("'moisturesensor3label': '" + moisture3label + "',\n");
                    sb.append("'co2alerts': '" + co2alerts + "',\n");
                    sb.append("'co2alertlow': '" + co2alertlow + "',\n");
                    sb.append("'co2alerthigh': '" + co2alerthigh + "',\n");
                    sb.append("'humidityalerts': '" + humidityalerts + "',\n");
                    sb.append("'humidityalertlow': '" + humidityalertlow + "',\n");
                    sb.append("'humidityalerthigh': '" + humidityalerthigh + "',\n");
                    sb.append("'lightalerts': '" + lightalerts + "',\n");
                    sb.append("'lightalertlow': '" + lightalertlow + "',\n");
                    sb.append("'lightalerthigh': '" + lightalerthigh + "',\n");
                    sb.append("'moisturealerts': '" + moisturealerts + "',\n");
                    sb.append("'moisturealertlow': '" + moisturealertlow + "',\n");
                    sb.append("'moisturealerthigh': '" + moisturealerthigh + "',\n");
                    sb.append("'temperaturealerts': '" + temperaturealerts + "',\n");
                    sb.append("'temperaturealertlow': '" + temperaturealertlow + "',\n");
                    sb.append("'temperaturealerthigh': '" + temperaturealerthigh + "',\n");
                    sb.append("'webpassword': '" + webpassword + "'\n");
                    sb.append("}");
                }
                Log.d(TAG,sb.toString());
                return newFixedLengthResponse(Response.Status.OK,"application/json", sb.toString());
            }
        } else if (uri.equals("/") || (uri.equals("/index.html"))) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/index.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder body = new StringBuilder();

            body.append("<h3>Current Readings</h3><table class=\"table table-striped\"><tbody>\n");
            if (sharedPrefs.getBoolean("prefTemperatureHumiditySensor", false)){
                Cursor cursor = database.selectRecordLast("Temperature");
                String temperature = "";
                if (cursor.moveToFirst()) {
                    temperature = cursor.getString(2);
                }
                String unit = "C";
                int temperatureLocalized = Integer.parseInt(temperature);
                if (sharedPrefs.getBoolean("prefTemperatureUnit", false)) {
                    unit = "F";
                    if (!temperature.equals("")){
                        temperatureLocalized = (int) Math.round(convertCtoF(Double.parseDouble(temperature)));
                    }
                }
                body.append("<tr><th scope=\"row\"><a href=\"/temperature\">Temperature</a></th><td>" + temperatureLocalized + unit + "</td></tr>\n");

                cursor = database.selectRecordLast("Humidity");
                String humidity = "";
                if (cursor.moveToFirst()) {
                    humidity = cursor.getString(2);
                }
                body.append("<tr><th scope=\"row\"><a href=\"/humidity\">Humidity</a></th><td>" + humidity + "%</td></tr>\n");
                cursor.close();
            }

            if (sharedPrefs.getBoolean("prefLightSensor", false)) {
                Cursor cursor = database.selectRecordLast("Light_Ambient");
                String ambient = "";
                if (cursor.moveToFirst()) {
                    ambient = cursor.getString(2);
                }
                body.append("<tr><th scope=\"row\"><a href=\"/lightambient\">Ambient Light</a></th><td>" + ambient + "</td></tr>\n");

                cursor = database.selectRecordLast("Light_IR");
                String ir = "";
                if (cursor.moveToFirst()) {
                    ir = cursor.getString(2);
                }
                body.append("<tr><th scope=\"row\"><a href=\"/lightir\">Infrared Light</a></th><td>" + ir + "</td></tr>\n");

                cursor = database.selectRecordLast("Light_LUX");
                String lux = "";
                if (cursor.moveToFirst()) {
                    lux = cursor.getString(2);
                }
                body.append("<tr><th scope=\"row\"><a href=\"/lightlux\">Light LUX</a></th><td>" + lux + "</td></tr>\n");
                cursor.close();
            }

            if (sharedPrefs.getBoolean("prefCO2Sensor", false)) {
                Cursor cursor = database.selectRecordLast("CO2");
                String co2 = "";
                if (cursor.moveToFirst()) {
                    co2 = cursor.getString(2);
                }
                body.append("<tr><th scope=\"row\"><a href=\"/co2\">Carbon Dioxide</a></th><td>" + co2 + "</td></tr>\n");
                cursor.close();
            }

            if (sharedPrefs.getBoolean("prefMoistureSensorOne", false)) {
                Cursor cursor = database.selectRecordLast("Soil_Moisture1");
                String moisture1 = "";
                if (cursor.moveToFirst()) {
                    moisture1 = cursor.getString(2);
                }
                String moisture1label = sharedPrefs.getString("prefMoistureSensorOneLabel","Plant One");
                body.append("<tr><th scope=\"row\"><a href=\"/soilmoisture1\">" + moisture1label + "</a></th><td>" + moisture1 + "</td></tr>\n");
                cursor.close();
            }

            if (sharedPrefs.getBoolean("prefMoistureSensorTwo", false)) {
                Cursor cursor = database.selectRecordLast("Soil_Moisture2");
                String moisture2 = "";
                if (cursor.moveToFirst()) {
                    moisture2 = cursor.getString(2);
                }
                String moisture2label = sharedPrefs.getString("prefMoistureSensorTwoLabel","Plant Two");
                body.append("<tr><th scope=\"row\"><a href=\"/soilmoisture2\">" + moisture2label + "</a></th><td>" + moisture2 + "</td></tr>\n");
                cursor.close();
            }

            if (sharedPrefs.getBoolean("prefMoistureSensorThree", false)) {
                Cursor cursor = database.selectRecordLast("Soil_Moisture3");
                String moisture3 = "";
                if (cursor.moveToFirst()) {
                    moisture3 = cursor.getString(2);
                }
                String moisture3label = sharedPrefs.getString("prefMoistureSensorThreeLabel","Plant Three");
                body.append("<tr><th scope=\"row\"><a href=\"/soilmoisture3\">" + moisture3label + "</a></th><td>" + moisture3 + "</td></tr>\n");
                cursor.close();
            }

            body.append("</tbody></table>");
            String content = sb.toString().replace("{{dashboard}}", body.toString());
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/temperature")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Temperature\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Temperature");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";

            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String temp = cursor.getString(2);
                    if (sharedPrefs.getBoolean("prefTemperatureUnit", true)) {
                        temp = Integer.toString(Math.round(convertCtoF(Double.parseDouble(cursor.getString(2)))));
                    }
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + temp + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            String unit = "C";
            int temperatureLocalized = Integer.parseInt(current);
            if (sharedPrefs.getBoolean("prefTemperatureUnit", true)) {
                unit = "F";
                if (!current.equals("")){
                    temperatureLocalized = Math.round(convertCtoF(Double.parseDouble(current)));
                }
            }

            graph.append("<h3>Temperature: " + Integer.toString(temperatureLocalized) + unit
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Temperature\" ],showRangeSelector: true, ylabel: 'Temperature', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=temperature"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/humidity")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Humidity\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Humidity");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            graph.append("<h3>Humidity: " + current + "%"
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Humidity\" ],showRangeSelector: true, ylabel: 'Humidity', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=humidity"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/lightambient")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Ambient Light\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Light_Ambient");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            graph.append("<h3>Ambient Light: " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Ambient Light\" ],showRangeSelector: true, ylabel: 'Ambient Light', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=lightambient"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/lightir")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Infrared Light\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Light_IR");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            graph.append("<h3>Infrared Light: " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Infrared Light\" ],showRangeSelector: true, ylabel: 'Infrared Light', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=lightir"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/lightlux")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Light LUX\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Light_LUX");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            graph.append("<h3>LUX: " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"LUX\" ],showRangeSelector: true, ylabel: 'LUX', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=lightlux"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/soilmoisture1")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Soil Moisture\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Soil_Moisture1");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            String label = sharedPrefs.getString("prefMoistureSensorOneLabel", "Plant One");
            graph.append("<h3>" + label + ": " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture1', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=soilmoisture1"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/soilmoisture2")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Soil Moisture\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Soil_Moisture2");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            String label = sharedPrefs.getString("prefMoistureSensorTwoLabel", "Plant Two");
            graph.append("<h3>" + label + ": " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture2', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=soilmoisture2"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/soilmoisture3")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/sensor.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Cursor cursor = database.selectRecords("Journal");
            StringBuilder annotations = new StringBuilder();

            annotations.append("g0.ready(function() { g0.setAnnotations([ ");
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    String[] datetime = cursor.getString(1).split(" ");
                    String date = datetime[0];
                    String time = datetime[1];
                    String entry = cursor.getString(2);

                    annotations.append("{ series: \"Temperature\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                    annotations.append("\n");
                    if (i < (cursor.getCount() - 1))
                        annotations.append(",");

                    i = i + 1;
                } while (cursor.moveToNext());
            }
            annotations.append(" ]); });");

            cursor = database.selectRecords("Soil_Moisture1");
            StringBuilder graph = new StringBuilder();
            StringBuilder graphValues = new StringBuilder();

            String current = "";
            i = 0;
            if (cursor.moveToFirst()) {
                do {
                    graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                    graphValues.append("\n");
                    if (i < (cursor.getCount() - 1))
                        graphValues.append(",");
                    else {
                        current = cursor.getString(2);
                    }

                    i = i + 1;
                } while (cursor.moveToNext());
            }

            String label = sharedPrefs.getString("prefMoistureSensorThreeLabel", "Plant Three");
            graph.append("<h3>" + label + ": " + current
                    + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                    + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                    + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture3', legend: 'always'});"
                    + annotations
                    + "</script><br><div id=\"divSelectedEntry\"></div><br>"
                    + "<button id=\"btnDownload\" class=\"btn btn-lg btn-primary btn-block\" type=\"button\" onclick=\"location.href=\'/export?id=soilmoisture3"
                    + "\';\">Download Data</button>");

            cursor.close();
            String content = sb.toString().replace("{{graph}}", graph.toString());

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/reporting")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/reporting.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String curdatetime = sdf.format(new Date());
            curdatetime = curdatetime.replace(" ", "T");

            if (session.getMethod().toString().equals("POST")) {
                String content = "";
                String begintime = "";
                String endtime = "";
                try {
                    StringBuilder annotations = new StringBuilder();
                    StringBuilder graph = new StringBuilder();
                    session.parseBody(new HashMap<String, String>());
                    Map<String, String> parms = session.getParms();
                    Log.d(TAG, session.getMethod() + " " + session.getParms());
                    begintime = parms.get("inputDateTimeBegin").replace("T", " ");
                    endtime = parms.get("inputDateTimeEnd").replace("T", " ");
                    if (parms.get("inputJournal") != null){
                        annotations.append("g0.ready(function() { g0.setAnnotations([ ");
                        Cursor cursor = database.selectRecordsByDate("Journal", begintime, endtime);
                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                String[] datetime = cursor.getString(1).split(" ");
                                String date = datetime[0];
                                String time = datetime[1];
                                String entry = cursor.getString(2);

                                annotations.append("{ series: \"{{anolabel}}\", x: new Date(\"" + date.replace("-","/") + " " + time + "\").getTime(), shortText: \"N\", text: \"" + entry + "\", tickHeight: 4, clickHandler: function() { document.getElementById(\"divSelectedEntry\").innerHTML = \'<table class=\"table table-striped\"><thead><tr><th>Date/Time</th><th>Entry</th></tr></thead><tbody><tr><th scope=\"row\">" + date + " " + time + "</th><td>" + entry + "</td></tr></tbody></table><br/>\';}},");
                                annotations.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    annotations.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }
                        annotations.append(" ]); });");
                    }
                    if (parms.get("inputSoilMoisture1") != null){
                        Cursor cursor = database.selectRecordsByDate("Soil_Moisture1", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        String label = sharedPrefs.getString("prefMoistureSensorOneLabel", "Plant One");
                        graph.append("<h3>" + label
                                + "</h3><div id=\"graphdiv0\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g0 = new Dygraph(document.getElementById(\"graphdiv0\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture1', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", label)
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }
                    if (parms.get("inputSoilMoisture2") != null){
                        Cursor cursor = database.selectRecordsByDate("Soil_Moisture2", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        String label = sharedPrefs.getString("prefMoistureSensorTwoLabel", "Plant Two");
                        graph.append("<h3>" + label
                                + "</h3><div id=\"graphdiv1\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g1 = new Dygraph(document.getElementById(\"graphdiv1\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture2', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", label).replace("g0","g1")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }
                    if (parms.get("inputSoilMoisture3") != null){
                        Cursor cursor = database.selectRecordsByDate("Soil_Moisture3", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        String label = sharedPrefs.getString("prefMoistureSensorTwoLabel", "Plant Three");
                        graph.append("<h3>" + label
                                + "</h3><div id=\"graphdiv2\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g2 = new Dygraph(document.getElementById(\"graphdiv2\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Moisture\" ],showRangeSelector: true, ylabel: 'Soil Moisture3', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", label).replace("g0","g2")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }
                    if (parms.get("inputTemperature") != null){
                        Cursor cursor = database.selectRecordsByDate("Temperature", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        graph.append("<h3>Temperature"
                                + "</h3><div id=\"graphdiv3\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g3 = new Dygraph(document.getElementById(\"graphdiv3\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Temperature\" ],showRangeSelector: true, ylabel: 'Temperature', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", "Temperature").replace("g0","g3")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }
                    if (parms.get("inputHumidity") != null){
                        Cursor cursor = database.selectRecordsByDate("Humidity", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        graph.append("<h3>Humidity"
                                + "</h3><div id=\"graphdiv4\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g4 = new Dygraph(document.getElementById(\"graphdiv4\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Humidity\" ],showRangeSelector: true, ylabel: 'Humidity', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", "Humidity").replace("g0","g4")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();

                    }
                    if (parms.get("inputAmbient") != null){
                        Cursor cursor = database.selectRecordsByDate("Light_Ambient", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        graph.append("<h3>Ambient Light"
                                + "</h3><div id=\"graphdiv5\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g5 = new Dygraph(document.getElementById(\"graphdiv5\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Ambient Light\" ],showRangeSelector: true, ylabel: 'Ambient Light', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", "Ambient Light").replace("g0","g5")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();

                    }
                    if (parms.get("inputIR") != null){
                        Cursor cursor = database.selectRecordsByDate("Light_IR", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        graph.append("<h3>Infrared Light"
                                + "</h3><div id=\"graphdiv6\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g6 = new Dygraph(document.getElementById(\"graphdiv6\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Infrared Light\" ],showRangeSelector: true, ylabel: 'Infrared Light', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", "Infrared Light").replace("g0","g6")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }
                    if (parms.get("inputLUX") != null){
                        Cursor cursor = database.selectRecordsByDate("Light_LUX", begintime, endtime);
                        StringBuilder graphValues = new StringBuilder();

                        int i = 0;
                        if (cursor.moveToFirst()) {
                            do {
                                graphValues.append("[  new Date(\"" + cursor.getString(1) + "\")," + cursor.getString(2) + "]");
                                graphValues.append("\n");
                                if (i < (cursor.getCount() - 1))
                                    graphValues.append(",");

                                i = i + 1;
                            } while (cursor.moveToNext());
                        }

                        graph.append("<h3>Light LUX"
                                + "</h3><div id=\"graphdiv7\" style=\"width:100%; height:300px;\"></div>"
                                + "<script type=\"text/javascript\">g7 = new Dygraph(document.getElementById(\"graphdiv7\"),"
                                + "[" + graphValues.toString() + "],{labels:[ \"Date\", \"Light LUX\" ],showRangeSelector: true, ylabel: 'Light LUX', legend: 'always'});"
                                + annotations.toString().replace("{{anolabel}}", "Light LUX").replace("g0","g7")
                                + "</script><br><div id=\"divSelectedEntry\"></div><br>");

                        cursor.close();
                    }

                    content = sb.toString().replace("{{results}}", graph.toString());

                } catch (IOException | ResponseException e){
                    e.printStackTrace();
                }
                String btime = begintime.replace(" ", "T");
                String etime = endtime.replace(" ", "T");
                return newFixedLengthResponse(Response.Status.OK, "text/html", content.replace("{{begindatetime}}", btime).replace("{{enddatetime}}", etime));
            } else {
                String content = sb.toString().replace("{{begindatetime}}", curdatetime).replace("{{enddatetime}}", curdatetime);
                content = content.replace("{{results}}", "");
                return newFixedLengthResponse(Response.Status.OK, "text/html", content);
            }
        } else if (uri.equals("/settings")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/settings.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String interval = sharedPrefs.getString("prefSensorInterval","60000");

            String co2sensor = "";
            if (sharedPrefs.getBoolean("prefCO2Sensor",false)){
                co2sensor = "checked";
            }

            String lightsensor = "";
            if (sharedPrefs.getBoolean("prefLightSensor",false)){
                lightsensor = "checked";
            }

            String temperaturehumiditysensor = "";
            if (sharedPrefs.getBoolean("prefTemperatureHumiditySensor",false)){
                temperaturehumiditysensor = "checked";
            }

            String port = sharedPrefs.getString("prefTemperatureHumiditySensorPort","7");

            String temperatureunit = "";
            if (sharedPrefs.getBoolean("prefTemperatureUnit",false)){
                temperatureunit = "checked";
            }

            String moisturesensor1 = "";
            if (sharedPrefs.getBoolean("prefMoistureSensorOne",false)){
                moisturesensor1 = "checked";
            }
            String moisture1label = sharedPrefs.getString("prefMoistureSensorOneLabel","");

            String moisturesensor2 = "";
            if (sharedPrefs.getBoolean("prefMoistureSensorTwo",false)){
                moisturesensor2 = "checked";
            }
            String moisture2label = sharedPrefs.getString("prefMoistureSensorTwoLabel","");

            String moisturesensor3 = "";
            if (sharedPrefs.getBoolean("prefMoistureSensorThree",false)){
                moisturesensor3 = "checked";
            }
            String moisture3label = sharedPrefs.getString("prefMoistureSensorThreeLabel","");

            String co2alerts = "";
            if (sharedPrefs.getBoolean("prefAlertsCO2",false)){
                co2alerts = "checked";
            }
            String co2alertlow = sharedPrefs.getString("prefAlertsCO2Low","");
            String co2alerthigh = sharedPrefs.getString("prefAlertsCO2High","");

            String humidityalerts = "";
            if (sharedPrefs.getBoolean("prefAlertsHumidity",false)){
                humidityalerts = "checked";
            }
            String humidityalertlow = sharedPrefs.getString("prefAlertsHumidityLow","");
            String humidityalerthigh = sharedPrefs.getString("prefAlertsHumidityHigh","");

            String lightalerts = "";
            if (sharedPrefs.getBoolean("prefAlertsLight",false)){
                lightalerts = "checked";
            }
            String lightalertlow = sharedPrefs.getString("prefAlertsLightLow","");
            String lightalerthigh = sharedPrefs.getString("prefAlertsLightHigh","");

            String moisturealerts = "";
            if (sharedPrefs.getBoolean("prefAlertsMoisture",false)){
                moisturealerts = "checked";
            }
            String moisturealertlow = sharedPrefs.getString("prefAlertsMoistureLow","");
            String moisturealerthigh = sharedPrefs.getString("prefAlertsMoistureHigh","");

            String temperaturealerts = "";
            if (sharedPrefs.getBoolean("prefAlertsTemperature",false)){
                temperaturealerts = "checked";
            }
            String temperaturealertlow = sharedPrefs.getString("prefAlertsTemperatureLow","");
            String temperaturealerthigh = sharedPrefs.getString("prefAlertsTemperatureHigh","");

            String webpassword = sharedPrefs.getString("prefWebPassword","secret");

            String content = sb.toString();
            String intervalstring = "value=\"{{interval}}\"";
            intervalstring = intervalstring.replace("{{interval}}", interval);
            String intervalstringchecked = intervalstring.replace(intervalstring, intervalstring + " selected");
            content = content.replace(intervalstring, intervalstringchecked);
            content = content.replace("{{co2sensor}}", co2sensor);
            content = content.replace("{{lightsensor}}", lightsensor);
            content = content.replace("{{thsensor}}", temperaturehumiditysensor);
            String thportstring = "value=\"{{port}}\"";
            thportstring = thportstring.replace("{{port}}", port);
            String thportstringchecked = thportstring.replace(thportstring, thportstring + " selected");
            content = content.replace(thportstring, thportstringchecked);
            content = content.replace("{{moisturesensor1}}", moisturesensor1);
            content = content.replace("{{moisture1label}}", moisture1label);
            content = content.replace("{{moisturesensor2}}", moisturesensor2);
            content = content.replace("{{moisture2label}}", moisture2label);
            content = content.replace("{{moisturesensor3}}", moisturesensor3);
            content = content.replace("{{moisture3label}}", moisture3label);
            content = content.replace("{{fahrenheit}}", temperatureunit);
            content = content.replace("{{co2alerts}}", co2alerts);
            content = content.replace("{{co2alertlow}}", co2alertlow);
            content = content.replace("{{co2alerthigh}}", co2alerthigh);
            content = content.replace("{{lightalerts}}", lightalerts);
            content = content.replace("{{lightalertlow}}", lightalertlow);
            content = content.replace("{{lightalerthigh}}", lightalerthigh);
            content = content.replace("{{temperaturealerts}}", temperaturealerts);
            content = content.replace("{{temperaturealertlow}}", temperaturealertlow);
            content = content.replace("{{temperaturealerthigh}}", temperaturealerthigh);
            content = content.replace("{{humidityalerts}}", humidityalerts);
            content = content.replace("{{humidityalertlow}}", humidityalertlow);
            content = content.replace("{{humidityalerthigh}}", humidityalerthigh);
            content = content.replace("{{moisturealerts}}", moisturealerts);
            content = content.replace("{{moisturealertlow}}", moisturealertlow);
            content = content.replace("{{moisturealerthigh}}", moisturealerthigh);
            content = content.replace("{{webpassword}}", webpassword);

            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/settingsSave")) {
            try {
                session.parseBody(new HashMap<String, String>());
                Map<String, String> parms = session.getParms();
                Log.d(TAG, session.getMethod() + " " + session.getParms());

                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putString("prefSensorInterval", parms.get("inputPollingPeriod"));
                if (parms.get("inputCO2Sensor") == null){
                    editor.putBoolean("prefCO2Sensor", false);
                } else {
                    editor.putBoolean("prefCO2Sensor", true);
                }
                if (parms.get("inputLightSensor") == null) {
                    editor.putBoolean("prefLightSensor", false);
                } else {
                    editor.putBoolean("prefLightSensor", true);
                }
                if (parms.get("inputTHSensor") == null) {
                    editor.putBoolean("prefTemperatureHumiditySensor", false);
                } else {
                    editor.putBoolean("prefTemperatureHumiditySensor", true);
                }
                editor.putString("prefTemperatureHumiditySensorPort", parms.get("inputTHLocation"));
                if (parms.get("inputMoistureSensor1") == null) {
                    editor.putBoolean("prefMoistureSensorOne", false);
                } else {
                    editor.putBoolean("prefMoistureSensorOne", true);
                }
                editor.putString("prefMoistureSensorOneLabel", parms.get("inputMoistureSensor1Label"));
                if (parms.get("inputMoistureSensor2") == null) {
                    editor.putBoolean("prefMoistureSensorTwo", false);
                } else {
                    editor.putBoolean("prefMoistureSensorTwo", true);
                }
                editor.putString("prefMoistureSensorTwoLabel", parms.get("inputMoistureSensor2Label"));
                if (parms.get("inputMoistureSensor3") == null) {
                    editor.putBoolean("prefMoistureSensorThree", false);
                } else {
                    editor.putBoolean("prefMoistureSensorThree", true);
                }
                editor.putString("prefMoistureSensorThreeLabel", parms.get("inputMoistureSensor3Label"));
                if (parms.get("inputFahrenheit") == null) {
                    editor.putBoolean("prefTemperatureUnit", false);
                } else {
                    editor.putBoolean("prefTemperatureUnit", true);
                }
                if (parms.get("inputCO2Alerts") == null) {
                    editor.putBoolean("prefAlertsCO2", false);
                } else {
                    editor.putBoolean("prefAlertsCO2", true);
                }
                editor.putString("prefAlertsCO2Low",parms.get("inputCO2AlertLow"));
                editor.putString("prefAlertsCO2High",parms.get("inputCO2AlertHigh"));
                if (parms.get("inputLightAlerts") == null) {
                    editor.putBoolean("prefAlertsLight", false);
                } else {
                    editor.putBoolean("prefAlertsLight", true);
                }
                editor.putString("prefAlertsLightLow",parms.get("inputLightAlertLow"));
                editor.putString("prefAlertsLightHigh",parms.get("inputLightAlertHigh"));
                if (parms.get("inputTemperatureAlerts") == null) {
                    editor.putBoolean("prefAlertsTemperature", false);
                } else {
                    editor.putBoolean("prefAlertsTemperature", true);
                }
                editor.putString("prefAlertsTemperatureLow",parms.get("inputTemperatureAlertLow"));
                editor.putString("prefAlertsTemperatureHigh",parms.get("inputTemperatureAlertHigh"));
                if (parms.get("inputHumidityAlerts") == null) {
                    editor.putBoolean("prefAlertsHumidity", false);
                } else {
                    editor.putBoolean("prefAlertsHumidity", true);
                }
                editor.putString("prefAlertsHumidityLow",parms.get("inputHumidityAlertLow"));
                editor.putString("prefAlertsHumidityHigh",parms.get("inputHumidityAlertHigh"));
                if (parms.get("inputMoistureAlerts") == null) {
                    editor.putBoolean("prefAlertsMoisture", false);
                } else {
                    editor.putBoolean("prefAlertsMoisture", true);
                }
                editor.putString("prefAlertsMoistureLow",parms.get("inputMoistureAlertLow"));
                editor.putString("prefAlertsMoistureHigh",parms.get("inputMoistureAlertHigh"));
                editor.putString("prefWebPassword", parms.get("inputGardenerPassword"));

                editor.commit();

                try {
                    InputStream inputStream = callingContext.getAssets().open("html/message.html");
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String readLine;
                    while ((readLine = br.readLine()) != null) {
                        sb.append(readLine);
                        sb.append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String content = sb.toString().replace("{{message}}","Settings have been saved");
                return newFixedLengthResponse(Response.Status.OK,"text/html", content);

            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }

            String content = sb.toString().replace("{{message}}","There was an error saving settings");
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/databasePurge")) {
            database.purgeDatabase();
            try {
                InputStream inputStream = callingContext.getAssets().open("html/message.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String content = sb.toString().replace("{{message}}","Data has been deleted");
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/journal")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("html/journal.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String datetime = sdf.format(new Date());
            datetime = datetime.replace(" ", "T");
            Cursor cursor = database.selectRecords("Journal");
            StringBuilder journalentries = new StringBuilder();
            journalentries.append("<thead><tr><th>Date/Time</th><th>Entry</th><th>Delete</th></tr></thead><tbody>");

            if (cursor.moveToFirst()) {
                do {
                    journalentries.append("<tr><th scope=\"row\">" + cursor.getString(1) + "</th><td>" + cursor.getString(2) + "</td><td><a href=/journalDelete?id=" + cursor.getString(0) + ">&times;</a></td></tr>");
                } while (cursor.moveToNext());
            }
            cursor.close();
            journalentries.append("</tbody>");

            String content = sb.toString().replace("{{journal}}", journalentries.toString());
            content = content.replace("{{curdatetime}}", datetime);
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/journalSave")) {
            try {
                session.parseBody(new HashMap<String, String>());
                Map<String, String> parms = session.getParms();

                String datetime = parms.get("inputDateTime").replace("T"," ");
                String entry = parms.get("inputEntry");
                database.createRecord("Journal", datetime, entry);
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }

            try {
                InputStream inputStream = callingContext.getAssets().open("html/message.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String content = sb.toString().replace("{{message}}","Journal entry saved");
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/journalDelete")) {
            try {
                session.parseBody(new HashMap<String, String>());
                Map<String, String> parms = session.getParms();

                Integer id = Integer.parseInt(parms.get("id"));

                database.deleteRecordByID("Journal", id);
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
            try {
                InputStream inputStream = callingContext.getAssets().open("html/message.html");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String content = sb.toString().replace("{{message}}","Journal Entry Deleted");
            return newFixedLengthResponse(Response.Status.OK,"text/html", content);
        } else if (uri.equals("/export")) {
            try {
                session.parseBody(new HashMap<String, String>());
                Map<String, String> parms = session.getParms();
                String sensor = parms.get("id");
                Log.d(TAG, "Sensor: " + sensor);
                String tablename = null;
                String header = null;
                if ( sensor.equals("temperature")){
                    tablename = "Temperature";
                    header = "Date,Temperature(C)";
                } else if ( sensor.equals("humidity")){
                    tablename = "Humidity";
                    header = "Date,Humidity(%)";
                } else if ( sensor.equals("soilmoisture1")){
                    tablename = "Soil_Moisture1";
                    header = "Date,Moisture";
                } else if ( sensor.equals("soilmoisture2")){
                    tablename = "Soil_Moisture2";
                    header = "Date,Moisture";
                }  else if ( sensor.equals("soilmoisture3")){
                    tablename = "Soil_Moisture3";
                    header = "Date,Moisture";
                }  else if ( sensor.equals("lightambient")){
                    tablename = "Light_Ambient";
                    header = "Date,Ambient Light";
                }  else if ( sensor.equals("lightir")){
                    tablename = "Light_IR";
                    header = "Date,Infrared Light";
                }  else if ( sensor.equals("lightlux")){
                    tablename = "Light_LUX";
                    header = "Date,LUX";
                }  else if ( sensor.equals("co2")){
                    tablename = "CO2";
                    header = "Date,CO2(ppm)";
                }  else if ( sensor.equals("journal")){
                    tablename = "Journal";
                    header = "Date,Journal Entry";
                }
                sb.append(header);
                sb.append("\n");

                Cursor cursor = database.selectRecords(tablename);
                if (cursor.moveToFirst()) {
                    do {
                        sb.append(cursor.getString(1) + "," + cursor.getString(2));
                        sb.append("\n");
                    } while (cursor.moveToNext());
                }
                cursor.close();

                NanoHTTPD.Response response = newFixedLengthResponse(Response.Status.OK, "text/csv", sb.toString());
                response.addHeader("Content-Disposition", "attachment; filename=" + tablename + ".csv");
                return response;


            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }
        } else if (uri.equals("/js/dygraph-combined.js")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("js/dygraph-combined.js");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/javascript", sb.toString());
        } else if (uri.equals("/js/jquery-1.11.3.min.js")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("js/jquery-1.11.3.min.js");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/javascript", sb.toString());
        }  else if (uri.equals("/js/synchronizer.js")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("js/synchronizer.js");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/javascript", sb.toString());
        } else if (uri.equals("/css/bootstrap.min.css")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("css/bootstrap.min.css");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/css", sb.toString());
        } else if (uri.equals("/css/bootstrap.min.css.map")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("css/bootstrap.min.css.map");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/css", sb.toString());
        } else if (uri.equals("/css/jumbotron-narrow.css")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("css/jumbotron-narrow.css");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/css", sb.toString());
        } else if (uri.equals("/css/settings.css")) {
            try {
                InputStream inputStream = callingContext.getAssets().open("css/settings.css");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = "";
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                    sb.append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK,"text/css", sb.toString());
        }
        return newFixedLengthResponse(sb.toString());
    }

    private float convertCtoF(double temp) {
        return (float) temp * 9 / 5 + 32;
    }
}
