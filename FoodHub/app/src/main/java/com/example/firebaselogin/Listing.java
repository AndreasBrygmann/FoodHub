package com.example.firebaselogin;

import java.util.ArrayList;

public class Listing  {

    public String documentID;

    public String title;
    public String city;
    public String expdate;
    public String description;
    public String userid;
    public ArrayList<String> selectedCategories;

    public Listing() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Listing(String title, String city, String expdate, String description, String userid, ArrayList<String> selectedCategories) {
        this.title = title;
        this.city = city;
        this.expdate = expdate;
        this.description = description;
        this.userid = userid;
        this.selectedCategories = selectedCategories;
    }

    public Listing(String documentID) {
        this.documentID = documentID;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }

}