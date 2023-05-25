package com.example.ubiquitousmusicstreaming.Models;

public class Device {
    private String id;
    private boolean isActive;
    private boolean isPrivateSession;
    private boolean isRestricted;
    private String name;
    private String type;
    private int volumePercent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPrivateSession() {
        return isPrivateSession;
    }

    public void setPrivateSession(boolean privateSession) {
        isPrivateSession = privateSession;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public void setRestricted(boolean restricted) {
        isRestricted = restricted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(int volumePercent) {
        this.volumePercent = volumePercent;
    }
}
