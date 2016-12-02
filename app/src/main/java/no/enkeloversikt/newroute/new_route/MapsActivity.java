package no.enkeloversikt.newroute.new_route;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import no.enkeloversikt.newroute.new_route.database.DatabaseHelper;
import no.enkeloversikt.newroute.new_route.database.GeoLocation;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseHelper db;

    public double latitude = 0.0;
    public double longitude = 0.0;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ProgressDialog apiProgress;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private double lastLat = 0;
    private double lastLng = 0;

    private Vibrator vib;

    private int total_points = 5;

    private int points_radius = 500;

    private boolean firstTimeLoadLocation = true;

    private TextView score;

    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new DatabaseHelper(this);
        score = (TextView) findViewById(R.id.scoreView);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        apiProgress = new ProgressDialog(this);
        apiProgress.setMessage(getString(R.string.loading_location_dialog));
        apiProgress.show();




        level = db.fetchType("level");
        Log.v("test", "onCreate1: " + db.fetchType("level"));
        if(level == null){
            db.updateOrInsert("level", "0");
            level = "0";
        }

        Log.v("test", "onCreate2: " + db.fetchType("level"));

        requestFineLocationPermit();



        final Intent finishedActivity = new Intent(this, FinishedActivity.class);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(finishedActivity);
            }
        });

    }


    /**
     * Permission request for Android 6.0 and later
     */
    public void requestFineLocationPermit() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            requestLoc();
        }
    }

    /**
     * Permission request for Android 6.0 and later
     *
     * @param requestCode  int
     * @param permissions  string array
     * @param grantResults int array
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Register the listener with the Location Manager to receive location updates
                    requestLoc();

                } else {
                    requestFineLocationPermit();
                }
            }
        }
    }

    /**
     * request location updates
     */
    public void requestLoc() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(lastKnownLocation != null){
            apiProgress.hide();
            apiProgress.dismiss();

            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();

            setLocations(latitude, longitude);
        }

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                apiProgress.hide();
                apiProgress.dismiss();

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                calcTotalDistance(latitude, longitude);
                setLocations(latitude, longitude);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(getBaseContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "GPS disabled: Please enable your location service", Toast.LENGTH_SHORT).show();
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }
    public void updateCamera(double lat, double lng){
        if(mMap == null) return;

        LatLng current = new LatLng(lat, lng);
        if(firstTimeLoadLocation){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
            firstTimeLoadLocation = false;
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(current));
        }
    }

    public void calcTotalDistance(double lat, double lng){

        if(lastLat == 0){
            lastLat = lat;
            lastLng = lng;
            return;
        }
        float results[] = new float[2];
        Location.distanceBetween(lat, lng, lastLat, lastLng, results);

        String dist = db.fetchType("totalDistance");

        if(dist == null){
            db.updateOrInsert("totalDistance", Float.toString(results[0]));
        } else {
            double addedDist = results[0] + Double.parseDouble(dist);
            db.updateOrInsert("totalDistance", Double.toString(addedDist));
        }

        lastLat = lat;
        lastLng = lng;

    }


    public void setLocations(double lat, double lng){
        if(mMap == null) return;

        updateCamera(lat, lng);

        GeoLocation[] allGeoLocs = db.fetchAll();

        float results[] = new float[2];
        mMap.clear();
        for (GeoLocation loc : allGeoLocs) {

            Circle spot = mMap.addCircle(new CircleOptions()
                    .center(loc.getLatLng())
                    .radius(125)
                    .strokeColor(Color.argb(70, 229, 57, 53))
                    .fillColor(Color.argb(40, 244, 67, 54)));

            Location.distanceBetween( lat, lng,
                    spot.getCenter().latitude, spot.getCenter().longitude, results);

            if( results[0] < spot.getRadius() ){
                vib.vibrate(500);
                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setTitle(R.string.alert_title);
                alert.setMessage(R.string.congratulations);
                alert.setPositiveButton(R.string.ok, null);
                alert.show();
                db.setPointAsVisited(loc);
                spot.remove();
            }
        }

        allGeoLocs = db.fetchAll();
        if(allGeoLocs.length <= 0){
            int nextLevel = Integer.parseInt(level);
            nextLevel = nextLevel + 1;
            level = Integer.toString(nextLevel);
            db.updateData("level", level);

            int points = (int) (nextLevel * 1.5 + 1);

            if(points > 10){
                points = 10;
            }

            int radius = points_radius * nextLevel / 4;

            if(radius < 500){
                radius = 500;
            }

            if(radius > 3000){
                radius = 3000;
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
            alert.setTitle("Level " + nextLevel);
            alert.setMessage("Congratulations you are now level " + nextLevel + ", catch " + points + " more to level up. Radius: " + radius + " meter");
            alert.setPositiveButton(R.string.ok, null);
            alert.show();

            db.createNewPoints(lat, lng, points, radius);
        }

        String scoreText = "Level: " + db.fetchType("level");
        score.setText(scoreText);

    }

    /**
     * Google maps init
     *
     * @param googleMap mMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        setLocations(latitude, longitude);

        mMap.setMyLocationEnabled(true);

    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingsListActivity.class);
        startActivity(intent);

    }
}