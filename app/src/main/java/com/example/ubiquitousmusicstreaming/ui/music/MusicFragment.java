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
import com.example.ubiquitousmusicstreaming.Spotify;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;
import com.example.ubiquitousmusicstreaming.ui.location.LocationFragment;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;
import com.spotify.protocol.types.Track;
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
import java.util.concurrent.atomic.AtomicReference;

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
    private SpotifyAppRemote spotifyAppRemote;
    private Spotify spotify;
    private static String ACCESS_TOKEN, playingLocation = "";
    private Button btnToken, btnProfile, btnSpeakers;
    private static TextView txtViewUserProfile;
    private static Hashtable<String, String> locationSpeakerID = new Hashtable<>();

    private String trackName, artistName, coverImageUrl;
    Button playerContextButton, playerStateButton;
    ImageView coverImageView;
    TextView txtViewTrackName, txtViewArtistName;
    AppCompatImageButton playPauseButton, shuffleButton, repeatButton, backButton, forwardButton, skipNext, skipPrevious;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;
    Subscription<Capabilities> mCapabilitiesSubscription;

    /*
    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                    playerContextButton.setText(
                            String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
                    playerContextButton.setTag(playerContext);
                }
            };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {

                    Drawable drawable =
                            ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.mediaservice_shuffle, mainActivity.getTheme());
                    if (!playerState.playbackOptions.isShuffling) {
                        shuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(shuffleButton.getDrawable(), Color.WHITE);
                    } else {
                        shuffleButton.setImageDrawable(drawable);
                        DrawableCompat.setTint(
                                shuffleButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    }

                    if (playerState.playbackOptions.repeatMode == Repeat.ALL) {
                        repeatButton.setImageResource(R.drawable.mediaservice_repeat_all);
                        DrawableCompat.setTint(
                                repeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else if (playerState.playbackOptions.repeatMode == Repeat.ONE) {
                        repeatButton.setImageResource(R.drawable.mediaservice_repeat_one);
                        DrawableCompat.setTint(
                                repeatButton.getDrawable(),
                                getResources().getColor(R.color.cat_medium_green));
                    } else {
                        repeatButton.setImageResource(R.drawable.mediaservice_repeat_off);
                        DrawableCompat.setTint(repeatButton.getDrawable(), Color.WHITE);
                    }

                    if (playerState.track != null) {
                        playerStateButton.setText(
                                String.format(
                                        Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name));
                        playerStateButton.setTag(playerState);
                    }

                    // Invalidate play / pause


                    if (playerState.track != null) {
                        // Get image from track
                        spotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(
                                        bitmap -> {
                                            coverImageView.setImageBitmap(bitmap);
                                        });
                    }
                }
            };

     */

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MusicViewModel musicViewModel =
                new ViewModelProvider(this).get(MusicViewModel.class);

        binding = FragmentMusicBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) getParentFragment().getActivity();
        spotify = mainActivity.getSpotify();
        //while(spotifyAppRemote == null) {
        spotifyAppRemote = spotify.getSpotifyAppRemote();
        //}

        View root = binding.getRoot();

        coverImageView = binding.image;
        //playPauseButton = mainActivity.findViewById(R.id.play_pause_button);
        playPauseButton = binding.playPauseButton;
        //shuffleButton = binding.toggleShuffleButton;
        //repeatButton = binding.toggleRepeatButton;
        //backButton = binding.seekBackButton;
        //forwardButton = binding.seekForwardButton;
        skipNext = binding.skipNextButton;
        skipPrevious = binding.skipPrevButton;
        //playerContextButton = binding.currentContextLabel;
        //playerStateButton = binding.currentTrackLabel;
        txtViewArtistName = binding.currentArtist;
        txtViewTrackName = binding.currentTrack;

        //SpotifyAppRemote.setDebugMode(true);
        onDisconnected();
        spotify.attachMusicFragment(this);

        updateTrackInformationMainActivity();
        updatePlaying();

        //subscribeToTrack();

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePlayPause();
            }
        });

        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spotifyAppRemote
                        .getPlayerApi()
                        .skipNext();
                playPauseButton.setImageResource(R.drawable.btn_pause);
                mainActivity.setPlaying(true);
            }
        });

        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spotifyAppRemote
                        .getPlayerApi()
                        .skipPrevious();
                playPauseButton.setImageResource(R.drawable.btn_pause);
                mainActivity.setPlaying(true);
            }
        });

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onDisconnected() {
        /*
        for (View view : mViews) {
            view.setEnabled(false);
        }
        coverImageView.setImageResource(R.drawable.widget_placeholder);
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

         */
    }

    private void changePlayPause() {
        spotifyAppRemote = spotify.getSpotifyAppRemote();
        spotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .resume();
                                playPauseButton.setImageResource(R.drawable.btn_pause);
                                mainActivity.setPlaying(true);
                            } else {
                                spotifyAppRemote
                                        .getPlayerApi()
                                        .pause();
                                playPauseButton.setImageResource(R.drawable.btn_play);
                                mainActivity.setPlaying(false);
                            }
                        });

    }

    /*
    private void subscribeToTrack() {
        // Subscribe to player state updates
        spotifyAppRemote = spotify.getSpotifyAppRemote();
        //System.out.println(spotifyAppRemote);
        //while(spotifyAppRemote == null) {
        spotifyAppRemote = spotify.getSpotifyAppRemote();
        //    System.out.println(spotifyAppRemote);
        //}
        spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            // Get the track object from the player state
            Track track = playerState.track;

            // Get the name of the track
            String trackName = track.name;

            // Get the artist name
            String artistName = track.artist.name;

            // Get the cover image URL
            String coverImageUrl = track.imageUri.raw;

            updateTrackInformation();
            // Do something with the updated track information
            // ...
        });

    }

     */
    private void updatePlaying() {
        Boolean playing = mainActivity.getPlaying();

        if (playing != null)
        {
            if (playing)
            {
                playPauseButton.setImageResource(R.drawable.btn_pause);
            }
            else {playPauseButton.setImageResource(R.drawable.btn_play); }
        }
    }

    private void updateTrackInformationMainActivity() {
        String trackName = mainActivity.getTrackName();
        String artistName = mainActivity.getArtistName();
        ImageUri coverImage = mainActivity.getCoverImage();
        if (trackName != null && artistName != null && coverImage != null)
        {
            updateTrackInformation(trackName, artistName, coverImage);
        }
    }

    public void updateTrackInformation(String trackName, String artistName, ImageUri coverImage) {
        txtViewArtistName.setText(artistName);
        txtViewTrackName.setText(trackName);

        spotifyAppRemote = spotify.getSpotifyAppRemote();
        spotifyAppRemote
                .getImagesApi()
                .getImage(coverImage, Image.Dimension.LARGE)
                .setResultCallback(
                        bitmap -> {
                            coverImageView.setImageBitmap(bitmap);
                        });

        mainActivity.setTrackName(trackName);
        mainActivity.setArtistName(artistName);
        mainActivity.setCoverImage(coverImage);

        /*
        playerStateButton.setText(
                String.format(
                        Locale.US, "%s\n%s", trackName, artistName));

         */
    }

    /*
    public void onSubscribeToCapabilitiesClicked(View view) {

        if (mCapabilitiesSubscription != null && !mCapabilitiesSubscription.isCanceled()) {
            mCapabilitiesSubscription.cancel();
            mCapabilitiesSubscription = null;
        }

        mCapabilitiesSubscription =
                (Subscription<Capabilities>)
                        spotifyAppRemote
                                .getUserApi()
                                .subscribeToCapabilities();


        spotifyAppRemote
                .getUserApi()
                .getCapabilities();
    }

    public void onSubscribedToPlayerContextButtonClicked(View view) {
        if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
            mPlayerContextSubscription.cancel();
            mPlayerContextSubscription = null;
        }

        playerContextButton.setVisibility(View.VISIBLE);
        playerContextButton.setVisibility(View.INVISIBLE);

        mPlayerContextSubscription =
                (Subscription<PlayerContext>)
                        spotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerContext()
                                .setEventCallback(mPlayerContextEventCallback)
                                .setErrorCallback(
                                        throwable -> {
                                            playerContextButton.setVisibility(View.INVISIBLE);
                                            playerContextButton.setVisibility(View.VISIBLE);
                                        });
    }

     */

    private void initializeLocationSpeakerID() {
        locationSpeakerID.put("Stue", "1af54257b625e17733f383612d7e027ad658bee2");
        locationSpeakerID.put("Kontor", "9421d826c2f75f49a95085a1063b9f74c8581cbb");
        locationSpeakerID.put("Køkken", "55ee551079c70a6e720fb7af7ed1455b050f0c37");
    }
}