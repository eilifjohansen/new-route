package no.enkeloversikt.newroute.new_route.database;

/**
 * Created by Agne Ødegaard on 03/11/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String databaseName = "NewRoute.db";
    private static final String tableName = "geoLocations";
    private static final int metersInADegree = 111300;

    public DatabaseHelper(Context context) {
        super(context, databaseName, null, 1);
    }

    /**
     * When the database is created
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + tableName + " (id int(11) AUTO_INCREMENT, lat decimal(10, 8) NOT NULL, lng decimal(11, 8) NOT NULL, visited tinyint(1) DEFAULT '0')");
    }

    /**
     * When the app is uninstalled`
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }


    /**
     * generate the points and put them inn the database
     * @param lat
     * @param lng
     * @param points
     * @return
     */
    public boolean createNewPoints(double lat, double lng, int points, int radius){
        LatLngCalculation geo = new LatLngCalculation();

        Log.v("gpstest", "db.createNewPoints");
        for (int i = 0; i < points; i++){

            GeoLocation loc = geo.newRandomLocation(lat, lng, radius);
            double newLat = loc.getLat();
            double newLng = loc.getLng();

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("lat", newLat);
            cv.put("lng", newLng);

            if(db.insert(tableName, null, cv) > -1) return false;

        }
        return true;
    }

    public boolean setPointAsVisited(){


        return true;
    }

    /**
     * fetch all gps points that are closer then 250m away
     * @param lat
     * @param lng
     * @return
     */
    public GeoLocation[] fetchNearBy(double lat, double lng){
        Log.v("gpstest", "db.fetchNearBy");
        SQLiteDatabase db = this.getWritableDatabase();

        // Haversine Formula
        Cursor c = db.rawQuery("SELECT geo.*, ( 6371 * acos( cos( radians(geo.lat) ) * cos( radians( "+Double.toString(lat)+" ) ) * cos( radians( "+Double.toString(lng)+" ) - radians(geo.lng) ) + sin( radians(geo.lat) ) * sin( radians( "+Double.toString(lat)+" ) ) ) ) AS distance FROM geoLocations as geo WHERE visited = 0 HAVING distance < 0.25 ORDER BY distance ",
                new String[] {});

        GeoLocation[] geoLocs = new GeoLocation[c.getCount()];

        try{
            while(c.moveToNext()){
                geoLocs[c.getPosition()] = new GeoLocation();
                geoLocs[c.getPosition()].setId(c.getInt(c.getColumnIndex("id")));
                geoLocs[c.getPosition()].setLat(c.getDouble(c.getColumnIndex("lat")));
                geoLocs[c.getPosition()].setLng(c.getDouble(c.getColumnIndex("lng")));
                geoLocs[c.getPosition()].setDistance(c.getFloat(c.getColumnIndex("distance")));
            }

        } finally {
            c.close();
        }

        return geoLocs;

    }

    /**
     * this is very similar to the function abow... need to make the code better if we have time.
     * @return
     */
    public GeoLocation[] fetchAll(){
        Log.v("gpstest", "db.fetchALL");
        SQLiteDatabase db = this.getWritableDatabase();

        // Haversine Formula
        Cursor c = db.rawQuery("SELECT * FROM geoLocations WHERE visited = 0", new String[]{});

        GeoLocation[] geoLocs = new GeoLocation[c.getCount()];

        try{
            while(c.moveToNext()){

                geoLocs[c.getPosition()] = new GeoLocation();
                geoLocs[c.getPosition()].setId(c.getInt(c.getColumnIndex("id")));
                geoLocs[c.getPosition()].setLat(c.getDouble(c.getColumnIndex("lat")));
                geoLocs[c.getPosition()].setLng(c.getDouble(c.getColumnIndex("lng")));
                geoLocs[c.getPosition()].setDistance(c.getFloat(c.getColumnIndex("distance")));

                Log.v("gpstest", Integer.toString(geoLocs[c.getPosition()].getId()));
            }

        } finally {
            c.close();
        }

        c.close();
        return geoLocs;

    }

}
