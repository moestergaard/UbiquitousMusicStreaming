package com.example.ubiquitousmusicstreaming;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

public class Settings implements Serializable {
    private static Hashtable<String, String> locationSpeakerID;
    private static String[] locations;
    private static String fileName;

    public Hashtable<String, String> getLocationSpeakerID() {
        return locationSpeakerID;
    }

    public void setLocationSpeakerID(Hashtable<String, String> locationSpeakerID) {
        this.locationSpeakerID = locationSpeakerID;
    }

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

    public static String convertToString() {
        return "Settings{" +
                "locationSpeakerID=" + locationSpeakerID +
                ", locations=" + Arrays.toString(locations) +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}

