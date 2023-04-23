package com.example.ubiquitousmusicstreaming.ui.music;

//package com.spotify.sdk.demo;
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
import com.example.ubiquitousmusicstreaming.TrackProgressBar;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;
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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.PlaybackSpeed;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;


import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;
    private static final String TAG = MusicFragment.class.getSimpleName();

    private static final String TRACK_URI = "spotify:track:4IWZsfEkaK49itBwCTFDXQ";
    private static final String ALBUM_URI = "spotify:album:4nZ5wPL5XxSY2OuDgbnYdc";
    private static final String ARTIST_URI = "spotify:artist:3WrFJ7ztbogyGnTHbHJFl2";
    private static final String PLAYLIST_URI = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF";
    private static final String PODCAST_URI = "spotify:show:2tgPYIeGErjk6irHRhk9kj";

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "6e101d8a913048819b5af5e6ee372b59";
    private static final String REDIRECT_URI = "ubiquitousmusicstreaming-login://callback";

    private static SpotifyAppRemote mSpotifyAppRemote;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Button mConnectButton, mConnectAuthorizeButton;
    Button mSubscribeToPlayerContextButton;
    Button mPlayerContextButton;
    Button mSubscribeToPlayerStateButton;
    Button mPlayerStateButton;
    ImageView mCoverArtImageView;
    AppCompatTextView mImageLabel;
    AppCompatTextView mImageScaleTypeLabel;
    AppCompatImageButton mToggleShuffleButton;
    AppCompatImageButton mPlayPauseButton;
    AppCompatImageButton mToggleRepeatButton;
    AppCompatSeekBar mSeekBar;
    AppCompatImageButton mPlaybackSpeedButton;

    List<View> mViews;
    TrackProgressBar mTrackProgressBar;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;
    Subscription<Capabilities> mCapabilitiesSubscription;

    // private final ErrorCallback mErrorCallback = this::logError;

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                    mPlayerContextButton.setText(
                            String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
                    mPlayerContextButton.setTag(playerContext);
                }
            };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {

                    Drawable drawable =
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.mediaservice_shuffle, getTheme());
                    if (!playerState.playbackOptions.isShuffling) {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(mToggleShuffleButton.getDrawable(), Color.WHITE);
                    } else {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(
                                mToggleShuffleButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    }

                    if (playerState.playbackOptions.repeatMode == Repeat.ALL) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_all);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else if (playerState.playbackOptions.repeatMode == Repeat.ONE) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_one);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_off);
                        DrawableCompat.setTint(mToggleRepeatButton.getDrawable(), Color.WHITE);
                    }

                    if (playerState.track != null) {
                        mPlayerStateButton.setText(
                                String.format(
                                        Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name));
                        mPlayerStateButton.setTag(playerState);
                    }
                    // Update progressbar
                    if (playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }

                    // Invalidate play / pause
                    if (playerState.isPaused) {
                        mPlayPauseButton.setImageResource(R.drawable.btn_play);
                    } else {
                        mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                    }

                    // Invalidate playback speed
                    mPlaybackSpeedButton.setVisibility(View.VISIBLE);
                    if (playerState.playbackSpeed == 0.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_50);
                    } else if (playerState.playbackSpeed == 0.8f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_80);
                    } else if (playerState.playbackSpeed == 1f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_100);
                    } else if (playerState.playbackSpeed == 1.2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_120);
                    } else if (playerState.playbackSpeed == 1.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_150);
                    } else if (playerState.playbackSpeed == 2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_200);
                    } else if (playerState.playbackSpeed == 3f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_300);
                    }
                    if (playerState.track != null && playerState.track.isPodcast && playerState.track.isEpisode) {
                        mPlaybackSpeedButton.setEnabled(true);
                        mPlaybackSpeedButton.clearColorFilter();
                    } else {
                        mPlaybackSpeedButton.setEnabled(false);
                        mPlaybackSpeedButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (playerState.track != null) {
                        // Get image from track
                        mSpotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(
                                        bitmap -> {
                                            mCoverArtImageView.setImageBitmap(bitmap);
                                            mImageLabel.setText(
                                                    String.format(
                                                            Locale.ENGLISH, "%d x %d", bitmap.getWidth(), bitmap.getHeight()));
                                        });
                        // Invalidate seekbar length and position
                        mSeekBar.setMax((int) playerState.track.duration);
                        mTrackProgressBar.setDuration(playerState.track.duration);
                        mTrackProgressBar.update(playerState.playbackPosition);
                    }

                    mSeekBar.setEnabled(true);
                }
            };




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




    /*
    private static SpotifyAppRemote mSpotifyAppRemote;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

     */

    Button mConnectButton, mConnectAuthorizeButton;
    Button mSubscribeToPlayerContextButton;
    Button mPlayerContextButton;
    Button mSubscribeToPlayerStateButton;
    Button mPlayerStateButton;
    ImageView mCoverArtImageView;
    AppCompatTextView mImageLabel;
    AppCompatTextView mImageScaleTypeLabel;
    AppCompatImageButton mToggleShuffleButton;
    AppCompatImageButton mPlayPauseButton;
    AppCompatImageButton mToggleRepeatButton;
    AppCompatSeekBar mSeekBar;
    AppCompatImageButton mPlaybackSpeedButton;

    List<View> mViews;

    /*
    TrackProgressBar mTrackProgressBar;


    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;
    Subscription<Capabilities> mCapabilitiesSubscription;

    private final ErrorCallback mErrorCallback = this::logError;

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                    mPlayerContextButton.setText(
                            String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
                    mPlayerContextButton.setTag(playerContext);
                }
            };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {

                    Drawable drawable =
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.mediaservice_shuffle, getTheme());
                    if (!playerState.playbackOptions.isShuffling) {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(mToggleShuffleButton.getDrawable(), Color.WHITE);
                    } else {
                        mToggleShuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(
                                mToggleShuffleButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    }

                    if (playerState.playbackOptions.repeatMode == Repeat.ALL) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_all);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else if (playerState.playbackOptions.repeatMode == Repeat.ONE) {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_one);
                        DrawableCompat.setTint(
                                mToggleRepeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else {
                        mToggleRepeatButton.setImageResource(R.drawable.mediaservice_repeat_off);
                        DrawableCompat.setTint(mToggleRepeatButton.getDrawable(), Color.WHITE);
                    }

                    if (playerState.track != null) {
                        mPlayerStateButton.setText(
                                String.format(
                                        Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name));
                        mPlayerStateButton.setTag(playerState);
                    }
                    // Update progressbar
                    if (playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }

                    // Invalidate play / pause
                    if (playerState.isPaused) {
                        mPlayPauseButton.setImageResource(R.drawable.btn_play);
                    } else {
                        mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                    }

                    // Invalidate playback speed
                    mPlaybackSpeedButton.setVisibility(View.VISIBLE);
                    if (playerState.playbackSpeed == 0.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_50);
                    } else if (playerState.playbackSpeed == 0.8f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_80);
                    } else if (playerState.playbackSpeed == 1f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_100);
                    } else if (playerState.playbackSpeed == 1.2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_120);
                    } else if (playerState.playbackSpeed == 1.5f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_150);
                    } else if (playerState.playbackSpeed == 2f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_200);
                    } else if (playerState.playbackSpeed == 3f) {
                        mPlaybackSpeedButton.setImageResource(R.drawable.ic_playback_speed_300);
                    }
                    if (playerState.track != null && playerState.track.isPodcast && playerState.track.isEpisode) {
                        mPlaybackSpeedButton.setEnabled(true);
                        mPlaybackSpeedButton.clearColorFilter();
                    } else {
                        mPlaybackSpeedButton.setEnabled(false);
                        mPlaybackSpeedButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (playerState.track != null) {
                        // Get image from track
                        mSpotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(
                                        bitmap -> {
                                            mCoverArtImageView.setImageBitmap(bitmap);
                                            mImageLabel.setText(
                                                    String.format(
                                                            Locale.ENGLISH, "%d x %d", bitmap.getWidth(), bitmap.getHeight()));
                                        });
                        // Invalidate seekbar length and position
                        mSeekBar.setMax((int) playerState.track.duration);
                        mTrackProgressBar.setDuration(playerState.track.duration);
                        mTrackProgressBar.update(playerState.playbackPosition);
                    }

                    mSeekBar.setEnabled(true);
                }
            };
    */

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

        /*
        btnToken = binding.tokenButton;
        btnProfile = binding.buttonGetProfile;
        btnSpeakers = binding.buttonGetSpeakers;
        txtViewUserProfile = binding.responseTextView;
         */

        //locationSpeakerID = mainActivity.getLocationSpeakerID();
        initializeLocationSpeakerID();

        /*
        btnToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView textView = binding.tokenTextView;
                ACCESS_TOKEN = mainActivity.getAccessToken();
                textView.setText(ACCESS_TOKEN);
                System.out.println(ACCESS_TOKEN);
            }
        });
         */

        /*
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

         */

        /*
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

         */



        //final TextView textView = binding.textHome;
        //musicViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
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

    /*
    private void initializeLocationSpeakerID() {
        locationSpeakerID.put("Køkken", "9421d826c2f75f49a95085a1063b9f74c8581cbb");
        locationSpeakerID.put("Kontor", "55ee551079c70a6e720fb7af7ed1455b050f0c37");
        locationSpeakerID.put("Stue", "1af54257b625e17733f383612d7e027ad658bee2");
    }

     */






    /*
    public void onGetSpeakersClicked(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(getView(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Authorization","Bearer " + mAccessToken)
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

    public void onGetUserProfileClicked(View view) {
        if (mAccessToken == null) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main), R.string.warning_need_token, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            return;
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization","Bearer " + mAccessToken)
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

    public void onRequestCodeClicked(View view) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void onRequestTokenClicked(View view) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                //.setScopes(new String[]{"user-read-email"})
                .setScopes(new String[]{"australiadk17@gmail.com"})
                //.setCampaign("your-campaign-token")
                .setCampaign("e8123d225a2341c1a4780d37a70c89d6")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        if (response.getError() != null && !response.getError().isEmpty()) {
            setResponse(response.getError());
        }
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            updateTokenView();
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.getCode();
            updateCodeView();
        }
    }

    private void setResponse(final String text) {
        runOnUiThread(() -> {
            final TextView responseView = findViewById(R.id.response_text_view);
            responseView.setText(text);
        });
    }

    private void updateTokenView() {
        final TextView tokenView = findViewById(R.id.token_text_view);
        tokenView.setText(getString(R.string.token, mAccessToken));
    }

    private void updateCodeView() {
        final TextView codeView = findViewById(R.id.code_text_view);
        codeView.setText(getString(R.string.code, mAccessCode));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }


     */



        /*
        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.app_remote_layout);

            mConnectButton = findViewById(R.id.connect_button);
            mConnectAuthorizeButton = findViewById(R.id.connect_authorize_button);
            mPlayerContextButton = findViewById(R.id.current_context_label);
            mSubscribeToPlayerContextButton = findViewById(R.id.subscribe_to_player_context_button);
            mCoverArtImageView = findViewById(R.id.image);
            mImageLabel = findViewById(R.id.image_label);
            mImageScaleTypeLabel = findViewById(R.id.image_scale_type_label);
            mPlayerStateButton = findViewById(R.id.current_track_label);
            mSubscribeToPlayerStateButton = findViewById(R.id.subscribe_to_player_state_button);
            mPlaybackSpeedButton = findViewById(R.id.playback_speed_button);
            mToggleRepeatButton = findViewById(R.id.toggle_repeat_button);
            mToggleShuffleButton = findViewById(R.id.toggle_shuffle_button);
            mPlayPauseButton = findViewById(R.id.play_pause_button);

            mSeekBar = findViewById(R.id.seek_to);
            mSeekBar.setEnabled(false);
            mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            mTrackProgressBar = new TrackProgressBar(mSeekBar);

            mViews =
                    Arrays.asList(
                            findViewById(R.id.disconnect_button),
                            mSubscribeToPlayerContextButton,
                            mSubscribeToPlayerStateButton,
                            mImageLabel,
                            mImageScaleTypeLabel,
                            mPlayPauseButton,
                            findViewById(R.id.seek_forward_button),
                            findViewById(R.id.seek_back_button),
                            findViewById(R.id.skip_prev_button),
                            findViewById(R.id.skip_next_button),
                            mToggleRepeatButton,
                            mToggleShuffleButton,
                            findViewById(R.id.connect_switch_to_local),
                            findViewById(R.id.play_podcast_button),
                            findViewById(R.id.play_track_button),
                            findViewById(R.id.play_album_button),
                            findViewById(R.id.play_artist_button),
                            findViewById(R.id.play_playlist_button),
                            findViewById(R.id.subscribe_to_capabilities),
                            findViewById(R.id.get_collection_state),
                            findViewById(R.id.remove_uri),
                            findViewById(R.id.save_uri),
                            findViewById(R.id.get_fitness_recommended_items_button),
                            mSeekBar);

            SpotifyAppRemote.setDebugMode(true);

            onDisconnected();
            onConnectAndAuthorizedClicked(null);
        }

        @Override
        protected void onStop() {
            super.onStop();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            onDisconnected();
        }

        private void onConnected() {
            for (View input : mViews) {
                input.setEnabled(true);
            }
            mConnectButton.setEnabled(false);
            mConnectButton.setText(R.string.connected);
            mConnectAuthorizeButton.setEnabled(false);
            mConnectAuthorizeButton.setText(R.string.connected);

            onSubscribedToPlayerStateButtonClicked(null);
            onSubscribedToPlayerContextButtonClicked(null);
        }

        private void onConnecting() {
            mConnectButton.setEnabled(false);
            mConnectButton.setText(R.string.connecting);
            mConnectAuthorizeButton.setEnabled(false);
            mConnectAuthorizeButton.setText(R.string.connecting);
        }

        private void onDisconnected() {
            for (View view : mViews) {
                view.setEnabled(false);
            }
            mConnectButton.setEnabled(true);
            mConnectButton.setText(R.string.connect);
            mConnectAuthorizeButton.setEnabled(true);
            mConnectAuthorizeButton.setText(R.string.authorize);
            mCoverArtImageView.setImageResource(R.drawable.widget_placeholder);
            mPlayerContextButton.setText(R.string.title_player_context);
            mPlayerStateButton.setText(R.string.title_current_track);
            mToggleRepeatButton.clearColorFilter();
            mToggleRepeatButton.setImageResource(R.drawable.btn_repeat);
            mToggleShuffleButton.clearColorFilter();
            mToggleShuffleButton.setImageResource(R.drawable.btn_shuffle);
            mPlayerContextButton.setVisibility(View.INVISIBLE);
            mSubscribeToPlayerContextButton.setVisibility(View.VISIBLE);
            mPlayerStateButton.setVisibility(View.INVISIBLE);
            mSubscribeToPlayerStateButton.setVisibility(View.VISIBLE);
        }

        public void onConnectClicked(View v) {
            onConnecting();
            connect(false);
        }

        public void onConnectAndAuthorizedClicked(View view) {
            onConnecting();
            connect(true);
        }

        private void connect(boolean showAuthView) {

            SpotifyAppRemote.disconnect(mSpotifyAppRemote);

            SpotifyAppRemote.connect(
                    getApplicationContext(),
                    new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(showAuthView)
                            .build(),
                    new Connector.ConnectionListener() {
                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            RemotePlayerActivity.this.onConnected();
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            logError(error);
                            RemotePlayerActivity.this.onDisconnected();
                        }
                    });
        }

        public void onDisconnectClicked(View v) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            onDisconnected();
        }

        public void onImageClicked(View view) {
            if (mSpotifyAppRemote != null) {
                mSpotifyAppRemote
                        .getPlayerApi()
                        .getPlayerState()
                        .setResultCallback(
                                playerState -> {
                                    PopupMenu menu = new PopupMenu(this, view);

                                    menu.getMenu().add(720, 720, 0, "Large (720px)");
                                    menu.getMenu().add(480, 480, 1, "Medium (480px)");
                                    menu.getMenu().add(360, 360, 2, "Small (360px)");
                                    menu.getMenu().add(240, 240, 3, "X Small (240px)");
                                    menu.getMenu().add(144, 144, 4, "Thumbnail (144px)");

                                    menu.show();

                                    menu.setOnMenuItemClickListener(
                                            item -> {
                                                mSpotifyAppRemote
                                                        .getImagesApi()
                                                        .getImage(
                                                                playerState.track.imageUri, Image.Dimension.values()[item.getOrder()])
                                                        .setResultCallback(
                                                                bitmap -> {
                                                                    mCoverArtImageView.setImageBitmap(bitmap);
                                                                    mImageLabel.setText(
                                                                            String.format(
                                                                                    Locale.ENGLISH,
                                                                                    "%d x %d",
                                                                                    bitmap.getWidth(),
                                                                                    bitmap.getHeight()));
                                                                });
                                                return false;
                                            });
                                })
                        .setErrorCallback(mErrorCallback);
            }
        }

        public void onImageScaleTypeClicked(View view) {
            if (mSpotifyAppRemote != null) {
                mSpotifyAppRemote
                        .getPlayerApi()
                        .getPlayerState()
                        .setResultCallback(
                                playerState -> {
                                    PopupMenu menu = new PopupMenu(this, view);

                                    menu.getMenu().add(0, ImageView.ScaleType.CENTER.ordinal(), 0, "CENTER");
                                    menu.getMenu().add(1, ImageView.ScaleType.CENTER_CROP.ordinal(), 1, "CENTER_CROP");
                                    menu.getMenu()
                                            .add(2, ImageView.ScaleType.CENTER_INSIDE.ordinal(), 2, "CENTER_INSIDE");
                                    menu.getMenu().add(3, ImageView.ScaleType.MATRIX.ordinal(), 3, "MATRIX");
                                    menu.getMenu().add(4, ImageView.ScaleType.FIT_CENTER.ordinal(), 4, "FIT_CENTER");
                                    menu.getMenu().add(4, ImageView.ScaleType.FIT_XY.ordinal(), 5, "FIT_XY");

                                    menu.show();

                                    menu.setOnMenuItemClickListener(
                                            item -> {
                                                mCoverArtImageView.setScaleType(
                                                        ImageView.ScaleType.values()[item.getItemId()]);
                                                mImageScaleTypeLabel.setText(
                                                        ImageView.ScaleType.values()[item.getItemId()].toString());
                                                return false;
                                            });
                                })
                        .setErrorCallback(mErrorCallback);
            }
        }

        public void onPlayPodcastButtonClicked(View view) {
            playUri(PODCAST_URI);
        }

        public void onPlayTrackButtonClicked(View view) {
            playUri(TRACK_URI);
        }

        public void onPlayAlbumButtonClicked(View view) {
            playUri(ALBUM_URI);
        }

        public void onPlayArtistButtonClicked(View view) {
            playUri(ARTIST_URI);
        }

        public void onPlayPlaylistButtonClicked(View view) {
            playUri(PLAYLIST_URI);
        }

        private void playUri(String uri) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .play(uri)
                    .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "play")))
                    .setErrorCallback(mErrorCallback);
        }

        public void showCurrentPlayerContext(View view) {
            if (view.getTag() != null) {
                showDialog("PlayerContext", gson.toJson(view.getTag()));
            }
        }

        public void showCurrentPlayerState(View view) {
            if (view.getTag() != null) {
                showDialog("PlayerState", gson.toJson(view.getTag()));
            }
        }

        public void onToggleShuffleButtonClicked(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .toggleShuffle()
                    .setResultCallback(
                            empty -> logMessage(getString(R.string.command_feedback, "toggle shuffle")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onToggleRepeatButtonClicked(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .toggleRepeat()
                    .setResultCallback(
                            empty -> logMessage(getString(R.string.command_feedback, "toggle repeat")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onSkipPreviousButtonClicked(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .skipPrevious()
                    .setResultCallback(
                            empty -> logMessage(getString(R.string.command_feedback, "skip previous")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onPlayPauseButtonClicked(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .getPlayerState()
                    .setResultCallback(
                            playerState -> {
                                if (playerState.isPaused) {
                                    mSpotifyAppRemote
                                            .getPlayerApi()
                                            .resume()
                                            .setResultCallback(
                                                    empty -> logMessage(getString(R.string.command_feedback, "play")))
                                            .setErrorCallback(mErrorCallback);
                                } else {
                                    mSpotifyAppRemote
                                            .getPlayerApi()
                                            .pause()
                                            .setResultCallback(
                                                    empty -> logMessage(getString(R.string.command_feedback, "pause")))
                                            .setErrorCallback(mErrorCallback);
                                }
                            });
        }

        public void onSkipNextButtonClicked(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .skipNext()
                    .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "skip next")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onSeekBack(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .seekToRelativePosition(-15000)
                    .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "seek back")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onSeekForward(View view) {
            mSpotifyAppRemote
                    .getPlayerApi()
                    .seekToRelativePosition(15000)
                    .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "seek fwd")))
                    .setErrorCallback(mErrorCallback);
        }

        public void onSubscribeToCapabilitiesClicked(View view) {

            if (mCapabilitiesSubscription != null && !mCapabilitiesSubscription.isCanceled()) {
                mCapabilitiesSubscription.cancel();
                mCapabilitiesSubscription = null;
            }

            mCapabilitiesSubscription =
                    (Subscription<Capabilities>)
                            mSpotifyAppRemote
                                    .getUserApi()
                                    .subscribeToCapabilities()
                                    .setEventCallback(
                                            capabilities ->
                                                    logMessage(
                                                            getString(
                                                                    R.string.on_demand_feedback,
                                                                    capabilities.canPlayOnDemand)))
                                    .setErrorCallback(mErrorCallback);

            mSpotifyAppRemote
                    .getUserApi()
                    .getCapabilities()
                    .setResultCallback(
                            capabilities ->
                                    logMessage(
                                            getString(
                                                    R.string.on_demand_feedback,
                                                    capabilities.canPlayOnDemand)))
                    .setErrorCallback(mErrorCallback);
        }

        public void onGetCollectionStateClicked(View view) {
            mSpotifyAppRemote
                    .getUserApi()
                    .getLibraryState(TRACK_URI)
                    .setResultCallback(
                            libraryState ->
                                    showDialog(
                                            getString(R.string.command_response, getString(R.string.get_collection_state)),
                                            gson.toJson(libraryState)))
                    .setErrorCallback(this::logError);
        }

        public void onRemoveUriClicked(View view) {
            mSpotifyAppRemote
                    .getUserApi()
                    .removeFromLibrary(TRACK_URI)
                    .setResultCallback(
                            empty -> getString(R.string.command_feedback, getString(R.string.remove_uri)))
                    .setErrorCallback(this::logError);
        }

        public void onSaveUriClicked(View view) {
            mSpotifyAppRemote
                    .getUserApi()
                    .addToLibrary(TRACK_URI)
                    .setResultCallback(
                            empty -> logMessage(getString(R.string.command_feedback, getString(R.string.save_uri))))
                    .setErrorCallback(this::logError);
        }

        public void onGetFitnessRecommendedContentItemsClicked(View view) {
            mSpotifyAppRemote
                    .getContentApi()
                    .getRecommendedContentItems(ContentApi.ContentType.FITNESS)
                    .setResultCallback(
                            listItems -> {
                                final CountDownLatch latch = new CountDownLatch(listItems.items.length);
                                final List<ListItem> combined = new ArrayList<>(50);
                                for (int j = 0; j < listItems.items.length; j++) {
                                    if (listItems.items[j].playable) {
                                        combined.add(listItems.items[j]);
                                        handleLatch(latch, combined);
                                    } else {
                                        mSpotifyAppRemote
                                                .getContentApi()
                                                .getChildrenOfItem(listItems.items[j], 3, 0)
                                                .setResultCallback(
                                                        childListItems -> {
                                                            combined.addAll(Arrays.asList(childListItems.items));
                                                            handleLatch(latch, combined);
                                                        })
                                                .setErrorCallback(mErrorCallback);
                                    }
                                }
                            })
                    .setErrorCallback(mErrorCallback);
        }

        private void handleLatch(CountDownLatch latch, List<ListItem> combined) {
            latch.countDown();
            if (latch.getCount() == 0) {
                showDialog(
                        getString(R.string.command_response, getString(R.string.browse_content)),
                        gson.toJson(combined));
            }
        }

        public void onConnectSwitchToLocalClicked(View view) {
            mSpotifyAppRemote
                    .getConnectApi()
                    .connectSwitchToLocalDevice()
                    .setResultCallback(
                            empty ->
                                    logMessage(
                                            getString(
                                                    R.string.command_feedback, getString(R.string.connect_switch_to_local))))
                    .setErrorCallback(mErrorCallback);
        }

        public void onSubscribedToPlayerContextButtonClicked(View view) {
            if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
                mPlayerContextSubscription.cancel();
                mPlayerContextSubscription = null;
            }

            mPlayerContextButton.setVisibility(View.VISIBLE);
            mSubscribeToPlayerContextButton.setVisibility(View.INVISIBLE);

            mPlayerContextSubscription =
                    (Subscription<PlayerContext>)
                            mSpotifyAppRemote
                                    .getPlayerApi()
                                    .subscribeToPlayerContext()
                                    .setEventCallback(mPlayerContextEventCallback)
                                    .setErrorCallback(
                                            throwable -> {
                                                mPlayerContextButton.setVisibility(View.INVISIBLE);
                                                mSubscribeToPlayerContextButton.setVisibility(View.VISIBLE);
                                                logError(throwable);
                                            });
        }

        public void onSubscribedToPlayerStateButtonClicked(View view) {

            if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
                mPlayerStateSubscription.cancel();
                mPlayerStateSubscription = null;
            }

            mPlayerStateButton.setVisibility(View.VISIBLE);
            mSubscribeToPlayerStateButton.setVisibility(View.INVISIBLE);

            mPlayerStateSubscription =
                    (Subscription<PlayerState>)
                            mSpotifyAppRemote
                                    .getPlayerApi()
                                    .subscribeToPlayerState()
                                    .setEventCallback(mPlayerStateEventCallback)
                                    .setLifecycleCallback(
                                            new Subscription.LifecycleCallback() {
                                                @Override
                                                public void onStart() {
                                                    logMessage("Event: start");
                                                }

                                                @Override
                                                public void onStop() {
                                                    logMessage("Event: end");
                                                }
                                            })
                                    .setErrorCallback(
                                            throwable -> {
                                                mPlayerStateButton.setVisibility(View.INVISIBLE);
                                                mSubscribeToPlayerStateButton.setVisibility(View.VISIBLE);
                                                logError(throwable);
                                            });
        }

        private void logError(Throwable throwable) {
            Toast.makeText(this, R.string.err_generic_toast, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "", throwable);
        }

        private void logMessage(String msg) {
            logMessage(msg, Toast.LENGTH_SHORT);
        }

        private void logMessage(String msg, int duration) {
            Toast.makeText(this, msg, duration).show();
            Log.d(TAG, msg);
        }

        private void showDialog(String title, String message) {
            new AlertDialog.Builder(this).setTitle(title).setMessage(message).create().show();
        }

        public void onPlaybackSpeedButtonClicked(View view) {
            PopupMenu menu = new PopupMenu(this, view);

            menu.getMenu().add(50, 50, 0, "0.5x");
            menu.getMenu().add(80, 80, 1, "0.8x");
            menu.getMenu().add(100, 100, 2, "1x");
            menu.getMenu().add(120, 120, 3, "1.2x");
            menu.getMenu().add(150, 150, 4, "1.5x");
            menu.getMenu().add(200, 200, 5, "2x");
            menu.getMenu().add(300, 300, 6, "3x");

            menu.show();

            menu.setOnMenuItemClickListener(
                    item -> {
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .setPodcastPlaybackSpeed(PlaybackSpeed.PodcastPlaybackSpeed.values()[item.getOrder()])
                                .setResultCallback(
                                        empty ->
                                                logMessage(
                                                        getString(
                                                                R.string.command_feedback,
                                                                getString(R.string.play_podcast_button_label))))
                                .setErrorCallback(mErrorCallback);
                        return false;
                    });
        }

        private class TrackProgressBar {

            private static final int LOOP_DURATION = 500;
            private final SeekBar mSeekBar;
            private final Handler mHandler;

            private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            mSpotifyAppRemote
                                    .getPlayerApi()
                                    .seekTo(seekBar.getProgress())
                                    .setErrorCallback(mErrorCallback);
                        }
                    };

            private final Runnable mSeekRunnable =
                    new Runnable() {
                        @Override
                        public void run() {
                            int progress = mSeekBar.getProgress();
                            mSeekBar.setProgress(progress + LOOP_DURATION);
                            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
                        }
                    };

            private TrackProgressBar(SeekBar seekBar) {
                mSeekBar = seekBar;
                mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
                mHandler = new Handler();
            }

            private void setDuration(long duration) {
                mSeekBar.setMax((int) duration);
            }

            private void update(long progress) {
                mSeekBar.setProgress((int) progress);
            }

            private void pause() {
                mHandler.removeCallbacks(mSeekRunnable);
            }

            private void unpause() {
                mHandler.removeCallbacks(mSeekRunnable);
                mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
            }
        }
        */
}