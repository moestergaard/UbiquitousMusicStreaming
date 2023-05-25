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
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagement;
import com.example.ubiquitousmusicstreaming.DataManagement.DataManagementSVM;
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
    private static WifiManager wifiManager;
    private static ConfigurationFragment configurationFragment;
    private static LocationFragment locationFragment;
    private static MusicFragment musicFragment;
    private static Boolean inUse = false, inUseTemp, inUseDataCollection = false;
    private static DataManagement dm;
    private static DataManagementSVM dmSVM;
    private static String predictedRoom, previousLocation = "";
    private String[] locations; // = /* new String[]{}; = */ new String[]{"Kontor", "Stue", "Køkken"};
    private final String[] locationsHardcodedForModelPurpose = new String[]{"Kontor", "Stue", "Køkken"};
    private String fileName = "";
    private static String lastLocationFragmentTextView = "";
    private IService service;
    private static Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private String trackName;
    private String artistName;
    private Bitmap coverImage;
    private static Boolean playing;
    private String roomCurrentlyScanning;
    private Boolean previousWasOutside = false;
    private String playingSpeaker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

         */


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
            Toast.makeText(this, "Giv gerne adgang til baggrundsplacering. Derved virker servicen også i baggrunden.", Toast.LENGTH_LONG).show();
        }



        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        fileSystem = new FileSystem(this);
        System.out.println("Inden loadsettings");

        fileSystem.createSettingFile();
        Settings settings = new Settings();
        fileSystem.storeSettings(settings);

        loadSettings();
        System.out.println("På den anden side af loadsettings");
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
        wifiReceiver.attach(this); // Adding this mainActivity as an observer.
        dm = new DataManagement();
        dmSVM = new DataManagementSVM(this);
        // playingSpeaker = spotify.getPlayingSpeaker();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            service.handleAuthorizationResponse(resultCode, intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.disconnect();
    }



    /*
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



     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


     /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission to location granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Check if background location is necessary
                    if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        // Ask for permission to background location
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
                        Toast.makeText(this, "For at appen kan fungere korrekt i baggrunden, er det nødvendigt at give adgang til baggrundsplacering.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Appen fungerer i baggrunden.", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // Permission to location is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Ask for permission again
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                } else {
                    // Brugeren har afvist tilladelse permanent
                    Toast.makeText(this, "Det er nødvendigt at give adgang til placering for at bruge denne service, da det giver adgang til nødvendige Wi-Fi informationer. Appen fungerer kun, når den er åben.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

      */





    /**
     * Will later be used to determine in which room the music are supposed to play.
     * @param scanResults
     */

    private void determineLocation(List<ScanResult> scanResults) {
        int locationIndex = dmSVM.getPredictionSVM(scanResults);
        System.out.println("Room: " + locationsHardcodedForModelPurpose[locationIndex]);
        predictedRoom = locationsHardcodedForModelPurpose[locationIndex];
        boolean isOutsideArea = checkIfOutsideArea(scanResults);

        if(inUseTemp) {
            if (isOutsideArea && previousWasOutside) {
                System.out.println("Den angiver tidligere og nuværende som udenfor.");
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
                wifiManager.startScan();

            }
        }
        else { inUse = false; }
    }

    private Boolean checkIfOutsideArea(List<ScanResult> scanResults) {
        double[] location = dm.getPredictionNN(scanResults);

        boolean outside = true;

        double epsilon = Math.ulp(1.0);
        double treshold = 1.0/location.length + epsilon;

        for (int i = 0; i < location.length; i++) {
            if(location[i] > treshold) {
                outside = false;
            }
        }

        System.out.println("Den prædikterer udenfor: " + outside);
        return outside;
    }


    private void determineLocation2(List<ScanResult> scanResults) {
        double[] location = dm.getPredictionNN(scanResults);
        int locationIndex = dmSVM.getPredictionSVM(scanResults);
        System.out.println("Room: " + locationsHardcodedForModelPurpose[locationIndex]);

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
            if(locationsHardcodedForModelPurpose[index].equals(previousLocation)) {
                predictedRoom = locationsHardcodedForModelPurpose[index];
            }
            else {
                quessedRoom = locationsHardcodedForModelPurpose[index];

                /*
                if(inUseTemp) {
                    //lastLocationFragmentTextView = "Gætter på: " + quessedRoom + ", men er stadigvæk: " + predictedRoom;
                    lastLocationFragmentTextView = "Gætter på: " + quessedRoom + ", men er stadigvæk: " + predictedRoom;
                    locationFragment.SetTextView(lastLocationFragmentTextView);
                }
                 */
                previousLocation = locationsHardcodedForModelPurpose[index];

            }
        }
        if(inUseTemp) {
            if (predictedRoom != null && predictedRoom.equals("Intet Rum")) {
                service.handleRequest("pause");
                playing = false;
            }
            else if (quessedRoom.equals("")) {
                Boolean result = locationFragment.updateSpeaker(predictedRoom);
                playing = result;
                // lastLocationFragmentTextView = "Lokation: " + predictedRoom;
                if (result) {
                    lastLocationFragmentTextView = predictedRoom;
                    locationFragment.SetTextView(lastLocationFragmentTextView);
                }
            }
            wifiManager.startScan();
        }
        else { inUse = false; }
    }

    /*
    private static void printLocation(List<ScanResult> scanResults) {
        double[] location = dm.getPredictionNN(scanResults);

        int index = getMaxIndex(location);
        String predictedRoom = getPredictedRoom(index, location);
        String guessedRoom = getGuessedRoom(index, location, predictedRoom);

        if (inUseTemp) {
            if (!guessedRoom.isEmpty()) {
                String lastLocation = "Gætter på: " + guessedRoom + ", men er stadigvæk: " + predictedRoom;
                locationFragment.SetTextView(lastLocation);
                previousLocation = guessedRoom;
            } else {
                String currentLocation = "Lokation: " + predictedRoom;
                locationFragment.updateSpeaker(predictedRoom);
                locationFragment.SetTextView(currentLocation);
                previousLocation = predictedRoom;
            }
            wifiManager.startScan();
        } else {
            inUse = false;
        }
    }

    private static int getMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static String getPredictedRoom(int index, double[] location) {
        if (location[index] < 0.70) {
            return "Intet Rum";
        } else {
            return locations[index];
        }
    }

    private static String getGuessedRoom(int index, double[] location, String predictedRoom) {
        String guessedRoom = "";
        if (location[index] < 0.70) {
            if (!previousLocation.equals("Intet Rum")) {
                guessedRoom = "Intet Rum";
            }
        } else {
            if (!locations[index].equals(previousLocation)) {
                guessedRoom = locations[index];
            }
        }
        return guessedRoom;
    }

     */


    public void updateLocationSpeakerName(Hashtable<String, String> _locationSpeakerName) {
        System.out.println("Hashtable mainactivity old: " + locationSpeakerName);
        locationSpeakerName = _locationSpeakerName;
        System.out.println("Hashtable mainactivity updated: " + locationSpeakerName);
    }

    public void loadSettings(){
        Settings settings = fileSystem.loadSettings();

        if (settings != null) {
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
    public static void startScan() { wifiManager.startScan(); }
    public String getFileName() { return fileName; }
    public String[] getLocation() { return locations; }
    public Boolean getInUse() { return inUse; }
    public Boolean getInUseTemp() { return inUseTemp; }
    public String getLastLocationFragmentTextView() { return lastLocationFragmentTextView; }
    public IService getService() { return service; }
    public WifiReceiver getWifiReceiver() { return wifiReceiver; }
    public static WifiManager getWifiManager() { return wifiManager; }
    public static Hashtable<String, String> getLocationSpeakerName() { return locationSpeakerName; }
    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public Bitmap getCoverImage() { return coverImage; }
    public Boolean getPlaying() { return playing; }
    public String getRoomCurrentlyScanning() { return roomCurrentlyScanning; }
    public Boolean getInUseDataCollection() { return inUseDataCollection; }
    public String getPlayingSpeaker() { return playingSpeaker; }
    public IFileSystem getFileSystem() { return fileSystem; }
    public void attachMusicFragment(MusicFragment musicFragment) { this.musicFragment = musicFragment; }
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