package no.enkeloversikt.newroute.new_route.database;

import android.util.Log;

import java.util.Random;

/**
 * Created by Agne Ødegaard on 04/11/2016.
 */

public class LatLngCalculation {

    public double getRandom(){
        Random r = new Random();
        return r.nextDouble();
    }

    public GeoLocation newRandomLocation(double lat, double lng, int radius){
        double u = getRandom();
        double v = getRandom();
        double r = radius / 111300f;

        double w = r * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);
        double lat1 = x / Math.cos(lat);

        GeoLocation loc =  new GeoLocation();
        loc.setLat(lat1 + lat);
        loc.setLng(y + lng);

        return loc;
    }

}
