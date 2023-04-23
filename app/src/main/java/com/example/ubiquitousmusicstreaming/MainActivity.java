package com.example.ubiquitousmusicstreaming;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;

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

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
//import com.spotify.sdk.android.auth.AuthorizationClient;
//import com.spotify.sdk.android.auth.AuthorizationRequest;
//import com.spotify.sdk.android.auth.AuthorizationResponse;


public class MainActivity extends AppCompatActivity {

    //static OkHttpClient mOkHttpClient;
    private ActivityMainBinding binding;
    private Integer REQUEST_CODE = 42;
    private static WifiReceiver wifiReceiver;
    private static WifiManager wifiManager;
    private static ConfigurationFragment configurationFragment;
    private static LocationFragment locationFragment;
    private static MusicFragment musicFragment;
    private static Boolean inUse = false, inUseTemp;
    private static DataManagement dm;
    private static String predictedRoom, previousLocation;
    private static String[] locations = new String[]{"Kontor", "Stue", "Køkken"};

    private static final String CLIENT_ID = "6e101d8a913048819b5af5e6ee372b59";
    //public static final String CLIENT_ID = "0bda033615af412eb05a8ce97d44fec2";
    private static final String REDIRECT_URI = "ubiquitousmusicstreaming-login://callback";
    //private static final String REDIRECT_URI = "http://com.yourdomain.yourapp/callback";
    private static SpotifyAppRemote mSpotifyAppRemote;
    private static String ACCESS_TOKEN;
    private Hashtable<String, String> locationSpeakerID;
    private String fileName;


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
        FileSystemTest fileSystemTest = new FileSystemTest(this);
        fileSystemTest.overAllTestMethod();

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
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

        //mOkHttpClient = new OkHttpClient();

        // Authorize spotify
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-email", "user-read-currently-playing", "user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);

        loadSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    ACCESS_TOKEN = response.getAccessToken();
                    // Handle successful response
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }


    private void connected() {
        // Then we will write some more code here.
        // Play a playlist

        //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX7K31D69s4M1");


        System.out.println(mSpotifyAppRemote.getConnectApi());



        // Subscribe to PlayerState
        /*
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

         */
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // Aaand we will finish off here.

        //mSpotifyAppRemote.getConnectApi().

    }

    public static WifiReceiver getWifiReceiver() {
        return wifiReceiver;
    }

    public static WifiManager getWifiManager() {
        return wifiManager;
    }

    public static void startScan() {
        wifiManager.startScan();
    }

    public static String getAccessToken() {
        return ACCESS_TOKEN;
    }

    public static SpotifyAppRemote getmSpotifyAppRemote() { return mSpotifyAppRemote; }

    //public static OkHttpClient getOkHttpClient() { return mOkHttpClient; }

    public void setInUse(Boolean bool) {
        if (bool) {
            inUse = inUseTemp = true;
        } else {
            inUseTemp = false;
        }
    }

    public void attachMusicFragment(MusicFragment musicFragment) { this.musicFragment = musicFragment; }
    public void attachConfigurationFragment(ConfigurationFragment configurationFragment) { this.configurationFragment = configurationFragment; }

    public void attachLocationFragment(LocationFragment locationFragment) { this.locationFragment = locationFragment; }

    public void removeLocationFragment() {locationFragment = null ;}

    public static void update() {
        if(!inUse) {
            if(configurationFragment != null) {
                configurationFragment.update();
            }
        }
        else {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            printLocation(scanResults);
        }
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
                    locationFragment.SetTextView("Gætter på: " + quessedRoom + ", men er stadigvæk: " + predictedRoom);
                }
                previousLocation = locations[index];
            }
        }
        if(inUseTemp) {
            if (quessedRoom.equals("")) {
                musicFragment.updateSpeaker(predictedRoom);
                locationFragment.SetTextView("Lokation: " + predictedRoom);
            }

            //System.out.println("Lokation: " + location);
            //System.out.println("Lokation string: " + location.toString());
            //System.out.println("Lokation length: " + location.length);
            wifiManager.startScan();
            //for (double d : location) {
            //    System.out.println("Value: " + d);
            //}
        }
        else { inUse = false; }
    }

    public void updateLocationSpeakerID(Hashtable<String, String> _locationSpeakerID) {
        locationSpeakerID = _locationSpeakerID;
    }

    public Hashtable<String, String> getLocationSpeakerID() {
        return locationSpeakerID;
    }

    public void loadSettings(){
        Settings settings = FileSystem.readObjectFromFile(this);
        //Settings settings = FileSystem.loadSettings(new View(this));

        if (settings != null) {
            String fileName = settings.getFileName();
            if (fileName != null) {
                this.fileName = fileName;
                /*
                if (configurationFragment != null) {
                    configurationFragment.setFileName(fileName);
                }
                */
            }

            locationSpeakerID = settings.getLocationSpeakerID();

            String[] settingLocations = settings.getLocations();

            if (settingLocations != null) {
                locations = settingLocations;
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String[] getLocation() {
        return locations;
    }
}