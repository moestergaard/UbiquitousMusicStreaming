package com.example.ubiquitousmusicstreaming.ui.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.Spotify;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageUri;
import java.util.Hashtable;

public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;
    private MainActivity mainActivity;
    private SpotifyAppRemote spotifyAppRemote;
    private Spotify spotify;
    private static Hashtable<String, String> locationSpeakerID = new Hashtable<>();
    ImageView coverImageView;
    TextView txtViewTrackName, txtViewArtistName;
    AppCompatImageButton playPauseButton, skipNext, skipPrevious;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MusicViewModel musicViewModel =
                new ViewModelProvider(this).get(MusicViewModel.class);

        binding = FragmentMusicBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) getParentFragment().getActivity();
        spotify = mainActivity.getSpotify();
        spotifyAppRemote = spotify.getSpotifyAppRemote();

        View root = binding.getRoot();

        coverImageView = binding.image;
        playPauseButton = binding.playPauseButton;
        skipNext = binding.skipNextButton;
        skipPrevious = binding.skipPrevButton;
        txtViewArtistName = binding.currentArtist;
        txtViewTrackName = binding.currentTrack;

        spotify.attachMusicFragment(this);

        updateTrackInformationMainActivity();

        Boolean playing = mainActivity.getPlaying();
        updatePlaying(playing);

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

    public void updatePlaying(Boolean playing) {
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
    }

    private void initializeLocationSpeakerID() {
        locationSpeakerID.put("Stue", "1af54257b625e17733f383612d7e027ad658bee2");
        locationSpeakerID.put("Kontor", "9421d826c2f75f49a95085a1063b9f74c8581cbb");
        locationSpeakerID.put("Køkken", "55ee551079c70a6e720fb7af7ed1455b050f0c37");
    }
}