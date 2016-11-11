package no.enkeloversikt.newroute.new_route.database;

/**
 * Created by Agne Ødegaard on 03/11/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String databaseName = "NewRoute.db";
    private static final String tableName = "geoLocations";
    private static final int metersInADegree = 111300;

    private final String tableColValue = "val";
    private final String tableColType = "type";

    public DatabaseHelper(Context context) {
        super(context, databaseName, null, 1);
    }

    /**
     * When the database is created
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL("CREATE TABLE " + tableName + " ('id' INTEGER PRIMARY KEY, 'lat' decimal(10, 8) NOT NULL, 'lng' decimal(10, 8) NOT NULL, 'visited' int(1) DEFAULT '0', time datetime DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE data ('id' INTEGER PRIMARY KEY, 'type' text NOT NULL, 'val' text NOT NULL)");
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

    /**
     * removes all points that the user did not walk to, so the app can create new points at the users location.
     * @return
     */
    public boolean killPoints(){
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(tableName, "visited = 0", null) > 0;

    }

    public boolean killScore(){
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(tableName, "visited = 1", null) > 0;

    }

    public boolean setPointAsVisited(GeoLocation loc){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("visited", 1);

        db.update(tableName, cv, "id="+loc.getId(), null);

        return true;
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


    /**
     * Insert a row into the database
     * @param type
     * @param value
     * @return
     */
    private boolean insertData(String type, String value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(tableColValue, value);
        cv.put(tableColType, type);

        long result = db.insert("data", null, cv);

        return result > -1;
    }

    /**
     * Update a row in the database
     * @param type
     * @param value
     * @return
     */
    private boolean updateData(String type, String value){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(tableColValue, value);

        int result = db.update("data", cv, tableColType + " = ?", new String[] {String.valueOf(type) });

        return result > -1;
    }

    /**
     * Fetch a value from the database
     * @param type
     * @return value
     */
    public String fetchType(String type){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM data WHERE type = ?", new String[] {String.valueOf(type) });


        if(!c.moveToFirst()) {
            c.close();
            return null;
        }


        String val = c.getString(c.getColumnIndex(tableColValue));
        c.close();
        return val;

    }

    /**
     * Update or Insert a value into the database
     * @param type
     * @param value
     */
    public void updateOrInsert(String type, String value){
        if(!insertData(type, value)) {
            updateData(type, value);
        }
    }

}