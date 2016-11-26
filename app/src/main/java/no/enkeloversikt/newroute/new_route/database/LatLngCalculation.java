package no.enkeloversikt.newroute.new_route.database;

import android.util.Log;

import java.util.Random;

/**
 * Created by Agne Ødegaard on 04/11/2016.
 */

public class LatLngCalculation {

    public GeoLocation newRandomLocation(double lat, double lng, int radius){
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(lat);

        double foundLongitude = new_x + lng;
        double foundLatitude = y + lat;

        GeoLocation loc =  new GeoLocation();
        loc.setLat(foundLatitude);
        loc.setLng(foundLongitude);

        return loc;
    }

}
