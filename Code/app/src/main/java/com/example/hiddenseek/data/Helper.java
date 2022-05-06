package com.example.hiddenseek.data;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import static com.example.hiddenseek.data.FileController.readFile;
import static com.example.hiddenseek.data.FileController.writeFile;
import static com.example.hiddenseek.data.FirebaseListener.getUserInfo;
import static com.example.hiddenseek.data.MsgContainer.writeLastMsg;

public class Helper {
    public static double meterToDegree(int radius) {
        return radius / 111000f;
    }
    public static String getCurrentUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static void addItemInDatabase(String item,int IntegerToBeAdded){
        Map<String, Object> userInfo = getUserInfo();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserID());
        int itemValue = Integer.parseInt(userInfo.get(item).toString());
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put(item, itemValue + IntegerToBeAdded);
        userRef.updateChildren(userUpdates);
    }

    public static Msg sendMsg (Context c,String content,String receiverUID,String currentUserID){
        Msg msg = new Msg(content, Msg.TYPE_RECEIVED,  new SimpleDateFormat("MM.dd.yyyy HH:mm").format(Calendar.getInstance().getTime()));
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(receiverUID)
                .child("msgs")
                .child(currentUserID)
                .push()
                .setValue(msg);
        msg = new Msg(content, Msg.TYPE_SENT,  new SimpleDateFormat("MM.dd.yyyy HH:mm").format(Calendar.getInstance().getTime()));
        // access local cached message-file, write new message into file and display
        MsgContainer.writeToRecords(c, msg.toString(), currentUserID, receiverUID, true);
        writeLastMsg(c, currentUserID, receiverUID, msg);
        return msg;
    }


    public static void rewriteSunOrBoxInFile(Context c,Marker marker, String markerList) {
        try {
            ArrayList<String> ssList = (ArrayList<String>) readFile(c, getCurrentUserID(), "sunShineAndBox",
                    markerList);
            Iterator<String> it = ssList.iterator();
            while (it.hasNext()) {
                String gp = it.next();
                JSONObject jsonObject = new JSONObject(gp);
                if (markerList.equals("boxList")) {
                    Pair<String, String> pair = new Gson().fromJson(gp, Pair.class);
                    jsonObject = new JSONObject((String) pair.first);
                }
                String la = jsonObject.getString("mLatitude");
                String lo = jsonObject.getString("mLongitude");
                if (la.equals(String.valueOf(marker.getPosition().getLatitude()))
                        && lo.equals(String.valueOf(marker.getPosition().getLongitude()))) {
                    it.remove();
                }
            }
            writeFile(c, getCurrentUserID(), "sunShineAndBox", markerList, ssList, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static Object getCertainItemInHashMapRetrieve(Object item, ArrayList<String> path){
        if (path.isEmpty() || item==null)
            return item;
        String currentRelativePath=path.get(0);
        path.remove(0);
        return getCertainItemInHashMapRetrieve(((HashMap<String,Object>)item).get(currentRelativePath),path);
    }

}
