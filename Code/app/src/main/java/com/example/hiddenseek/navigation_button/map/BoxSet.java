package com.example.hiddenseek.navigation_button.map;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;

import androidx.fragment.app.DialogFragment;

import com.example.hiddenseek.R;
import com.example.hiddenseek.navigation_button.map.MarkerClickEvent.MarkerClickFragment;
import com.firebase.geofire.GeoLocation;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import static com.example.hiddenseek.data.Helper.meterToDegree;

public class BoxSet {
    private final static int boxNum = 3 + (int) (Math.random() * 5);
    public static List<Pair<GeoPoint, Integer>> initBoxSet(GeoLocation myCurrentLocation) {
        List<Pair<GeoPoint, Integer>> returnList = new ArrayList<>();
        double la = myCurrentLocation.latitude;
        double lo = myCurrentLocation.longitude;
        returnList.clear();
        for (int i = 0; i < boxNum; i++) {
            Log.e("Tag3", "boxPointinit");
            Double las = la + (Math.random() * 2 - 1) * meterToDegree(1500);
            Double los = lo + (Math.random() * 2 - 1) * meterToDegree(1500);
            int coinsNum = 100 + (int) (Math.random() * 20);
            returnList.add(new Pair<>(new GeoPoint(las, los), coinsNum));
        }
        return returnList;
    }
}
