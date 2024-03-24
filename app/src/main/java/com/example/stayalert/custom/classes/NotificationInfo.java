package com.example.stayalert.custom.classes;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class NotificationInfo {
    private Bitmap readUndreadNotif;
    private String notifTitle, notifDate, message, timestamp;
    private ArrayList<Integer> drowsyList = new ArrayList<>();
    private ArrayList<Integer> yawnList = new ArrayList<>();
    private ArrayList<Integer> timeValues = new ArrayList<>();
    private boolean wasRead=false;

    // Constructor
    public NotificationInfo(String notifTitle, String notifDate, String message, String timestamp, ArrayList drowsyList, ArrayList yawnList, ArrayList timeValues, boolean wasRead,Bitmap readUndreadNotif) {
        this.readUndreadNotif = readUndreadNotif;
        this.notifTitle = notifTitle;
        this.notifDate =notifDate;
        this.message= message;
        this.timestamp = timestamp;
        this.drowsyList = drowsyList;
        this.yawnList = yawnList;
        this.timeValues= timeValues;
        this.wasRead= wasRead;
    }

    // Getter methods
    public Bitmap getReadUndreadNotif() {
        return readUndreadNotif;
    }

    public String getNotifTitle() {
        return notifTitle;
    }

    public String getMessage(){
        return message;
    }

    public String getTimestamp(){
        return timestamp;
    }
    public ArrayList<Integer> getDrowsyList(){
        return drowsyList;
    }
    public ArrayList<Integer> getYawnList(){
        return yawnList;
    }
    public ArrayList<Integer> getTimeValues(){
        return timeValues;
    }
    public boolean getWasRead(){
        return wasRead;
    }

    public String getNotifDate() {
        return notifDate;
    }

    // Setter methods (optional)

    public void setNotifTitle(String notifTitle) {
        this.notifTitle = notifTitle;
    }
    public  Bitmap getReadUnreadBitmap(){
        return readUndreadNotif;
    }
}