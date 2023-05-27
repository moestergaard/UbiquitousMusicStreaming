package com.example.ubiquitousmusicstreaming.Location;

import android.net.wifi.ScanResult;
import com.example.ubiquitousmusicstreaming.DataManagement.IDataManagement;
import java.util.List;

public class Location implements ILocation{
    private final String[] locationsHardcodedForModelPurpose = new String[]{"Kontor", "Stue", "Køkken"};
    private final IDataManagement dataManagementNN;
    private final IDataManagement dataManagementSVM;
    private String previousLocation;
    private Boolean previousWasOutside = false;

    public Location(IDataManagement dataManagementNN, IDataManagement dataManagementSVM) {
        this.dataManagementNN = dataManagementNN;
        this.dataManagementSVM = dataManagementSVM;
    }

    public String determineLocation(List<ScanResult> scanResults) {
        double[] location = dataManagementSVM.getPrediction(scanResults);
        int locationIndex = (int) location[0];
        String predictedRoom = locationsHardcodedForModelPurpose[locationIndex];
        boolean isOutsideArea = checkIfOutsideArea(scanResults);

        if (isOutsideArea && previousWasOutside) { return "outside"; }
        if (predictedRoom.equals(previousLocation)) { return predictedRoom; }

        previousLocation = predictedRoom;
        previousWasOutside = isOutsideArea;
        return null;
    }

    public Boolean needToChangeLocation(String location, String currentLocation) {
        boolean sameLocationAsPrevious = location.equals(previousLocation);
        boolean alreadyChosenLocation = location.equals(currentLocation);

        return sameLocationAsPrevious && !alreadyChosenLocation;
    }

    public void setPreviousLocation(String location) { previousLocation = location; }

    private Boolean checkIfOutsideArea(List<ScanResult> scanResults) {
        double[] location = dataManagementNN.getPrediction(scanResults);

        boolean outside = true;

        double epsilon = Math.ulp(1.0);
        double treshold = 1.0 / location.length + epsilon;

        for (double v : location) {
            if (v > treshold) {
                outside = false;
                break;
            }
        }
        return outside;
    }
}
