package no.enkeloversikt.newroute.new_route.database;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Agne Ødegaard on 03/11/2016.
 */

public class GeoLocation {

    private double lat;
    private double lng;
    private float distance;
    private int id;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getLatLng(){
        return new LatLng(this.lat, this.lng);
    }
}
