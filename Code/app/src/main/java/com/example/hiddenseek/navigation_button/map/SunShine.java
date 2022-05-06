package com.example.hiddenseek.navigation_button.map;

import android.util.Log;
import android.util.Pair;

import com.firebase.geofire.GeoLocation;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import static com.example.hiddenseek.data.Helper.meterToDegree;

public class SunShine {
    private static int sunShineNum = 10;

    public static List<GeoPoint> initSunShineSet(GeoLocation myCurrentLocation) {
        List<GeoPoint> sunShineList = new ArrayList<>();
        double la = myCurrentLocation.latitude;
        double lo = myCurrentLocation.longitude;
        sunShineList.clear();
        for (int i = 0; i < sunShineNum; i++) {
            Double las = la + (Math.random() * 2 - 1) * meterToDegree(1000);
            Double los = lo + (Math.random() * 2 - 1) * meterToDegree(1000);

            sunShineList.add(new GeoPoint(las, los));
        }
        return sunShineList;
    }

}
