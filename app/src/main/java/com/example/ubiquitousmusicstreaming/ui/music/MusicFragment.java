package com.example.ubiquitousmusicstreaming.ui.music;

import android.annotation.SuppressLint;
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
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.databinding.FragmentMusicBinding;

public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;
    private MainActivity mainActivity;
    private View root;
    private ImageView coverImageView;
    private TextView txtViewTrackName, txtViewArtistName, txtViewPlaying, txtViewPlayingOn;
    private AppCompatImageButton playPauseButton, skipNext, skipPrevious;
    private String playingSpeaker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        assert getParentFragment() != null;
        mainActivity = (MainActivity) getParentFragment().getActivity();
        mainActivity.attachMusicFragment(this);

        setupBindings(inflater, container);
        updateTrackInformationMainActivity();

        Boolean playing = mainActivity.getPlaying();
        updatePlaying(playing);
        mainActivity.updateActiveDevice();

        playPauseButton.setOnClickListener(view -> mainActivity.changeActivationOfDevice());

        skipNext.setOnClickListener(view -> {
            mainActivity.handleRequestDevice("next");
            playPauseButton.setImageResource(R.drawable.btn_pause);
            updateTxtViews(true);
        });

        skipPrevious.setOnClickListener(view -> {
            mainActivity.handleRequestDevice("prev");
            playPauseButton.setImageResource(R.drawable.btn_pause);
            updateTxtViews(true);
        });
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    @SuppressLint("SetTextI18n")
    private void updateTxtViews(Boolean playing) {
        if (playing) {
            mainActivity.updateActiveDevice();
            if (!mainActivity.getPlayingSpeaker().equals("")) {
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

    @SuppressLint("SetTextI18n")
    public void updatePlayingSpeaker(String speakerName) {
        playingSpeaker = speakerName;
        mainActivity.runOnUiThread(() -> {
            txtViewPlaying.setText("Afspiller på");
            txtViewPlayingOn.setText(playingSpeaker);
        });
    }

    public void updateCoverImage(Bitmap coverImage) {
        coverImageView.setImageBitmap(coverImage);
        mainActivity.setCoverImage(coverImage);
    }
}