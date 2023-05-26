package com.example.ubiquitousmusicstreaming.Configuration;

import android.net.wifi.ScanResult;

import java.util.List;

public interface IConfiguration {
    void update(List<ScanResult> scanResults, Boolean currentlyScanning, String roomCurrentlyScanning);
}
