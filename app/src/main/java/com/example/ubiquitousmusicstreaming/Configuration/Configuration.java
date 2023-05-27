package com.example.ubiquitousmusicstreaming.Configuration;

import android.net.wifi.ScanResult;
import com.example.ubiquitousmusicstreaming.MainActivity;
import java.util.List;

public class Configuration implements IConfiguration{

    private final MainActivity mainActivity;
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
            boolean newAndLastScanAreEqual = !lastScanResults.equals(resultElse);
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

        StringBuilder scanResultString = new StringBuilder("Scanning: " + roomCurrentlyScanning + "\n\n");

        for (ScanResult result : scanResults) {
            scanResultString.append("SSID: ").append(result.SSID).append("\n");
            scanResultString.append("BSSID: ").append(result.BSSID).append("\n");
            scanResultString.append("ResultLevel: ").append(result.level).append("\n");
            scanResultString.append("Frequency: ").append(result.frequency).append("\n");
            scanResultString.append("Timestamp: ").append(result.timestamp).append("\n\n\n");
        }
        scanResultString.append("************** \n\n");
        return scanResultString.toString();
    }
}
