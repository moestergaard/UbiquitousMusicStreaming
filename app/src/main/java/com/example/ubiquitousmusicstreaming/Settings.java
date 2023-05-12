package com.example.ubiquitousmusicstreaming;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

public class Settings implements Serializable {
    private Hashtable<String, String> locationSpeakerName;
    private Hashtable<String, String> speakerNameId;
    private String[] locations;
    private String fileName;

    public Settings() {
        locationSpeakerName = new Hashtable<>();
        speakerNameId = new Hashtable<>();
        locations = new String[]{};
        fileName = "";
    }

    public Hashtable<String, String> getLocationSpeakerName() {
        return locationSpeakerName;
    }

    public Hashtable<String, String> getSpeakerNameId() { return speakerNameId; }

    public void setLocationSpeakerName(Hashtable<String, String> locationSpeakerName) {
        this.locationSpeakerName = locationSpeakerName;
    }

    public void setSpeakerNameId(Hashtable<String, String> speakerNameId) { this.speakerNameId = speakerNameId; }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*
    public static String convertToString() {
        return "Settings{" +
                "locationSpeakerID=" + locationSpeakerID +
                ", locations=" + Arrays.toString(locations) +
                ", fileName='" + fileName + '\'' +
                '}';
    }

     */
}

