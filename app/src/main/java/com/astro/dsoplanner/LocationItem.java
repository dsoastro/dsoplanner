package com.astro.dsoplanner;

import com.astro.dsoplanner.base.Exportable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocationItem implements Exportable {
    private static final double precision = 0.00001;//for equal comparison
    private static final String NAME2 = ", name=";
    private static final String LON2 = ", lon=";
    private static final String LOCATION_ITEM_LAT = "LocationItem [lat=";
    public static final String LATTITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String NAME = "name";

    double lat;
    double lon;
    String name;

    public LocationItem(String name, double lat, double lon) {

        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public LocationItem(DataInputStream stream) throws IOException {
        lat = stream.readDouble();
        lon = stream.readDouble();
        name = stream.readUTF();

    }

    public int getClassTypeId() {
        return Exportable.LOCATION_ITEM;
    }

    public Map<String, String> getStringRepresentation() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(LATTITUDE, String.format(Locale.US, "%.6f", lat));
        map.put(LONGITUDE, String.format(Locale.US, "%.6f", lon));
        map.put(NAME, name);
        return map;
    }

    @Override
    public String toString() {
        return LOCATION_ITEM_LAT + lat + LON2 + lon + NAME2 + name + "]";
    }

    public byte[] getByteRepresentation() {

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buff);
        try {
            stream.writeDouble(lat);
            stream.writeDouble(lon);
            stream.writeUTF(name);
        } catch (IOException e) {
            return null;
        }

        return buff.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocationItem) {
            LocationItem item = (LocationItem) o;
            if (Math.abs(lat - item.lat) < precision && Math.abs(lon - item.lon) < precision && name.equals(item.name))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 37 * Double.valueOf(lat).hashCode() + Double.valueOf(lon).hashCode();
    }
}
