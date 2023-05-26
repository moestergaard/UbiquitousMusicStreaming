package com.example.ubiquitousmusicstreaming;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import java.util.List;

public class WifiReceiver extends BroadcastReceiver {
    private List<ScanResult> resultsOverall;
    private MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mainActivity, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
            int REQUEST_CODE = 42;
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            resultsOverall = wifiManager.getScanResults();
            notifyMainActivity();
        }
    }

    private void notifyMainActivity() { mainActivity.update(); }
    public void attach(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public List<ScanResult> getScanResult() {
        return resultsOverall;
    }
    public void clearScanResult() {
        resultsOverall.clear();
    }
}
