package com.example.hiddenseek.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import static com.example.hiddenseek.data.FileController.createFileDir;
import static com.example.hiddenseek.data.FileController.readFile;
import static com.example.hiddenseek.data.FileController.writeFile;

public class TimeController {
    public static void writeCurrentTime(Context c, String currentUserID) {
        long currentTime = new Date().getTime();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lastLog", currentTime);
            writeFile(c, currentUserID,"sunShineAndBox", "lastLogTime",jsonObject.toString(),false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static long readLastLogTime(Context c,String currentUserID){
        long result=-1;
        Log.e("map",currentUserID);
        try {
            Object lastLogTime=readFile(c, currentUserID,"sunShineAndBox", "lastLogTime");
            if (lastLogTime!=null)
            {
                JSONObject jsonObject = new JSONObject((String)(lastLogTime ));
                long lastLog = Long.parseLong(jsonObject.getString("lastLog"));
                result=lastLog;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
