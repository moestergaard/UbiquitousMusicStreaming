package com.example.ubiquitousmusicstreaming.ui.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.CircularArray;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.Spotify;
import com.example.ubiquitousmusicstreaming.databinding.FragmentLocationBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private static TextView txtViewInUse, txtViewLocation;
    private static Button buttonInUse, buttonStop;
    private Boolean inUse = false, inUseTemp;
    private static String playingLocation = "";
    private static Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    private Hashtable<String, String> speakerNameId = new Hashtable<>();
    private static MainActivity mainActivity;
    private Spotify spotify;


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

        inUse = mainActivity.getInUse();
        inUseTemp = mainActivity.getInUseTemp();
        locationSpeakerName = mainActivity.getLocationSpeakerName();
        spotify = mainActivity.getSpotify();
        speakerNameId = mainActivity.getSpeakerNameId();

        updateTextView();

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
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = inUseTemp = false;

                mainActivity.removeLocationFragment();
                mainActivity.setInUse(false);

                txtViewLocation.setText("");
                updateTextView();
            }
        });
        return root;
    }

    private void updateTextView() {
        if(inUse) {
            txtViewInUse.setText("Sporing er aktiveret");
        }
        else {
            txtViewInUse.setText("Sporing er deaktiveret");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void SetTextView(String text) {
        txtViewLocation.setText(text);
    }

    public void updateSpeaker(String location) {
        if (playingLocation != location) {
            playingLocation = location;

            String speakerName = locationSpeakerName.get(location);

            Hashtable<String, String> speakerNameId = spotify.getSpeakerNameId();
            String speakerId = speakerNameId.get(speakerName);

            if (speakerId == null) {
                Toast.makeText(mainActivity, "Højtaleren " + speakerName + " er ikke tilgængelig.", Toast.LENGTH_LONG).show();
            } else { spotify.changeSpeaker(speakerId); }
        }
    }
}