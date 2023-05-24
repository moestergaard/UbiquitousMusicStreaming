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
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.SpotifyService;
import com.example.ubiquitousmusicstreaming.databinding.FragmentLocationBinding;

import java.util.Hashtable;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private static TextView txtViewInUse, txtViewLocation, txtViewRoom, txtViewSpeaker;
    private static Button buttonInUse, buttonStop;
    private Boolean inUse = false, inUseTemp;
    private static String playingLocation = "";
    private static Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private static MainActivity mainActivity;
    private SpotifyService spotifyService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LocationViewModel locationViewModel =
                new ViewModelProvider(this).get(LocationViewModel.class);

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setHasOptionsMenu(false);

        mainActivity = (MainActivity) getParentFragment().getActivity();

        txtViewInUse = binding.textInUse;
        txtViewLocation = binding.textLocation;
        buttonInUse = binding.btnUseSystem;
        buttonStop = binding.btnStopSystem;
        txtViewRoom = binding.textRoom;
        txtViewSpeaker = binding.textSpeaker;

        inUse = mainActivity.getInUse();
        inUseTemp = mainActivity.getInUseTemp();
        locationSpeakerName = mainActivity.getLocationSpeakerName();
        spotifyService = mainActivity.getSpotify();

        updateTextView();

        updateLocationSpeakerTextView();

        SetTextView(mainActivity.getLastLocationFragmentTextView());

        buttonInUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = true;
                inUseTemp = true;

                mainActivity.attachLocationFragment(LocationFragment.this);
                mainActivity.setInUse(true);

                mainActivity.getWifiManager().startScan();
                updateTextView();
                updateButtons(true);
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
                updateButtons(false);
            }
        });
        return root;
    }

    private void updateTextView() {
        if(inUse) {
            txtViewInUse.setText("Sporing er aktiveret");
            updateButtons(true);
        }
        else {
            txtViewInUse.setText("Sporing er deaktiveret");
            updateButtons(false);
        }
    }

    private void updateButtons(Boolean activated) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void SetTextView(String text) {
        txtViewLocation.setText(text);
    }

    public boolean updateSpeaker(String location) {
        if (playingLocation != location) {
            playingLocation = location;

            System.out.println(location);
            System.out.println(locationSpeakerName);

            String speakerName = locationSpeakerName.get(location);
            if(speakerName == null) {
                Toast.makeText(mainActivity, "Vælg hvilken højtaler, der hører til " + location, Toast.LENGTH_LONG).show();
                return false;
            }
            Hashtable<String, String> speakerNameId = spotifyService.getDeviceNameId();
            System.out.println("Dette er speakerNameId: " + speakerNameId);
            System.out.println("Dette er speakerName: " + speakerName);
            String speakerId = speakerNameId.get(speakerName);

            if (speakerId == null) {
                Toast.makeText(mainActivity, "Højtaleren " + speakerName + " er ikke tilgængelig.", Toast.LENGTH_LONG).show();
                return false;
            }
            mainActivity.setPlayingSpeaker(speakerName);
            spotifyService.changeDevice(speakerId);
            return true;
        }
        return true;
    }


}