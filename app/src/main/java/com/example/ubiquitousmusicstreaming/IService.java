package com.example.ubiquitousmusicstreaming;

import android.content.Intent;

import com.example.ubiquitousmusicstreaming.ui.configuration.Device;

import java.util.List;

public interface IService {
    void connect();
    void disconnect();
    void handleAuthorizationResponse(int resultCode, Intent intent);
    void changeDevice(String deviceID);
    void updateActiveDevice();
    void handleRequest(String request);
    List<Device> getAvailableDevices();
}
