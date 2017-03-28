package org.thecongers.gardenthing.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GardenDatabase {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String TAG = "GardenThing";

    public final static String CO2_TABLE="CO2";
    public final static String HUMIDITY_TABLE="Humidity";
    public final static String LIGHT_AMBIENT_TABLE="Light_Ambient";
    public final static String LIGHT_IR_TABLE="Light_IR";
    public final static String LIGHT_LUX_TABLE="Light_LUX";
    public final static String TEMPERATURE_TABLE="Temperature";
    public final static String MOISTURE1_TABLE="Soil_Moisture1";
    public final static String MOISTURE2_TABLE="Soil_Moisture2";
    public final static String MOISTURE3_TABLE="Soil_Moisture3";
    public final static String JOURNAL_TABLE="Journal";
    public final static String ALARMS_TABLE="Alarms";

    /**
     *
     * @param context
     */
    public GardenDatabase(Context context){
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }

    public long createRecord(String table, String datetime, String entry){
        ContentValues values = new ContentValues();
        values.put("datetime", datetime);
        values.put("entry", entry);
        return database.insert(table, null, values);
    }

    public boolean deleteRecordByID(String table, int id)
    {
        return database.delete(table, "_id" + "=" + id, null) > 0;
    }

    public Cursor selectRecords(String table) {
        String[] cols = new String[] {"_id", "datetime", "entry"};
        Cursor mCursor = database.query(true, table, cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }

    public Cursor selectRecordsByDate(String table, String begindate, String enddate) {
        String[] cols = new String[] {"_id", "datetime", "entry"};
        String where = "datetime BETWEEN \'" + begindate + "\' AND \'" + enddate + "\'";
        Cursor mCursor = database.query(true, table, cols, where, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }

    public Cursor selectRecordLast(String table) {
        String[] cols = new String[] {"_id", "datetime", "entry"};
        String orderBy = "_id DESC LIMIT 1";
        Cursor mCursor = database.query(true, table, cols, null, null, null, null, orderBy, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }

    public void purgeDatabase(){
        Log.d(TAG, "Removing all data from the database");
        database.execSQL("DROP TABLE IF EXISTS CO2");
        database.execSQL("DROP TABLE IF EXISTS Humidity");
        database.execSQL("DROP TABLE IF EXISTS Light_Ambient");
        database.execSQL("DROP TABLE IF EXISTS Light_IR");
        database.execSQL("DROP TABLE IF EXISTS Light_LUX");
        database.execSQL("DROP TABLE IF EXISTS Temperature");
        database.execSQL("DROP TABLE IF EXISTS Soil_moisture1");
        database.execSQL("DROP TABLE IF EXISTS Soil_moisture2");
        database.execSQL("DROP TABLE IF EXISTS Soil_moisture3");
        database.execSQL("DROP TABLE IF EXISTS Journal");
        database.execSQL("DROP TABLE IF EXISTS Alarms");
        dbHelper.onCreate(database);
    }
}
