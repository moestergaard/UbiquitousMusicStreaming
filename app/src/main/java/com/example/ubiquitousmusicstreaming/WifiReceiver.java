package com.example.ubiquitousmusicstreaming;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.ui.configuration.ConfigurationFragment;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;

import java.util.List;

public class WifiReceiver extends BroadcastReceiver {
    List<ScanResult> resultsOverall;

    MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        /*
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
         */
        //if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
        resultsOverall = wifiManager.getScanResults();
        notifyMainActivity();
        //}
    }

    public void attach(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }



    private void notifyMainActivity() {
        mainActivity.update();
    }

    public List<ScanResult> getScanResult() {
        return resultsOverall;
    }

    public void clearScanResult() {
        resultsOverall.clear();
    }
}
