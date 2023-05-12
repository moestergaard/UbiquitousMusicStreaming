package com.example.ubiquitousmusicstreaming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.ubiquitousmusicstreaming.ui.configuration.Device;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.protocol.types.PlayerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Spotify {

    private Context context;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private static List<Device> devices = new ArrayList<>();
    private String responseMessage;
    private static final String CLIENT_ID = "6e101d8a913048819b5af5e6ee372b59";
    private static final String REDIRECT_URI = "ubiquitousmusicstreaming-login://callback";
    private Integer REQUEST_CODE = 42;
    private static SpotifyAppRemote spotifyAppRemote;
    private String accessToken;

    public Spotify(Context context) {
        this.context = context;
        //authorize();
        //refreshSpeakers();
    }

    public void connect() {
        SpotifyAppRemote.connect(context, new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build(), new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote _spotifyAppRemote) {
                spotifyAppRemote = _spotifyAppRemote;
                // Now you can start interacting with App Remote
                // ...

                // Retrieve access token after connecting
                retrieveAccessToken();
            }

            @Override
            public void onFailure(Throwable throwable) {
                // Something went wrong when attempting to connect
                // ...
            }
        });
    }

    public void retrieveAccessToken() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-email", "user-read-currently-playing", "user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity((Activity) context, REQUEST_CODE, request);
    }

    public void handleAuthorizationResponse(int resultCode, Intent intent) {
        AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

        switch (response.getType()) {
            case TOKEN:
                accessToken = response.getAccessToken();
                // Access token has been retrieved
                // ...
                break;
            case ERROR:
                // Handle error response
                break;
            default:
                // Handle other cases
        }
    }

    public void disconnect() {
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
            spotifyAppRemote = null;
        }
    }

    /*

    public void authorize() {
        // Authorize spotify
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-email", "user-read-currently-playing", "user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity((Activity) context, REQUEST_CODE, request);
    }

     */

    public void refreshSpeakers() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Authorization","Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                responseMessage = "Failure";
                System.out.println("Noget gik galt.");
                refreshSpeakers();
            }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        JsonArray devicesJson = json.getAsJsonArray("devices");

                        List<Device> tmpDevices = new ArrayList<>();

                        responseMessage = "Received";

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            tmpDevices.add(device);
                        }

                        devices = tmpDevices;

                    } catch (IOException e) {
                        refreshSpeakers();
                        throw new RuntimeException(e);
                    }
                }
                else { refreshSpeakers(); }
            }
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public List<Device> getAvailableSpeakers() {
        // refreshSpeakers();
        return devices;
    }

    public void playPlaylist(String uri) {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().play(uri);
        }
    }

    /*
    public void subscribeToPlayerState(SpotifyAppRemote.PlayerStateCallback callback) {
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(callback);
        }
    }

     */


    public String getResponseMessage() {
        return responseMessage;
    }

    public String getAccessToken() { return accessToken; }
    public SpotifyAppRemote getSpotifyAppRemote() { return spotifyAppRemote; }

    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mainActivity.onActivityResult(requestCode, resultCode, intent);

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

     */

    /*
    protected void onStart() {
        mainActivity.super.onStart();
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
/*
}


    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // Aaand we will finish off here.

        //mSpotifyAppRemote.getConnectApi().

    }


 */

}
