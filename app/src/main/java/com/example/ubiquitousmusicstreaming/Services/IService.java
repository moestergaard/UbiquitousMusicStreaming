package com.example.ubiquitousmusicstreaming.Services;

import android.content.Intent;
import com.example.ubiquitousmusicstreaming.Models.Device;
import java.util.Hashtable;
import java.util.List;

public interface IService {
    void connect();
    void disconnect();
    void handleAuthorizationResponse(int resultCode, Intent intent);
    void handleRequest(String request);
    void stopService();
    void changeLocation(String location);
    void updateActiveDevice();
    void updateAvailableDevices();
    List<Device> getAvailableDevices();
    Hashtable<String, String> getDeviceNameId();
    void changeActivationOfDevice();
}
