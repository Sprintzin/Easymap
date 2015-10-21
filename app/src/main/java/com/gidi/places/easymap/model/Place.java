package com.gidi.places.easymap.model;

/**
 * POJO
 */


public class Place {

    public double lat;
    public double lng;
    public long id;
    public String icon;
    public String name;
    public String vicinity;
    public String formatted_address;

    public Place(double lat, double lng, long id, String icon, String name, String vicinity, String formatted_address) {
        this.lat = lat;
        this.lng = lng;
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.vicinity = vicinity;
        this.formatted_address = formatted_address;
    }
    public Place (double lat, double lng, String name){
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }
}

