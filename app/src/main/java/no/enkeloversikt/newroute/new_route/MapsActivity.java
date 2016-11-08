package no.enkeloversikt.newroute.new_route;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import no.enkeloversikt.newroute.new_route.database.DatabaseHelper;
import no.enkeloversikt.newroute.new_route.database.GeoLocation;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseHelper db;

    public double longitude, latitude;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ProgressDialog apiProgress;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private double lastLat = 0;
    private double lastLng = 0;

    private Vibrator vib;

    private int total_points = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new DatabaseHelper(this);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        apiProgress = new ProgressDialog(this);
        apiProgress.setMessage(getString(R.string.loading_location_dialog));
        apiProgress.show();

        requestFineLocationPermit();

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

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                apiProgress.hide();
                apiProgress.dismiss();

                Log.v("newLatLng", "Location changed");
                latitude = location.getLatitude();
                longitude = location.getLongitude();


                LatLng current = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(current));
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

            Log.v("newLatLng", Float.toString(results[0]));
            double addedDist = results[0] + Double.parseDouble(dist);
            Log.v("newLatLng", Double.toString(addedDist));
            db.updateOrInsert("totalDistance", Double.toString(addedDist));

            Log.v("newLatLng", db.fetchType("totalDistance"));
        }

        lastLat = lat;
        lastLng = lng;

    }


    public void setLocations(double lat, double lng){
        GeoLocation[] allGeoLocs = db.fetchAll();

        if(allGeoLocs.length < total_points){
            db.createNewPoints(lat, lng, total_points - allGeoLocs.length, 1000);
            allGeoLocs = db.fetchAll();
        }

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
                Toast.makeText(getBaseContext(), "Congratulations", Toast.LENGTH_SHORT).show();
                db.createNewPoints(lat, lng, 1, 1000);
                db.setPointAsVisited(loc);
                vib.vibrate(500);
                spot.remove();
            }
        }

        TextView score = (TextView) findViewById(R.id.scoreView);
        String scoreText = Integer.toString(db.getScore());
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
    }

}