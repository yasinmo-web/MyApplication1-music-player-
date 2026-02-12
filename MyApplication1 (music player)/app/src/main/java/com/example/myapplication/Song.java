package com.example.myapplication;

public class Track {
    private String name;
    private int resourceId;

    public Track(String name, int resourceId) {
        this.name = name;
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public int getResourceId() {
        return resourceId;
    }
}