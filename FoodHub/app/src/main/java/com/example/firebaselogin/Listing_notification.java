package com.example.firebaselogin;

public class Listing_notification {

    String name,phoneNumber,title,userid;

    public Listing_notification(){};

    public Listing_notification(String name,String title,String phoneNumber,String userid) {
        this.name = name;
        this.title = title;
        this.phoneNumber = phoneNumber;
        this.userid = userid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
