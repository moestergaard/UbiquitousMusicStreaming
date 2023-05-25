package com.example.ubiquitousmusicstreaming.ui.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ubiquitousmusicstreaming.Services.IService;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.databinding.FragmentLocationBinding;
import java.util.Hashtable;
import java.util.Objects;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private static TextView txtViewInUse, txtViewLocation, txtViewRoom, txtViewSpeaker;
    private static Button buttonInUse, buttonStop;
    private Boolean inUse = false, inUseTemp;
    private static String playingLocation = "";
    private static Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private static MainActivity mainActivity;
    private IService service;
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
                inUseTemp = true;

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
                inUse = inUseTemp = false;

                mainActivity.removeLocationFragment();
                mainActivity.setInUse(false);
                playingLocation = "";
                mainActivity.setLastLocationFragmentTextView("");

                txtViewLocation.setText("");
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
        inUse = mainActivity.getInUse();
        inUseTemp = mainActivity.getInUseTemp();
        locationSpeakerName = mainActivity.getLocationSpeakerName();
        service = mainActivity.getService();
    }

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
        String roomTextView = "";
        for (String r : rooms) {
            roomTextView += r + "\n";
        }
        txtViewRoom.setText(roomTextView);

        String[] speaker = locationSpeakerName.values().toArray(new String[0]);
        String speakerTextView = "";
        for (String s : speaker) {
            speakerTextView += s + "\n";
        }
        txtViewSpeaker.setText(speakerTextView);
    }

    public static void SetTextView(String text) { txtViewLocation.setText(text); }

    public boolean updateSpeaker(String location) {
        if (!playingLocation.equals(location)) {
            playingLocation = location;

            String speakerName = locationSpeakerName.get(location);
            if(speakerName == null) {
                Toast.makeText(mainActivity, "Vælg hvilken højtaler, der hører til " + location, Toast.LENGTH_LONG).show();
                return false;
            }
            Hashtable<String, String> speakerNameId = service.getDeviceNameId();
            String speakerId = speakerNameId.get(speakerName);

            if (speakerId == null) {
                Toast.makeText(mainActivity, "Højtaleren " + speakerName + " er ikke tilgængelig.", Toast.LENGTH_LONG).show();
                return false;
            }
            mainActivity.setPlayingSpeaker(speakerName);
            service.changeDevice(speakerId);
            return true;
        }
        return true;
    }
}