package com.example.ubiquitousmusicstreaming.Services;

import android.content.Intent;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.Models.Device;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
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

    private final MainActivity mainActivity;
    private Call mCall;
    private final OkHttpClient mOkHttpClient;
    private static List<Device> devices = new ArrayList<>();
    private static final String CLIENT_ID = "6e101d8a913048819b5af5e6ee372b59";
    private static final String REDIRECT_URI = "ubiquitousmusicstreaming-login://callback";
    private static SpotifyAppRemote spotifyAppRemote;
    private String accessToken;
    private final Hashtable<String, String> speakerNameId = new Hashtable<String, String>();
    private Boolean playing;
    private String playingSpeaker = "";

    public SpotifyService(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mOkHttpClient = new OkHttpClient();
        connect();
    }

    private void retrieveAccessToken() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-read-email", "user-read-currently-playing", "user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        int REQUEST_CODE = 42;
        AuthorizationClient.openLoginActivity(mainActivity, REQUEST_CODE, request);
    }

    private void subscribeToTrack() {
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            Track track = playerState.track;
            String trackName = track.name;
            String artistName = track.artist.name;
            ImageUri coverImage = track.imageUri;
            playing = !playerState.isPaused;

            updateMainActivity(trackName, artistName, coverImage, playing);
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

        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                refreshSpeakers();
            }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        JsonArray devicesJson = json.getAsJsonArray("devices");
                        List<Device> tmpDevices = new ArrayList<>();

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            tmpDevices.add(device);
                        }
                        devices = tmpDevices;
                        updateSpeakerNameId();
                        response.close();
                        cancelCall();
                    } catch (IOException e) {
                        e.printStackTrace();
                        response.close();
                        cancelCall();
                        refreshSpeakers();
                    }
                }
                else {
                    response.close();
                    cancelCall();
                    refreshSpeakers();
                }
            }
        });
    }

    private void updateSpeakerNameId() {
        if (!devices.isEmpty()) {
            for (Device d : devices) {
                speakerNameId.put(d.getName(), d.getId());
                if (d.isActive()) {
                    playingSpeaker = d.getName();
                    updateMainActivitySpeaker();
                }
            }
        }
    }

    private void updateMainActivity(String trackName, String artistName, ImageUri coverImage, Boolean playing)
    {
        updateCoverImage(coverImage);
        mainActivity.updateTrackInformation(trackName, artistName);
        updateMainActivityPlaying();
    }

    private void updateCoverImage(ImageUri coverImage) {
        spotifyAppRemote
                .getImagesApi()
                .getImage(coverImage, Image.Dimension.LARGE)
                .setResultCallback(
                        bitmap -> {
                            mainActivity.updateCoverImage(bitmap);
                        });
    }

    private void updateMainActivityPlaying() { mainActivity.updatePlaying(playing); }
    private void updateMainActivitySpeaker() { mainActivity.updateSpeaker(playingSpeaker); }

    public void connect() {
        SpotifyAppRemote.connect(mainActivity, new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build(), new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote _spotifyAppRemote) {
                spotifyAppRemote = _spotifyAppRemote;
                subscribeToTrack();
                retrieveAccessToken();
                refreshSpeakers();
                updateMainActivityPlaying();
                updateActiveDevice();
            }

            @Override
            public void onFailure(Throwable throwable) { throwable.printStackTrace(); }
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

        if (response.getType() == AuthorizationResponse.Type.TOKEN) {
            accessToken = response.getAccessToken();
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
            e.printStackTrace();
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(body));

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player")
                .put(requestBody)
                .addHeader("Authorization","Bearer " + accessToken)
                .build();

        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); cancelCall(); }
            @Override
            public void onResponse(Call call, Response response) { response.close(); cancelCall(); }
        });
    }

    public void changeActivationOfDevice() {
        spotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .resume();
                                playing = true;
                            } else {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .pause();
                                playing = false;
                            }
                            updateMainActivityPlaying();
                        });
    }

    public void updateActiveDevice() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization","Bearer " + accessToken)
                .build();

        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); cancelCall(); }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        JsonArray devicesJson = json.getAsJsonArray("devices");

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            if (device.isActive()) {
                                playingSpeaker = device.getName();
                                updateMainActivitySpeaker();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        response.close();
                        cancelCall();
                        refreshSpeakers();
                    }
                }
                response.close();
                cancelCall();
            }
        });
    }

    public void handleRequest(String request) {
        switch (request) {
            case "next":
                spotifyAppRemote
                        .getPlayerApi()
                        .skipNext();
                break;
            case "prev":
                spotifyAppRemote
                        .getPlayerApi()
                        .skipPrevious();
                break;
            case "play":
                spotifyAppRemote
                        .getPlayerApi()
                        .resume();
                break;
            case "pause":
                spotifyAppRemote
                        .getPlayerApi()
                        .pause();
                break;
        }
    }
    public List<Device> getAvailableDevices() { return devices; }
    public Hashtable<String, String> getDeviceNameId() { return speakerNameId; }
}
