package com.example.ubiquitousmusicstreaming.Services;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.example.ubiquitousmusicstreaming.Models.Device;
import java.util.Hashtable;
import java.util.List;

public interface IService {
    void connect();
    void disconnect();
    void handleAuthorizationResponse(int resultCode, Intent intent);
    void changeDevice(String deviceID);
    void updateActiveDevice();
    void handleRequest(String request);
    List<Device> getAvailableDevices();
    Hashtable<String, String> getDeviceNameId();
    void changeActivationOfDevice();
    void attachFragment(Fragment fragment);
}
