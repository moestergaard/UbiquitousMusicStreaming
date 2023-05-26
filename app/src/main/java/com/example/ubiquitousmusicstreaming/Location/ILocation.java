package com.example.ubiquitousmusicstreaming.Location;

import android.net.wifi.ScanResult;
import java.util.List;

public interface ILocation {
    String determineLocation(List<ScanResult> scanResults);
    Boolean needToChangeLocation(String location, String currentLocation);
}
