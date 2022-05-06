package com.example.hiddenseek.data;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.hiddenseek.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.GeoPoint;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.example.hiddenseek.data.FileController.readFile;
import static com.example.hiddenseek.data.FileController.writeFile;

public class DailyMission {
    public String CinemaMission =
            "White bear leaves a Treasure Chest in the CINEMA. " +
                    "Go to a CINEMA in your area to open it. " +
                    "You have chance to get coin!";
    public String ParkMission =
            "Unicorn is waiting for you at PARK. " +
                    "Go to a PARK in your area to meet him. " +
                    "You have chance to get fertilizer!";
    public String SupermarketMission =
            "Little girl is in the SUPERMARKET. Go to a SUPERMARKET to meet her. " +
                    "You have chance to buy rare seeds!";

    public HashMap<String, String> MissionDescription;
    public ArrayList<String> Missions;

    public DailyMission() {
        MissionDescription = new HashMap<>();
        MissionDescription.put("", "You have finished the daily Mission!");
        MissionDescription.put("park", ParkMission);
        MissionDescription.put("cinema", CinemaMission);
        MissionDescription.put("supermarket", SupermarketMission);
        Missions=new ArrayList<>();
        Missions.add("");
        Missions.add("park");
        Missions.add("cinema");
        Missions.add("supermarket");
    }

    public void installDailyMission(Context c, String currentUserID,boolean cancelMission) {
        Random rand = new Random();
        // nextInt as provided by Random is exclusive of the top value so you need to add 1
        Log.e("install Daily Task","now");
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (cancelMission)
            writeFile(c, currentUserID, "Task", "DailyTask", "", false);
        else
            writeFile(c, currentUserID, "Task", "DailyTask", Missions.get(randomNum), false);
    }

    public String readDailyMission(Context c, String currentUserID){
        String target= (String)readFile(c,currentUserID,"Task","DailyTask");
        return MissionDescription.get(target);
    }

    public String readPosition(Context c, String currentUserID){
        return (String)readFile(c,currentUserID,"Task","DailyTask");
    }

    public boolean ifPOINear(Context c,String currentUserID,GeoPoint currentPoint){
        String target= (String)readFile(c,currentUserID,"Task","DailyTask");
        Log.e("POI" ,target);
        NominatimPOIProvider poiProvider = new NominatimPOIProvider("OsmNavigator/1.0");
        ArrayList<POI> pois = poiProvider.getPOICloseTo(currentPoint, target,5 , 0.001);
        return !pois.isEmpty();
    }

}
