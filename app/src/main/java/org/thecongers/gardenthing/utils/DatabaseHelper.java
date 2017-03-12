package org.thecongers.gardenthing.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;
    private static final String DATABASE_NAME = "GardenDatabase";

    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String CO2_DATABASE_CREATE = "create table CO2( _id integer primary key,datetime text not null,entry text not null);";
    private static final String HUMIDITY_DATABASE_CREATE = "create table Humidity( _id integer primary key,datetime text not null,entry text not null);";
    private static final String LIGHT_AMBIENT_DATABASE_CREATE = "create table Light_Ambient( _id integer primary key,datetime text not null,entry text not null);";
    private static final String LIGHT_IR_DATABASE_CREATE = "create table Light_IR( _id integer primary key,datetime text not null,entry text not null);";
    private static final String LIGHT_LUX_DATABASE_CREATE = "create table Light_LUX( _id integer primary key,datetime text not null,entry text not null);";
    private static final String TEMPERATURE_DATABASE_CREATE = "create table Temperature( _id integer primary key,datetime text not null,entry text not null);";
    private static final String MOISTURE1_DATABASE_CREATE = "create table Soil_Moisture1( _id integer primary key,datetime text not null,entry text not null);";
    private static final String MOISTURE2_DATABASE_CREATE = "create table Soil_Moisture2( _id integer primary key,datetime text not null,entry text not null);";
    private static final String MOISTURE3_DATABASE_CREATE = "create table Soil_Moisture3( _id integer primary key,datetime text not null,entry text not null);";
    private static final String JOURNAL_DATABASE_CREATE = "create table Journal( _id integer primary key,datetime text not null,entry text not null);";
    private static final String ALARMS_DATABASE_CREATE = "create table Alarms( _id integer primary key,datetime text not null,type integer not null);";

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CO2_DATABASE_CREATE);
        database.execSQL(HUMIDITY_DATABASE_CREATE);
        database.execSQL(LIGHT_AMBIENT_DATABASE_CREATE);
        database.execSQL(LIGHT_IR_DATABASE_CREATE);
        database.execSQL(LIGHT_LUX_DATABASE_CREATE);
        database.execSQL(TEMPERATURE_DATABASE_CREATE);
        database.execSQL(MOISTURE1_DATABASE_CREATE);
        database.execSQL(MOISTURE2_DATABASE_CREATE);
        database.execSQL(MOISTURE3_DATABASE_CREATE);
        database.execSQL(JOURNAL_DATABASE_CREATE);
        database.execSQL(ALARMS_DATABASE_CREATE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
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
        onCreate(database);
    }
}
