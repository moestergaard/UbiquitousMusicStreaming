package com.example.ubiquitousmusicstreaming.ui.music;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
//import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;

public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;

    private static final String TAG = MusicFragment.class.getSimpleName();

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "6e101d8a913048819b5af5e6ee372b59";
    private static final String REDIRECT_URI = "ubiquitousmusicstreaming-login://callback";

    private static final String TRACK_URI = "spotify:track:4IWZsfEkaK49itBwCTFDXQ";
    private static final String ALBUM_URI = "spotify:album:4nZ5wPL5XxSY2OuDgbnYdc";
    private static final String ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2";
    private static final String PLAYLIST_URI = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF";
    private static final String PODCAST_URI = "spotify:show:2tgPYIeGErjk6irHRhk9kj";

    // public static final String CLIENT_ID = "0bda033615af412eb05a8ce97d44fec2";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;

    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;
    private String mAccessCode;
    private Call mCall;

    private MainActivity mainActivity;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static String ACCESS_TOKEN, playingLocation = "";
    private Button btnToken, btnProfile, btnSpeakers;
    private static TextView txtViewUserProfile;
    private static Hashtable<String, String> locationSpeakerID = new Hashtable<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MusicViewModel musicViewModel =
                new ViewModelProvider(this).get(MusicViewModel.class);

        binding = FragmentMusicBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) getParentFragment().getActivity();
        mainActivity.attachMusicFragment(MusicFragment.this);

        ACCESS_TOKEN = mainActivity.getAccessToken();
        mSpotifyAppRemote = mainActivity.getmSpotifyAppRemote();
        View root = binding.getRoot();

        btnToken = binding.tokenButton;
        btnProfile = binding.buttonGetProfile;
        btnSpeakers = binding.buttonGetSpeakers;
        txtViewUserProfile = binding.responseTextView;

        //locationSpeakerID = mainActivity.getLocationSpeakerID();
        initializeLocationSpeakerID();


        btnToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView textView = binding.tokenTextView;
                ACCESS_TOKEN = mainActivity.getAccessToken();
                textView.setText(ACCESS_TOKEN);
                System.out.println(ACCESS_TOKEN);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/me")
                        .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
                        .build();

                cancelCall();
                mCall = mOkHttpClient.newCall(request);

                mCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        setResponse("Failed to fetch data: " + e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject jsonObject = new JSONObject(response.body().string());
                            setResponse(jsonObject.toString(3));
                        } catch (JSONException e) {
                            setResponse("Failed to parse data: " + e);
                        }
                    }
                });
            }
        });


        btnSpeakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/me/player/devices")
                        .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
                        .build();

                cancelCall();
                mCall = mOkHttpClient.newCall(request);

                mCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        setResponse("Failed to fetch data: " + e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject jsonObject = new JSONObject(response.body().string());
                            setResponse(jsonObject.toString(3));
                        } catch (JSONException e) {
                            setResponse("Failed to parse data: " + e);
                        }
                    }
                });
            }
        });
        return root;
    }

    private void initializeLocationSpeakerID() {
        locationSpeakerID.put("Stue", "1af54257b625e17733f383612d7e027ad658bee2");
        locationSpeakerID.put("Kontor", "9421d826c2f75f49a95085a1063b9f74c8581cbb");
        locationSpeakerID.put("Køkken", "55ee551079c70a6e720fb7af7ed1455b050f0c37");
    }

    // Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateSpeaker(String location) {
        if(playingLocation != location) {
            playingLocation = location;

            String speakerID = locationSpeakerID.get(location);

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
                    .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
                    .build();

            cancelCall();
            mCall = mOkHttpClient.newCall(request);

            mCall.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    setResponse("Noget gik galt.");
                }
                @Override
                public void onResponse(Call call, Response response) {
                    System.out.println("Der kom response");
                }
            });
        }
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void setResponse(String response) {
        System.out.println("Reponse to put in txt view: " + response);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtViewUserProfile.setText(response);
            }
        });
    }
}