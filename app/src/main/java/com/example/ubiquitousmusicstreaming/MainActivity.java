package com.example.ubiquitousmusicstreaming;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.Manifest;
import android.view.Window;
import com.example.ubiquitousmusicstreaming.ui.configuration.ConfigurationFragment;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;
import com.example.ubiquitousmusicstreaming.ui.music.MusicFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ubiquitousmusicstreaming.databinding.ActivityMainBinding;
import java.util.Hashtable;
import java.util.List;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Integer REQUEST_CODE = 42;
    private static WifiReceiver wifiReceiver;
    private static WifiManager wifiManager;
    private static ConfigurationFragment configurationFragment;
    private static LocationFragment locationFragment;
    private static MusicFragment musicFragment;
    private static Boolean inUse = false, inUseTemp, inUseDataCollection = false;
    private static DataManagement dm;
    private static String predictedRoom, previousLocation = "";
    private static String[] locations; //= /* new String[]{}; = */ new String[]{"Kontor", "Stue", "Køkken"};
    private String fileName = "";
    private static String lastLocationFragmentTextView = "";
    private static Spotify spotify;
    private static Hashtable<String, String> locationSpeakerID = new Hashtable<>();


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, do something
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user, then prompt again
                } else {
                    // Prompt the user to grant the permission
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                }
                // Permission denied, handle the denial
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ONLY FOR TESTING OF FILE SYSTEM
        // FileSystemTest fileSystemTest = new FileSystemTest(this);
        // fileSystemTest.overAllTestMethod();

        super.onCreate(savedInstanceState);
        loadSettings();

        spotify = new Spotify(this);
        spotify.connect();
        spotify.refreshSpeakers();

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Request window feature
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(binding.getRoot());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
               R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
               .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiReceiver.attach(this); // Adding this mainActivity as an observer.
        dm = new DataManagement();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            spotify.handleAuthorizationResponse(resultCode, intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spotify.disconnect();
    }

    /**
     * Will later be used to determine in which room the music are supposed to play.
     * @param scanResults
     */
    private static void printLocation(List<ScanResult> scanResults) {
        double[] location = dm.getPredictionNN(scanResults);
        String quessedRoom = "";

        double temp = 0;
        int index = 0;
        for (int i = 0; i < location.length; i++) {
            if(location[i] > temp) {
                temp = location[i];
                index = i;
            }
        }
        if (location[index] < 0.70) {
            if (previousLocation.equals("Intet Rum")) {
                predictedRoom = "Intet Rum";
            }
            else {
                quessedRoom = "Intet Rum";
                previousLocation = "Intet Rum";
            }
        }
        else {
            if(locations[index].equals(previousLocation)) {
                predictedRoom = locations[index];
            }
            else {
                quessedRoom = locations[index];
                if(inUseTemp) {
                    lastLocationFragmentTextView = "Gætter på: " + quessedRoom + ", men er stadigvæk: " + predictedRoom;
                    locationFragment.SetTextView(lastLocationFragmentTextView);
                }
                previousLocation = locations[index];
            }
        }
        if(inUseTemp) {
            if (quessedRoom.equals("")) {
                musicFragment.updateSpeaker(predictedRoom);
                lastLocationFragmentTextView = "Lokation: " + predictedRoom;
                locationFragment.SetTextView(lastLocationFragmentTextView);
            }
            wifiManager.startScan();
        }
        else { inUse = false; }
    }

    public void updateLocationSpeakerID(Hashtable<String, String> _locationSpeakerID) {
        locationSpeakerID = _locationSpeakerID;
    }

    public void loadSettings(){
        Settings settings = FileSystem.readObjectFromFile(this);

        if (settings != null) {
            String fileName = settings.getFileName();
            if (fileName != null) {
                this.fileName = fileName;
            }
            locationSpeakerID = settings.getLocationSpeakerID();
            String[] settingLocations = settings.getLocations();
            if (settingLocations != null) {
                locations = settingLocations;
            }
        }
    }

    public static void update() {
        if(!inUse) {
            if(inUseDataCollection) {
                if(configurationFragment != null) {
                    configurationFragment.update();
                }
            }
        }
        else {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            printLocation(scanResults);
        }
    }
    public void setInUse(Boolean bool) {
        if (bool) {
            inUse = inUseTemp = true;
        } else {
            inUseTemp = false;
        }
    }
    public static void startScan() { wifiManager.startScan(); }
    public String getFileName() { return fileName; }
    public String[] getLocation() { return locations; }
    public Boolean getInUse() { return inUse; }
    public Boolean getInUseTemp() { return inUseTemp; }
    public String getLastLocationFragmentTextView() { return lastLocationFragmentTextView; }
    public Spotify getSpotify() { return spotify; }
    public WifiReceiver getWifiReceiver() { return wifiReceiver; }
    public static WifiManager getWifiManager() { return wifiManager; }
    public static String getAccessToken() { return spotify.getAccessToken(); }
    public static Hashtable<String, String> getLocationSpeakerID() { return locationSpeakerID; }
    public void attachMusicFragment(MusicFragment musicFragment) { this.musicFragment = musicFragment; }
    public void attachConfigurationFragment(ConfigurationFragment configurationFragment) { this.configurationFragment = configurationFragment; }
    public void attachLocationFragment(LocationFragment locationFragment) { this.locationFragment = locationFragment; }
    public void setInUseDataCollection(Boolean bool) { inUseDataCollection = bool; }
    public void removeLocationFragment() {locationFragment = null ;}


}