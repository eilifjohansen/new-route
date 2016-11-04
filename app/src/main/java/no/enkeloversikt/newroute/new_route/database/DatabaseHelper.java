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


        db.execSQL("CREATE TABLE " + tableName + " ('id' INTEGER PRIMARY KEY, 'lat' decimal(10, 8) NOT NULL, 'lng' decimal(10, 8) NOT NULL, 'visited' int(1) DEFAULT '0')");
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
            db.insert(tableName, null, cv);

        }
        return true;
    }

    public boolean setPointAsVisited(GeoLocation loc){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("visited", 1);

        db.update(tableName, cv, "id="+loc.getId(), null);

        return true;
    }

    public GeoLocation[] haversine(){
        //SELECT geo.*, ( 6371 * acos( cos( radians(geo.lat) ) * cos( radians( 37 ) ) * cos( radians( 5 ) - radians(geo.lng) ) + sin( radians(geo.lat) ) * sin( radians( 37 ) ) ) ) AS distance FROM geoLocations as geo WHERE visited = 0 GROUP BY geo.id HAVING distance < 0.25 ORDER BY distance

        GeoLocation[] locs = new GeoLocation[5];
        return locs;
    }

    /**
     * this is very similar to the function abow... need to make the code better if we have time.
     * @return
     */
    public GeoLocation[] fetchAll(){
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
                geoLocs[c.getPosition()].setDistance(0);
            }

        } finally {
            c.close();
        }

        c.close();
        return geoLocs;

    }

    public int getScore(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM geoLocations WHERE visited = 1", new String[]{});
        return c.getCount();
    }

}
