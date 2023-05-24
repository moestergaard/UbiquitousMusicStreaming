package com.example.ubiquitousmusicstreaming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.ubiquitousmusicstreaming.ui.configuration.Device;
import com.example.ubiquitousmusicstreaming.ui.music.MusicFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpotifyService implements IService {

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
    private MusicFragment musicFragment;
    private Hashtable<String, String> speakerNameId = new Hashtable<String, String>();
    private Boolean playing;
    private String playingSpeaker = "";

    public SpotifyService(Context context) {
        this.context = context;
        connect();
    }

    private void retrieveAccessToken() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-email", "user-read-currently-playing", "user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity((Activity) context, REQUEST_CODE, request);
    }

    private void subscribeToTrack() {
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            Track track = playerState.track;
            String trackName = track.name;
            String artistName = track.artist.name;
            ImageUri coverImage = track.imageUri;
            playing = !playerState.isPaused;
            updateMusicFragment(trackName, artistName, coverImage, playing);
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void refreshSpeakers() {
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

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            tmpDevices.add(device);
                        }
                        responseMessage = "Received";
                        devices = tmpDevices;
                        updateSpeakerNameId();

                    } catch (IOException e) {
                        refreshSpeakers();
                        throw new RuntimeException(e);
                    }
                }
                else { refreshSpeakers(); }
            }
        });
    }

    private void updateSpeakerNameId() {
        if (!devices.isEmpty()) {
            for (Device d : devices) {
                speakerNameId.put(d.getName(), d.getId());
                if (d.isActive()) {
                    playingSpeaker = d.getName();
                    updateMusicFragmentSpeaker();
                }
            }
        }
    }

    private void updateMusicFragment(String trackName, String artistName, ImageUri coverImage, Boolean playing)
    {
        musicFragment.updateTrackInformation(trackName, artistName, coverImage);
        musicFragment.updatePlaying(playing);
    }
    private void updateMusicFragmentPlaying() {musicFragment.updatePlaying(playing);}
    private void updateMusicFragmentSpeaker() { musicFragment.updatePlayingSpeaker(playingSpeaker); }

    public void connect() {
        SpotifyAppRemote.connect(context, new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build(), new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote _spotifyAppRemote) {
                spotifyAppRemote = _spotifyAppRemote;
                subscribeToTrack();
                retrieveAccessToken();
                refreshSpeakers();
                updateMusicFragmentPlaying();
                updateActiveDevice();
            }

            @Override
            public void onFailure(Throwable throwable) { }
        });
    }

    public void disconnect() {
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
            spotifyAppRemote = null;
        }
    }

    public void handleAuthorizationResponse(int resultCode, Intent intent) {
        AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

        switch (response.getType()) {
            case TOKEN:
                accessToken = response.getAccessToken();
                break;
            case ERROR:
                break;
            default:
        }
    }

    public void changeDevice(String speakerID) {
        JSONArray device = new JSONArray();
        device.put(speakerID);

        JSONObject body = new JSONObject();
        try {
            body.put("device_ids", device);
            body.put("play", true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(body));

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player")
                .put(requestBody)
                .addHeader("Authorization","Bearer " + accessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) { }
        });
    }

    /*
    public Boolean changePlayPause() {
        final Boolean[] isPlaying = {true};
        spotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .resume();
                                isPlaying[0] = false;
                                System.out.println("I spotify:" + isPlaying[0]);
                            } else {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .pause();
                            }
                        });
        System.out.println("end of method: " + isPlaying[0]);
        return isPlaying[0];
    }



    public Boolean checkIfPlaying() {
        final Boolean[] isPlaying = {true};
        spotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                isPlaying[0] = false;
                            } else {
                            }
                        });
        return isPlaying[0];
    }

     */

    public void updateActiveDevice() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization","Bearer " + accessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        JsonArray devicesJson = json.getAsJsonArray("devices");

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            if (device.isActive()) {
                                playingSpeaker = device.getName();
                                updateMusicFragmentSpeaker();
                            }
                        }
                    } catch (IOException e) {
                        refreshSpeakers();
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void handleRequest(String request) {
        if (request.equals("next")) {
            spotifyAppRemote
                    .getPlayerApi()
                    .skipNext();
        } else if (request.equals("prev")) {
            spotifyAppRemote
                    .getPlayerApi()
                    .skipPrevious();
        } else if (request.equals("play")) {
            spotifyAppRemote
                    .getPlayerApi()
                    .resume();
        } else if (request.equals("pause")) {
            spotifyAppRemote
                    .getPlayerApi()
                    .pause();
        }
    }

    public List<Device> getAvailableDevices() { return devices; }
    public String getAccessToken() { return accessToken; }
    public SpotifyAppRemote getSpotifyAppRemote() { return spotifyAppRemote; }
    public Hashtable<String, String> getDeviceNameId() { return speakerNameId; }
    public void attachMusicFragment(MusicFragment musicFragment) { this.musicFragment = musicFragment; }

}
