package com.example.hiddenseek.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.util.Log;

import com.example.hiddenseek.DialogActivity;
import com.example.hiddenseek.data.Msg;

import org.json.JSONException;
import org.json.JSONObject;


import com.example.hiddenseek.navigation_button.friend.Friend;
import com.example.hiddenseek.navigation_button.friend.MessageBox;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.hiddenseek.data.FileController.createFileDir;
import static com.example.hiddenseek.data.FileController.readFile;
import static com.example.hiddenseek.data.FileController.readFileWithFile;
import static com.example.hiddenseek.data.FileController.writeFile;

public class MsgContainer {

    private static File CacheRoot;

    /**
     * 存储Json文件到records
     *
     * @param c
     * @param json   json字符串
     * @param append true 增加到文件末，false则覆盖掉原来的文件
     */
    public static void writeToRecords(Context c, String json, String currentUserID, String fromUserID, boolean append) {
        writeFile(c,currentUserID,fromUserID, "records",json,true);
    }

    /**
     * write values in Map to temp in local
     *
     * @param c
     * @param jsonMap
     * @param append
     */

    public static void writeMsgsToTemp(Context c,
                                       HashMap<String, Msg> jsonMap,
                                       String currentUserID, String fromUserID,
                                       boolean append) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").child(fromUserID).child("username").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.e("write to temp", "profile");
                String fromDBUser = task.getResult().getValue().toString();
                setProfile(c, currentUserID, fromUserID, fromDBUser);
            }
        });
        writeLastMsg(c, currentUserID, fromUserID, (Msg) jsonMap.values().toArray()[jsonMap.size() - 1]);
        String targetFile = "temp";
        if (c.getClass().equals(DialogActivity.class)) {
            DialogActivity dialogActivity = (DialogActivity) c;
            boolean sameRecevier = dialogActivity.getReceiverUID().equals(fromUserID);
            if (sameRecevier) {
                //this is in a dialogactivity, we need to write it directly in record.
                targetFile = "records";
                for (Msg v : jsonMap.values()) {
                    //write it to display
                    ((DialogActivity) c).setMsg(v);
                }
                ((DialogActivity) c).notifyData();
            }
        } else {

        }
        for (Msg v : jsonMap.values()) {
            writeFile(c,currentUserID,fromUserID, targetFile,v.toString(),true);
        }
    }



    @SuppressWarnings("resource")

    public static List<Msg> readJson(Context c, String userName, String withSomeone) {
        CacheRoot = c.getFilesDir();
        List<Msg> result = new ArrayList<Msg>();
        try {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            String communFilePath = CacheRoot.getCanonicalPath()
                    + File.separator + userName
                    + File.separator + withSomeone
                    + File.separator;
            String recordsFilePath = communFilePath +  "records";
            String tempFilePath = communFilePath + "temp";
            File des = new File(recordsFilePath);
            if (des.exists()) {
                fis = new FileInputStream(des);
                while (fis.available() > 0) {
                    ois = new ObjectInputStream(fis);
                    JSONObject jsonObject = new JSONObject(((String) ois.readObject()));
                    Log.e("map",jsonObject.toString());
                    Msg msg = new Msg(jsonObject.getString("content"), Integer.parseInt(jsonObject.getString("type")), jsonObject.getString("date"));
                    result.add(msg);
                }
                ois.close();
                fis.close();
            }
            File tempdes = new File(tempFilePath);
            if (tempdes.exists()) {
                fis = new FileInputStream(tempdes);
                fos = new FileOutputStream(des, true);
                while (fis.available() > 0) {
                    ois = new ObjectInputStream(fis);
                    os = new ObjectOutputStream(fos);
                    JSONObject jsonObject = new JSONObject(((String) ois.readObject()));
                    Msg temp_msg = new Msg(jsonObject.getString("content"), Integer.parseInt(jsonObject.getString("type")), jsonObject.getString("date"));
                    result.add(temp_msg);
                    os.writeObject(temp_msg.toString());
                }
                ois.close();
                fis.close();
                tempdes.delete();
                os.close();
                fos.close();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<MessageBox> getCommunicatedUsers(Context c, String userId) {
        CacheRoot = c.getFilesDir();
        ArrayList<MessageBox> msgBoxList = new ArrayList<>();
        try {
            String msgPath = CacheRoot.getCanonicalPath()
                    + File.separator + userId;
            File msgBox = new File(msgPath);
            File[] files = msgBox.listFiles();
            if (files == null) {
                return msgBoxList;
            }
            for (File file : files) {
                String uid = file.getName();
                File des = new File(file,"profile");
                File desLastMsg = new File(file,"lastMsg");
                if (des.exists() && desLastMsg.exists()) {
                    JSONObject jsonObject = new JSONObject(((String) readFileWithFile(des)));
                    String name = jsonObject.getString("name");
                    Friend friend = new Friend(name, uid);
                    MessageBox msgb = new MessageBox(friend);
                    JSONObject jsonObjectMsg = new JSONObject(((String) readFileWithFile(desLastMsg)));
                    Msg lastMsg = new Msg(jsonObjectMsg.getString("content"), Integer.parseInt(jsonObjectMsg.getString("type")), jsonObjectMsg.getString("date"));
                    msgb.setLastMessage(lastMsg.getContent());
                    msgb.setLastTime(lastMsg.getDate());
                    msgBoxList.add(msgb);
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgBoxList;
    }

    public static void setProfile(Context c, String currentUserID, String fromUserID, String fromUserName) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", fromUserName);
            jsonObject.put("uid", fromUserID);
            writeFile(c, currentUserID,fromUserID, "profile",jsonObject.toString(),false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void writeLastMsg(Context c, String currentUserID, String fromUserID, Msg lastMsg) {
        writeFile(c, currentUserID,fromUserID, "lastMsg",lastMsg.toString(),false);
    }

}
