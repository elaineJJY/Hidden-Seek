package com.example.hiddenseek.navigation_button.friend;

public class MessageBox {
    private Friend friend;
    private String lastMessage;
    private String lastTime;
    public MessageBox(Friend friend){
        this.friend = friend;
    }

    public Friend getFriend(){
        return friend;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }
}
