package com.example.hiddenseek.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;

    private String content;
    private String date;
    private int type;
    public Msg(){
    }
    public Msg(String content, int type, String date){
        this.content = content;
        this.type = type;
        this.date = date;

    }
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", content);
            jsonObject.put("type", type);
            jsonObject.put("date", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }


    public String getContent() {
        return content;
    }

    public int getType(){
        return type;
    }

    public String getDate(){
        return date;
    }

    public void setType(int t){
        this.type = t;
    }

}
