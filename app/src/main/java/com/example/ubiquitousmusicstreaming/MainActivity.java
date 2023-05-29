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
import com.example.ubiquitousmusicstreaming.Configuration.Configuration;
import com.example.ubiquitousmusicstreaming.Configuration.IConfiguration;
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagementNN;
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagementSVM;
import com.example.ubiquitousmusicstreaming.DataManagement.IDataManagement;
import com.example.ubiquitousmusicstreaming.FileSystem.FileSystem;
import com.example.ubiquitousmusicstreaming.FileSystem.IFileSystem;
import com.example.ubiquitousmusicstreaming.Location.ILocation;
import com.example.ubiquitousmusicstreaming.Location.Location;
import com.example.ubiquitousmusicstreaming.Models.Device;
import com.example.ubiquitousmusicstreaming.Services.IService;
import com.example.ubiquitousmusicstreaming.Services.SpotifyService;
import com.example.ubiquitousmusicstreaming.ui.locationUI.LocationFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ubiquitousmusicstreaming.databinding.ActivityMainBinding;
import com.example.ubiquitousmusicstreaming.ui.musicUI.MusicFragment;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final Integer REQUEST_CODE = 42;
    private static WifiReceiver wifiReceiver;
    private IFileSystem fileSystem;
    private WifiManager wifiManager;
    private LocationFragment locationFragment;
    private MusicFragment musicFragment;
    private Boolean inUseTracking = false, inUseDataCollection = false;
    private String[] locations;
    private String fileName = "";
    private IService service;
    private Hashtable<String, String> locationDeviceName = new Hashtable<>();
    private String trackName;
    private String artistName;
    private Bitmap coverImage;
    private Boolean playing;
    private String roomCurrentlyScanning;
    private String playingSpeaker = "";
    private String currentLocation = "", previousLocation = "";
    private ILocation locationClass;
    private IConfiguration configurationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setupVariables();
        setupView();
    }

    private void setupView() {
        com.example.ubiquitousmusicstreaming.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_music, R.id.navigation_location, R.id.navigation_configuration)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void setupVariables() {
        fileSystem = new FileSystem(this);
        service = new SpotifyService(this);
        wifiReceiver = new WifiReceiver();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiReceiver.attach(this);
        initializeSettings();
        service.connect();

        IDataManagement dmNN = new DataManagementNN();
        IDataManagement dmSVM = new DataManagementSVM(this);

        locationClass = new Location(dmNN, dmSVM);
        configurationClass = new Configuration(this);
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
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String determineDevice(String location) {
        String speakerName = locationDeviceName.get(location);
        if(speakerName == null) {
            Toast.makeText(this, "Vælg hvilken højtaler, der hører til " + location, Toast.LENGTH_LONG).show();
            return null;
        }
        Hashtable<String, String> speakerNameId = service.getDeviceNameId();
        String speakerId = speakerNameId.get(speakerName);

        if (speakerId == null) {
            Toast.makeText(this, "Højtaleren " + speakerName + " er ikke tilgængelig.", Toast.LENGTH_LONG).show();
            return null;
        }
        return speakerName;
    }

    private void updatePlayingLocation(String location, String deviceName) {
        playing = true;
        previousLocation = currentLocation;
        currentLocation = location;
        locationFragment.setTextView(currentLocation);
        playingSpeaker = deviceName;
        service.changeLocation(currentLocation);

    }

    private Settings addLocationToSettings(String room, Settings settings) {
        if (settings != null) {
            String[] locations = settings.getLocations();
            String[] newLocations;
            if (locations != null) {
                newLocations = Arrays.copyOf(locations, locations.length + 1);
            } else {
                newLocations = new String[1];
            }

            newLocations[newLocations.length - 1] = room;
            settings.setLocations(newLocations);
        }
        return settings;
    }

    public void initializeSettings() {
        Settings settings = fileSystem.loadSettings();

        if (settings == null) {
            makeNewSettings();
        } else {
            String fileName = settings.getFileName();
            if (fileName != null) {
                this.fileName = fileName;
            }
            locationDeviceName = settings.getLocationSpeakerName();
            String[] settingLocations = settings.getLocations();
            if (settingLocations != null) {
                locations = settingLocations;
            }
        }
    }

    public void update() {
        if(inUseDataCollection) {
            List<ScanResult> scanResult = wifiReceiver.getScanResult();
            configurationClass.update(scanResult, inUseDataCollection, roomCurrentlyScanning);
            return;
        } if (inUseTracking) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                return;
            }

            List<ScanResult> scanResults = wifiManager.getScanResults();
            String location = locationClass.determineLocation(scanResults);

            if (location != null) {
                if (location.equals("outside")) {
                    service.stopService();
                    currentLocation = "";
                    locationFragment.setTextView(currentLocation);
                    playing = false;
                    startScan();
                    return;
                }

                boolean needForChangingLocation = locationClass.needToChangeLocation(location, currentLocation);

                if (needForChangingLocation) {
                    String deviceName = determineDevice(location);

                    if (deviceName != null) {
                        updatePlayingLocation(location, deviceName);
                        if (!previousLocation.equals("")) { locationFragment.updateButtonChangeDevice(true); }
                    }
                }
            }
            startScan();
        }
    }

    public void startScan() { wifiManager.startScan(); }
    public void makeNewSettings() {
        fileSystem.createSettingFile();
        Settings newSettings = new Settings();
        fileSystem.storeSettings(newSettings);
    }
    public Hashtable<String, String> getLocationDeviceName() { return locationDeviceName; }

    /**
     * Configuration fragment
     */
    public void updateLocationDeviceName(Hashtable<String, String> _locationDeviceName) {
        locationDeviceName = _locationDeviceName;
    }
    public void storeSettings(Settings settings) { fileSystem.storeSettings(settings); }
    public void addLocationToSettings(String location) {
        Settings settingsOld = getSettings();
        Settings settingsUpdated = addLocationToSettings(location, settingsOld);
        storeSettings(settingsUpdated);
    }
    public void addLocationDeviceNameToSettings(Hashtable<String, String> locationDeviceName) {
            Settings settings = getSettings();
            if (settings != null) {
                settings.setLocationSpeakerName(locationDeviceName);
            }
            storeSettings(settings);
    }
    public void makeFile(String fileName) { fileSystem.makeFile(fileName); }
    public void setFileNameInSettings(String fileName) {
        Settings settings = fileSystem.loadSettings();
        if (settings != null) {
            settings.setFileName(fileName);
            fileSystem.storeSettings(settings);
        }
    }
    public void setInUseDataCollection(Boolean bool) { inUseDataCollection = bool; }
    public void setRoomCurrentlyScanning(String roomCurrentlyScanning) { this.roomCurrentlyScanning = roomCurrentlyScanning; }
    public void setLocations(String[] locations) { this.locations = locations; }
    public String getFileName() { return fileName; }
    public String[] getLocations() { return locations; }
    public String getRoomCurrentlyScanning() { return roomCurrentlyScanning; }
    public Boolean getInUseDataCollection() { return inUseDataCollection; }
    public List<Device> getAvailableDevices() { return service.getAvailableDevices(); }
    public Settings getSettings() { return fileSystem.loadSettings(); }

    /**
     * Location fragment
     */
    public void attachLocationFragment(LocationFragment locationFragment) { this.locationFragment = locationFragment; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    public void setInUse(Boolean bool) { inUseTracking = bool; }
    public void changeToPreviousLocation() {
        if (previousLocation.equals("")) { return; }

        String deviceName = determineDevice(previousLocation);

        if (deviceName != null) {
            playing = true;
            currentLocation = previousLocation;
            previousLocation = "";
            locationClass.setPreviousLocation(previousLocation);
            locationFragment.setTextView(currentLocation);
            locationFragment.updateButtonChangeDevice(true);
            playingSpeaker = deviceName;
            service.changeLocation(previousLocation);
        }
    }
    public String getCurrentLocation() { return currentLocation; }
    public String getPreviousLocation() { return previousLocation; }
    public Boolean getInUseTracking() { return inUseTracking; }

    /**
     * Music fragment
     */
    public void attachMusicFragment(MusicFragment musicFragment) { this.musicFragment = musicFragment; }
    public void changeActivationOfDevice() { service.changeActivationOfDevice(); }
    public void handleRequestDevice(String request) {
        service.handleRequest(request);
        playing = true;
    }
    public void updateActiveDevice() {service.updateActiveDevice();}
    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public Bitmap getCoverImage() { return coverImage; }
    public Boolean getPlaying() { return playing; }
    public String getPlayingSpeaker() { return playingSpeaker; }

    /**
     * Spotify Service
     */
    public void updatePlayingNow(Boolean playing) {
        this.playing = playing;
        musicFragment.updatePlayingNow(this.playing);
    }

    public void updateSpeaker(String playingSpeaker) {
        this.playingSpeaker = playingSpeaker;
        musicFragment.updatePlayingSpeaker(this.playingSpeaker);
    }

    public void updateTrackInformation(String trackName, String artistName) {
        this.trackName = trackName;
        this.artistName = artistName;
        musicFragment.updateTrackInformation(trackName, artistName);
    }

    public void updateCoverImage(Bitmap image) {
        coverImage = image;
        musicFragment.updateCoverImage(image);
    }

    /**
     * Configuration Class
     */
    public void writeToFile(String data) {
        fileSystem.writeToFile(data, fileName);
    }
    public void clearScanResult() { wifiReceiver.clearScanResult(); }
}