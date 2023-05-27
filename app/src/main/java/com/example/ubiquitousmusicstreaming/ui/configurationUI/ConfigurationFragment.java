package com.example.ubiquitousmusicstreaming.ui.configurationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.ubiquitousmusicstreaming.Models.Device;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.databinding.FragmentConfigurationBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDateTime;
import com.example.ubiquitousmusicstreaming.Settings;

public class ConfigurationFragment extends Fragment {

    private FragmentConfigurationBinding binding;
    private MainActivity mainActivity;
    private EditText editTextRoom;
    private View root;
    private Button buttonStartScanning, buttonStopScanning, buttonNewDataFile, buttonStoreDeviceRoom;
    private TextView textViewRoom, textViewDataFile;
    private Spinner spinDevices, spinRoom;
    private String room;
    private String fileName = "";
    private String[] locations = new String[]{};
    private String chosenDeviceUniqueName = "", chosenDeviceReadableName = "", chosenRoom = "";
    private Hashtable<String, String> locationDeviceName = new Hashtable<>();
    private Hashtable<String, String> devicesNameID = new Hashtable<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setupFromMainActivity();
        setupBindings(inflater, container);
        setupDeviceRoomSelection(spinDevices, spinRoom);
        updateTextViewDataFile();
        updateButtonsScanningActive(false);
        updateTextViewRoom(room);

        buttonStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileName != null) {
                    room = editTextRoom.getText().toString();
                    if (!room.isEmpty()) {
                        List<String> l = locations != null ? new ArrayList<>(Arrays.asList(locations)) : new ArrayList<>();

                        if (!l.contains(room)) {
                            l.add(room);
                            mainActivity.addLocationToSettings(room);
                        }

                        if (locations != null) {
                            locations = l.toArray(locations);
                        } else { locations = l.toArray(new String[]{}); }

                        editTextRoom.getText().clear();
                        updateTextViewRoom(room);
                        mainActivity.setLocations(locations);
                        mainActivity.setInUseDataCollection(true);
                        mainActivity.setRoomCurrentlyScanning(room);
                        mainActivity.startScan();

                        setupDeviceRoomSelection(spinDevices, spinRoom);
                    } else { Toast.makeText(mainActivity, "Angiv rummets navn", Toast.LENGTH_LONG).show(); }
                }
                else { textViewRoom.setText("Lav en ny datafil først"); }
            }
        });

        buttonStopScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.setInUseDataCollection(false);
                mainActivity.setRoomCurrentlyScanning(null);
                String message = "Scanning stoppet for: ";
                textViewRoom.setText(message + room);
                updateButtonsScanningActive(false);
            }
        });

        buttonNewDataFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                builder.setTitle("Lav ny datafil");
                builder.setMessage("Ønsker du at lave en ny datafil til en ny model?");

                builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = LocalDateTime.now().toString() + ".txt";
                        updateTextViewDataFile();

                        mainActivity.makeFile(fileName);
                        mainActivity.initializeSettings();

                        locations = new String[]{};
                        locationDeviceName = new Hashtable<>();
                        devicesNameID = new Hashtable<>();
                        setupDeviceRoomSelection(spinDevices, spinRoom);
                    }
                });

                builder.setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        buttonStoreDeviceRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!chosenDeviceUniqueName.equals("") && !chosenRoom.equals("")) {
                    changeLocationDeviceName();
                    mainActivity.addLocationDeviceNameToSettings(locationDeviceName);
                    mainActivity.updateLocationDeviceName(locationDeviceName);
                    Toast.makeText(mainActivity, "Rum: " + chosenRoom + "\nHøjtaler: " + chosenDeviceReadableName, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mainActivity, "Vælg både en højtaler og et rum.", Toast.LENGTH_LONG).show();
                }
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
        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        editTextRoom = binding.editTextRoom;
        buttonStartScanning = binding.btnStartScanning;
        buttonStopScanning = binding.btnStopScanning;
        buttonNewDataFile = binding.btnNewFile;
        buttonStoreDeviceRoom = binding.btnStoreDeviceRoom;
        textViewRoom = binding.textRoom;
        textViewDataFile = binding.textFileName;
        spinDevices = binding.spinnerDevice;
        spinRoom = binding.spinnerRoom;
    }

    private void setupFromMainActivity() {
        mainActivity = (MainActivity) getParentFragment().getActivity();

        locations = mainActivity.getLocations();
        room = mainActivity.getRoomCurrentlyScanning();
        locationDeviceName = mainActivity.getLocationDeviceName();
        fileName = mainActivity.getFileName();
        if(fileName.equals("")) { fileName = LocalDateTime.now().toString() + ".txt"; }
        if (mainActivity.getInUseDataCollection()) { mainActivity.startScan(); }
    }

    private void updateTextViewDataFile() {
        if (!fileName.equals("")) {
            String date = fileName.split("T")[0];
            textViewDataFile.setText("Nuværende datafil er fra " + date);
        }
    }

    private void updateButtonsScanningActive(Boolean activated) {
        buttonStartScanning.setEnabled(!activated);
        buttonStopScanning.setEnabled(activated);
    }

    private void updateTextViewRoom(String room) {
        if (room != null)
        {
            String displayStartScanning = "Scanning startet af: ";
            textViewRoom.setText(displayStartScanning + room);
            updateButtonsScanningActive(true);
        }
    }

    private void setupDeviceRoomSelection(Spinner spinDevices, Spinner spinRoom) {
        List<Device> devices = mainActivity.getAvailableDevices();
        List<String> deviceNames = new ArrayList<>();

        if (!devices.isEmpty()) {
            for (Device d : devices) {
                deviceNames.add(d.getName());
                devicesNameID.put(d.getName(), d.getId());
            }
        }

        String[] deviceNamesArray = new String[deviceNames.size()];
        deviceNames.toArray(deviceNamesArray);

        ArrayAdapter<String> adapterDevices = new ArrayAdapter<String>(mainActivity, R.layout.list_item, deviceNamesArray);
        adapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDevices.setAdapter(adapterDevices);

        if (locations != null) {
            ArrayAdapter<String> adapterRoom = new ArrayAdapter<String>(mainActivity, R.layout.list_item, locations);
            adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinRoom.setAdapter(adapterRoom);
        }

        spinDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenDeviceReadableName = adapterView.getItemAtPosition(i).toString();
                chosenDeviceUniqueName = devicesNameID.get(chosenDeviceReadableName);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenRoom = adapterView.getItemAtPosition(i).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void changeLocationDeviceName() {
        boolean containsChosenDevices = locationDeviceName.containsValue(chosenDeviceReadableName);
        while (containsChosenDevices) {
            Iterator<String> iterator = locationDeviceName.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (locationDeviceName.get(key).equals(chosenDeviceReadableName)) {
                    iterator.remove();
                }
            }
            containsChosenDevices = locationDeviceName.containsValue(chosenDeviceReadableName);
        }
        locationDeviceName.put(chosenRoom, chosenDeviceReadableName);
    }
}