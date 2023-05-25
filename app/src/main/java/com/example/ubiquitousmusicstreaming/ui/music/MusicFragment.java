package com.example.ubiquitousmusicstreaming.ui.music;

import android.graphics.Bitmap;
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

import com.example.ubiquitousmusicstreaming.IService;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;

public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;
    private MainActivity mainActivity;
    private IService service;
    private View root;
    ImageView coverImageView;
    TextView txtViewTrackName, txtViewArtistName, txtViewPlaying, txtViewPlayingOn;
    AppCompatImageButton playPauseButton, skipNext, skipPrevious;
    private String playingSpeaker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getParentFragment().getActivity();

        setupService();
        setupBindings(inflater, container);
        updateTrackInformationMainActivity();

        Boolean playing = mainActivity.getPlaying();
        updatePlaying(playing);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.changeActivationOfDevice();
            }
        });

        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.handleRequest("next");
                playPauseButton.setImageResource(R.drawable.btn_pause);
                mainActivity.setPlaying(true);
                updateTxtViews(true);
            }
        });

        skipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.handleRequest("prev");
                playPauseButton.setImageResource(R.drawable.btn_pause);
                mainActivity.setPlaying(true);
                updateTxtViews(true);
            }
        });
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupService() {
        service = mainActivity.getService();
        service.attachMusicFragment(this);
        service.updateActiveDevice();
    }

    private void setupBindings(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        coverImageView = binding.image;
        playPauseButton = binding.playPauseButton;
        skipNext = binding.skipNextButton;
        skipPrevious = binding.skipPrevButton;
        txtViewArtistName = binding.currentArtist;
        txtViewTrackName = binding.currentTrack;
        txtViewPlaying = binding.playing;
        txtViewPlayingOn = binding.playingOn;
    }

    private void updateTxtViews(Boolean playing) {
        if (playing) {
            service.updateActiveDevice();
            if (mainActivity.getPlayingSpeaker() != "") {
                txtViewPlaying.setText("Afspiller på");
                txtViewPlayingOn.setText(mainActivity.getPlayingSpeaker());
            }
        } else {
            txtViewPlaying.setText("");
            txtViewPlayingOn.setText("");
        }
    }

    public void updatePlaying(Boolean playing) {
        if (playing != null)
        {
            if (playing)
            {
                playPauseButton.setImageResource(R.drawable.btn_pause);
            }
            else {
                playPauseButton.setImageResource(R.drawable.btn_play);
            }
            updateTxtViews(playing);
            mainActivity.setPlaying(playing);
        }
    }

    private void updateTrackInformationMainActivity() {
        String trackName = mainActivity.getTrackName();
        String artistName = mainActivity.getArtistName();
        Bitmap coverImage = mainActivity.getCoverImage();
        if (trackName != null && artistName != null && coverImage != null)
        {
            updateTrackInformation(trackName, artistName);
            updateCoverImage(coverImage);
        }
    }

    public void updateTrackInformation(String trackName, String artistName) {
        txtViewArtistName.setText(artistName);
        txtViewTrackName.setText(trackName);

        mainActivity.setTrackName(trackName);
        mainActivity.setArtistName(artistName);
    }

    public void updatePlayingSpeaker(String speakerName) {
        playingSpeaker = speakerName;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtViewPlaying.setText("Afspiller på");
                txtViewPlayingOn.setText(playingSpeaker);
            }
        });
        mainActivity.setPlayingSpeaker(playingSpeaker);
    }

    public void updateCoverImage(Bitmap coverImage) {
        coverImageView.setImageBitmap(coverImage);
        mainActivity.setCoverImage(coverImage);
    }
}