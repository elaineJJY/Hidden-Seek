package com.example.hiddenseek.navigation_button.friend;

import androidx.fragment.app.Fragment;

public class Friend {
    private String name;
    private String uid;
    public Friend(String name, String uid){
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }
}
