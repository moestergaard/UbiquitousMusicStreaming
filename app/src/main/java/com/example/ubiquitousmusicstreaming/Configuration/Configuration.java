package com.example.ubiquitousmusicstreaming.Configuration;

import android.net.wifi.ScanResult;
import com.example.ubiquitousmusicstreaming.MainActivity;
import java.util.List;

public class Configuration implements IConfiguration{

    private MainActivity mainActivity;
    private String lastScanResults = "";

    public Configuration(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void update(List<ScanResult> scanResults, Boolean currentlyScanning, String roomCurrentlyScanning) {

        if(lastScanResults.equals("")) {
            String result = makeScanResultString(scanResults, roomCurrentlyScanning);
            lastScanResults = result;
            mainActivity.writeToFile(result);
        }
        else {
            String resultElse = makeScanResultString(scanResults, roomCurrentlyScanning);
            Boolean newAndLastScanAreEqual = !lastScanResults.equals(resultElse);
            if(newAndLastScanAreEqual) {
                mainActivity.writeToFile(resultElse);
            }
            lastScanResults = resultElse;
        }
        if(currentlyScanning) {
            mainActivity.clearScanResult();
            mainActivity.startScan();
        }
    }

    private String makeScanResultString(List<ScanResult> scanResults, String roomCurrentlyScanning) {

        String scanResultString = "Scanning: " + roomCurrentlyScanning + "\n\n";

        for (ScanResult result : scanResults) {
            scanResultString += "SSID: " + result.SSID + "\n";
            scanResultString += "BSSID: " + result.BSSID + "\n";
            scanResultString += "ResultLevel: " + result.level + "\n";
            scanResultString += "Frequency: " + result.frequency + "\n";
            scanResultString += "Timestamp: " + result.timestamp + "\n\n\n";
        }
        scanResultString += "************** \n\n";
        return scanResultString;
    }
}
