package org.thecongers.gardenthing;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.OrderedXYSeries;
import com.androidplot.xy.SampledXYSeries;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.ZoomEstimator;
import org.thecongers.gardenthing.utils.GardenDatabase;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class SoilMoistureActivity extends AppCompatActivity {

    private XYPlot plot;
    private GardenDatabase database;
    private SharedPreferences sharedPrefs;

    Number[] valueArray;
    Number[] dateArray;

    private static final String TAG = "GardenThing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_moisture);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        plot = (XYPlot) findViewById(R.id.plot);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Bundle b = getIntent().getExtras();
        int sensor = -1;
        if(b != null)
            sensor = b.getInt("key");

        //Fill valueArray and dateArray from database
        database = new GardenDatabase(this);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date currentdate = new Date();
        Date previousdate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentdate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        previousdate.setTime( calendar.getTime().getTime() );

        String curdatetime = sdf.format(currentdate);
        String predatetime = sdf.format(previousdate);
        Date date;

        Cursor cursor = database.selectRecordsByDate("Soil_moisture" + sensor,predatetime,curdatetime);
        valueArray = new Number[cursor.getCount()];
        dateArray = new Number[cursor.getCount()];
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                try {
                    date = sdf.parse(cursor.getString(1));
                    dateArray[i] = date.getTime();
                    valueArray[i] = Float.parseFloat(cursor.getString(2));
                    i = i + 1;
                }catch (ParseException e){
                    Log.d(TAG, "Err: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        String label ="";
        if (sensor == 1){
            label = sharedPrefs.getString("prefMoistureSensorOneLabel","Plant One");
        } else if (sensor == 2){
            label = sharedPrefs.getString("prefMoistureSensorTwoLabel","Plant Two");
        } else if (sensor == 3){
            label = sharedPrefs.getString("prefMoistureSensorThreeLabel","Plant Three");
        }

        plot.setTitle(label + " Moisture Last 24hrs");

        // Draw
        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(dateArray), Arrays.asList(valueArray), "Moisture");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        series1Format.setLegendIconEnabled(false);

        //TODO: Test
        if ( valueArray.length > 150 ) {
            SampledXYSeries sampledSeries =
                    new SampledXYSeries(series1, OrderedXYSeries.XOrder.ASCENDING, 2,150);
            // add a new series' to the xyplot:
            plot.addSeries(sampledSeries, series1Format);
            Log.d(TAG,"Sampled Series");
        } else {
            // add a new series' to the xyplot:
            plot.addSeries(series1, series1Format);
            Log.d(TAG,"UnSampled Series");
        }

        // enable autoselect of sampling level based on visible boundaries:
        plot.getRegistry().setEstimator(new ZoomEstimator());

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new Format() {

                    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd hh:mm");

                    @Override
                    public StringBuffer format(Object obj, StringBuffer toAppendTo,
                                               FieldPosition pos) {

                        long timestamp = ((Number) obj).longValue();
                        Date date = new Date(timestamp);
                        return dateFormat.format(date, toAppendTo, pos);
                    }

                    @Override
                    public Object parseObject(String source, ParsePosition pos) {
                        return null;

                    }
                });

    }
}
