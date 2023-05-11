package com.example.ubiquitousmusicstreaming.ui.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.databinding.FragmentLocationBinding;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private static TextView txtViewInUse, txtViewLocation;
    private static Button buttonInUse, buttonStop;
    private Boolean inUse = false, inUseTemp;
    private MainActivity mainActivity;

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

        updateTextView();

        buttonInUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = true;
                inUseTemp = true;

                mainActivity.attachLocationFragment(LocationFragment.this);
                mainActivity.setInUse(true);

                //txtViewInUse.setText("Systemet er taget brug.");
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

                //txtViewInUse.setText("Stoppet med at tracke.");
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
}