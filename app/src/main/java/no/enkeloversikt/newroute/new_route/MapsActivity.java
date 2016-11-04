package no.enkeloversikt.newroute.new_route;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private int REFRESH_TIME = 1000;

    private boolean customLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new DatabaseHelper(this);

        apiProgress = new ProgressDialog(this);
        apiProgress.setMessage("loading...");
        apiProgress.show();

        requestFineLocationPermit();

    }


    /**
     * Permission request for Android 6.0 and later
     */
    public void requestFineLocationPermit() {
        Log.v("gpstest", "fineLocation permission");
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
        Log.v("gpstest", "Request Loc");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v("gpstest", "Denied location");
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.v("newLatLng", "loc changed");
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng current = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 12));
                mMap.clear();

               //GeoLocation[] nearByGeoLocs = db.fetchNearBy(latitude, longitude);
               //for (GeoLocation loc : nearByGeoLocs) {
               //     //Points within 250m
               //     //add score or whatever
               //     db.setPointAsVisited(loc);
               //}

               // db.createNewPoints(latitude, longitude, 5, 1000);

                GeoLocation[] allGeoLocs = db.fetchAll();
                if(allGeoLocs.length < 5){
                    db.createNewPoints(latitude, longitude, 5 - allGeoLocs.length, 1000);
                    allGeoLocs = db.fetchAll();
                }

                    float results[] = new float[2];

                    for (GeoLocation loc : allGeoLocs) {
                        Log.v("gpstest", "locations: " + loc.getDistance());

                        //mMap.addMarker(new MarkerOptions().position(loc.getLatLng()).title("Distance: " + loc.getDistance()));
                        Circle spot = mMap.addCircle(new CircleOptions()
                               .center(loc.getLatLng())
                               .radius(125)
                               .strokeColor(Color.argb(70, 229, 57, 53))
                               .fillColor(Color.argb(40, 244, 67, 54)));

                        Location.distanceBetween( latitude, longitude,
                                spot.getCenter().latitude, spot.getCenter().longitude, results);

                        if( results[0] < spot.getRadius() ){
                            Log.v("newLatLng", "ID: " + loc.getId() + "--" +results[0] + " < " + spot.getRadius());
                            Toast.makeText(getBaseContext(), "Congratulations", Toast.LENGTH_SHORT).show();
                            db.createNewPoints(latitude, longitude, 1, 1000);
                            db.setPointAsVisited(loc);
                            spot.remove();
                        }
                    }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(getBaseContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
            }
        };

        // 6000mm == 6s
        Log.v("gpstest", "locationManager");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }


    /**
     * Google maps init
     *
     * @param googleMap mMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("gpstest", "maps ready");

        apiProgress.hide();
        apiProgress.dismiss();

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

}