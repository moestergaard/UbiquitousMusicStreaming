package com.example.ubiquitousmusicstreaming.DataManagement;

import android.net.wifi.ScanResult;

import java.util.List;

public interface IDataManagement {
    double[] getPrediction(List<ScanResult> scanResult);
}
