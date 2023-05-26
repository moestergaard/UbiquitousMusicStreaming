package com.example.ubiquitousmusicstreaming.ui.locationUI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.databinding.FragmentLocationBinding;
import java.util.Hashtable;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private TextView txtViewInUse, txtViewLocation, txtViewRoom, txtViewSpeaker;
    private Button buttonInUse, buttonStop;
    private Boolean inUse = false;
    private String playingLocation = "";
    private Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private MainActivity mainActivity;
    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setupBindings(inflater, container);
        setupFromMainActivity();
        updateTextView();
        updateLocationSpeakerTextView();
        txtViewLocation.setText(mainActivity.getLastLocationFragmentTextView());

        buttonInUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = true;

                mainActivity.attachLocationFragment(LocationFragment.this);
                mainActivity.setInUse(true);
                mainActivity.startScan();

                updateTextView();
                updateButtonsTrackingActive(true);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = false;
                mainActivity.setInUse(false);
                playingLocation = "";
                mainActivity.setCurrentLocation(playingLocation);
                mainActivity.setLastLocationFragmentTextView("");
                txtViewLocation.setText(playingLocation);
                updateTextView();
                updateButtonsTrackingActive(false);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupBindings(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentLocationBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        txtViewInUse = binding.textInUse;
        txtViewLocation = binding.textLocation;
        buttonInUse = binding.btnUseSystem;
        buttonStop = binding.btnStopSystem;
        txtViewRoom = binding.textRoom;
        txtViewSpeaker = binding.textSpeaker;
    }

    private void setupFromMainActivity() {
        mainActivity = (MainActivity) getParentFragment().getActivity();
        inUse = mainActivity.getInUseTracking();
        locationSpeakerName = mainActivity.getLocationDeviceName();
    }

    @SuppressLint("SetTextI18n")
    private void updateTextView() {
        if(inUse) {
            txtViewInUse.setText("Sporing er aktiveret");
            updateButtonsTrackingActive(true);
        }
        else {
            txtViewInUse.setText("Sporing er deaktiveret");
            updateButtonsTrackingActive(false);
        }
    }

    private void updateButtonsTrackingActive(Boolean activated) {
        buttonInUse.setEnabled(!activated);
        buttonStop.setEnabled(activated);
    }

    private void updateLocationSpeakerTextView() {
        String[] rooms = locationSpeakerName.keySet().toArray(new String[0]);
        StringBuilder roomTextView = new StringBuilder();
        for (String r : rooms) {
            roomTextView.append(r).append("\n");
        }
        txtViewRoom.setText(roomTextView.toString());

        String[] speaker = locationSpeakerName.values().toArray(new String[0]);
        StringBuilder speakerTextView = new StringBuilder();
        for (String s : speaker) {
            speakerTextView.append(s).append("\n");
        }
        txtViewSpeaker.setText(speakerTextView.toString());
    }

    public void SetTextView(String text) { txtViewLocation.setText(text); }
}