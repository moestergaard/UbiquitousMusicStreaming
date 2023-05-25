package com.example.ubiquitousmusicstreaming;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.Manifest;
import android.view.Window;
import android.widget.Toast;
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagementNN;
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagementSVM;
import com.example.ubiquitousmusicstreaming.DataManagement.IDataManagement;
import com.example.ubiquitousmusicstreaming.FileSystem.FileSystem;
import com.example.ubiquitousmusicstreaming.FileSystem.IFileSystem;
import com.example.ubiquitousmusicstreaming.Services.IService;
import com.example.ubiquitousmusicstreaming.Services.SpotifyService;
import com.example.ubiquitousmusicstreaming.ui.configuration.ConfigurationFragment;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;
import com.example.ubiquitousmusicstreaming.ui.music.MusicFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ubiquitousmusicstreaming.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Integer REQUEST_CODE = 42;
    private static WifiReceiver wifiReceiver;
    private IFileSystem fileSystem;
    private WifiManager wifiManager;
    private ConfigurationFragment configurationFragment;
    private LocationFragment locationFragment;
    private Boolean inUse = false, inUseTemp, inUseDataCollection = false;
    private IDataManagement dmNN;
    private IDataManagement dmSVM;
    private String predictedRoom, previousLocation = "";
    private String[] locations;
    private final String[] locationsHardcodedForModelPurpose = new String[]{"Kontor", "Stue", "Køkken"};
    private String fileName = "";
    private String lastLocationFragmentTextView = "";
    private IService service;
    private Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private String trackName;
    private String artistName;
    private Bitmap coverImage;
    private Boolean playing;
    private String roomCurrentlyScanning;
    private Boolean previousWasOutside = false;
    private String playingSpeaker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        fileSystem = new FileSystem(this);
        initializeSettings();
        service = new SpotifyService(this);
        service.connect();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
               R.id.navigation_music, R.id.navigation_location, R.id.navigation_configuration)
               .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiReceiver.attach(this);
        dmNN = new DataManagementNN();
        dmSVM = new DataManagementSVM(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            service.handleAuthorizationResponse(resultCode, intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void determineLocation(List<ScanResult> scanResults) {
        double[] location = dmSVM.getPrediction(scanResults);
        int locationIndex = (int) location[0];
        predictedRoom = locationsHardcodedForModelPurpose[locationIndex];
        boolean isOutsideArea = checkIfOutsideArea(scanResults);

        if(inUseTemp) {
            if (isOutsideArea && previousWasOutside) {
                service.handleRequest("pause");
                playing = false;
                lastLocationFragmentTextView = "";
                locationFragment.SetTextView(lastLocationFragmentTextView);
            } else {
                if (predictedRoom.equals(previousLocation)) {
                    Boolean result = locationFragment.updateSpeaker(predictedRoom);
                    playing = result;
                    if (result) {
                        lastLocationFragmentTextView = predictedRoom;
                        locationFragment.SetTextView(lastLocationFragmentTextView);
                    }
                } else previousLocation = predictedRoom;
                previousWasOutside = isOutsideArea;
                startScan();
            }
        }
        else { inUse = false; }
    }

    private Boolean checkIfOutsideArea(List<ScanResult> scanResults) {
        double[] location = dmNN.getPrediction(scanResults);

        boolean outside = true;

        double epsilon = Math.ulp(1.0);
        double treshold = 1.0/location.length + epsilon;

        for (int i = 0; i < location.length; i++) {
            if(location[i] > treshold) {
                outside = false;
            }
        }
        return outside;
    }

    public void updateLocationSpeakerName(Hashtable<String, String> _locationSpeakerName) {
        locationSpeakerName = _locationSpeakerName;
    }

    public void initializeSettings(){
        Settings settings = fileSystem.loadSettings();

        if (settings == null) {
            fileSystem.createSettingFile();
            Settings newSettings = new Settings();
            fileSystem.storeSettings(newSettings);
        } else {
            String fileName = settings.getFileName();
            if (fileName != null) {
                this.fileName = fileName;
            }
            locationSpeakerName = settings.getLocationSpeakerName();
            String[] settingLocations = settings.getLocations();
            if (settingLocations != null) {
                locations = settingLocations;
            }
        }
    }

    public void update() {
        if(inUse) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            determineLocation(scanResults);
        } else {
            if(inUseDataCollection) {
                if(configurationFragment != null) {
                    configurationFragment.update();
                }
            }
        }
    }

    public void setInUse(Boolean bool) {
        if (bool) {
            inUse = inUseTemp = true;
        } else {
            inUseTemp = false;
        }
    }

    public void startScan() { wifiManager.startScan(); }
    public String getFileName() { return fileName; }
    public String[] getLocation() { return locations; }
    public Boolean getInUse() { return inUse; }
    public String getLastLocationFragmentTextView() { return lastLocationFragmentTextView; }
    public IService getService() { return service; }
    public WifiReceiver getWifiReceiver() { return wifiReceiver; }
    public Hashtable<String, String> getLocationSpeakerName() { return locationSpeakerName; }
    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public Bitmap getCoverImage() { return coverImage; }
    public Boolean getPlaying() { return playing; }
    public String getRoomCurrentlyScanning() { return roomCurrentlyScanning; }
    public Boolean getInUseDataCollection() { return inUseDataCollection; }
    public String getPlayingSpeaker() { return playingSpeaker; }
    public IFileSystem getFileSystem() { return fileSystem; }
    public void attachConfigurationFragment(ConfigurationFragment configurationFragment) { this.configurationFragment = configurationFragment; }
    public void attachLocationFragment(LocationFragment locationFragment) { this.locationFragment = locationFragment; }
    public void setLastLocationFragmentTextView(String lastLocation) { lastLocationFragmentTextView = lastLocation; }
    public void setInUseDataCollection(Boolean bool) { inUseDataCollection = bool; }
    public void setTrackName(String trackName) { this.trackName = trackName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public void setCoverImage(Bitmap coverImage) { this.coverImage = coverImage; }
    public void setPlayingSpeaker(String speakerName) { playingSpeaker = speakerName; }
    public void setPlaying(Boolean playing) {this.playing = playing; }
    public void setRoomCurrentlyScanning(String roomCurrentlyScanning) { this.roomCurrentlyScanning = roomCurrentlyScanning; }
    public void removeLocationFragment() {locationFragment = null ;}
    public void setLocations(String[] locations) { this.locations = locations; }
}