package com.example.ubiquitousmusicstreaming;

import com.example.ubiquitousmusicstreaming.ui.configuration.Device;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Spotify {

    private MainActivity mainActivity;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private static List<Device> devices = new ArrayList<>();
    private String responseMessage;

    public Spotify(MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        refreshSpeakers();
    }

    public void refreshSpeakers() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
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

    public String getResponseMessage() {
        return responseMessage;
    }


}
