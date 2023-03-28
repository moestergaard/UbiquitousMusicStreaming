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
    private static TextView txtViewInUse;
    private static Button buttonInUse, buttonStop;
    private Boolean inUse = false, inUseTemp;
    private MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LocationViewModel locationViewModel =
                new ViewModelProvider(this).get(LocationViewModel.class);

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getParentFragment().getActivity();

        txtViewInUse = binding.textInUse;
        buttonInUse = binding.btnUseSystem;
        buttonStop = binding.btnStopSystem;

        buttonInUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = true;
                inUseTemp = true;

                mainActivity.attachLocationFragment(LocationFragment.this);
                mainActivity.setInUse(true);

                txtViewInUse.setText("Systemet er taget brug.");
                mainActivity.getWifiManager().startScan();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inUse = inUseTemp = false;

                mainActivity.removeLocationFragment();
                mainActivity.setInUse(false);

                txtViewInUse.setText("Stoppet med at tracke.");
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void SetTextView(String text) {
        txtViewInUse.setText(text);
    }
}