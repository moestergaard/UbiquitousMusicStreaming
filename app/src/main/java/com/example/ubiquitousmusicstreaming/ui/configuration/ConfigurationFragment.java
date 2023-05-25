package com.example.ubiquitousmusicstreaming.ui.configuration;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
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

import com.example.ubiquitousmusicstreaming.FileSystem.FileSystem;
import com.example.ubiquitousmusicstreaming.FileSystem.IFileSystem;
import com.example.ubiquitousmusicstreaming.Models.Device;
import com.example.ubiquitousmusicstreaming.Services.IService;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.WifiReceiver;
import com.example.ubiquitousmusicstreaming.databinding.FragmentConfigurationBinding;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDateTime;

import com.example.ubiquitousmusicstreaming.Settings;


public class ConfigurationFragment extends Fragment {

    private FragmentConfigurationBinding binding;
    private IFileSystem fileSystem;
    private MainActivity mainActivity;
    private WifiReceiver wifiReceiver;
    private EditText editTextRoom;
    private View root;
    private Button buttonStartScanning, buttonStopScanning, buttonNewDataFile, buttonStoreDeviceRoom;
    private TextView textViewRoom, textViewDataFile;
    private Spinner spinSpeaker, spinRoom;
    private ArrayAdapter<String> adapterDevices, adapterRoom;
    private String room, lastScanResults = "";
    private String FILE_NAME = "";
    private String[] locations = new String[]{};
    private List<Device> devices = new ArrayList<>();
    private String chosenDeviceUniqueName = "", chosenDeviceReadableName = "", chosenRoom = "";
    private Hashtable<String, String> locationDeviceName = new Hashtable<>();
    private Hashtable<String, String> devicesNameID = new Hashtable<>();
    private List<ScanResult> scanResults;
    private Boolean scan = false;
    private IService service;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setupFromMainActivity();
        setupBindings(inflater, container);
        setupSpeakerRoomSelection(spinSpeaker, spinRoom);
        updateTextViewDataFile();
        buttonStartScanning.setEnabled(true);
        buttonStopScanning.setEnabled(false);
        updateTextViewRoom(room);

        buttonStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FILE_NAME != null) {
                    room = editTextRoom.getText().toString();
                    if (!room.isEmpty()) {
                        List<String> l = locations != null ? new ArrayList<>(Arrays.asList(locations)) : new ArrayList<>();

                        if (!l.contains(room)) {
                            l.add(room);
                            addLocationToSettings(room);
                        }

                        if (locations != null) {
                            locations = l.toArray(locations);
                        } else { locations = l.toArray(new String[]{}); }

                        editTextRoom.getText().clear();
                        updateTextViewRoom(room);
                        scan = true;
                        mainActivity.setLocations(locations);
                        mainActivity.setInUseDataCollection(true);
                        mainActivity.setRoomCurrentlyScanning(room);
                        startScanning();

                        setupSpeakerRoomSelection(spinSpeaker, spinRoom);
                    } else { Toast.makeText(mainActivity, "Angiv rummets navn", Toast.LENGTH_LONG).show(); }
                }
                else { textViewRoom.setText("Lav en ny datafil først"); }
            }
        });

        buttonStopScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan = false;
                mainActivity.setInUseDataCollection(false);
                mainActivity.setRoomCurrentlyScanning(null);
                String message = "Scanning stoppet for: ";
                textViewRoom.setText(message + room);
                buttonStartScanning.setEnabled(true);
                buttonStopScanning.setEnabled(false);
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
                        FILE_NAME = LocalDateTime.now().toString() + ".txt";
                        makeFile(new View(mainActivity), FILE_NAME);
                        updateTextViewDataFile();
                        fileSystem.createSettingFile();
                        Settings settings = new Settings();
                        settings.setFileName(FILE_NAME);
                        fileSystem.storeSettings(settings);

                        mainActivity.loadSettings();
                        locations = new String[]{};
                        locationDeviceName = new Hashtable<>();
                        devicesNameID = new Hashtable<>();
                        setupSpeakerRoomSelection(spinSpeaker, spinRoom);
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
                    changeLocationSpeakerName();
                    addLocationSpeakerNameToSettings();
                    mainActivity.updateLocationSpeakerName(locationDeviceName);
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
        spinSpeaker = binding.spinnerDevice;
        spinRoom = binding.spinnerRoom;
    }

    private void setupFromMainActivity() {
        mainActivity = (MainActivity) getParentFragment().getActivity();
        mainActivity.attachConfigurationFragment(this);

        FILE_NAME = mainActivity.getFileName();
        locations = mainActivity.getLocation();
        room = mainActivity.getRoomCurrentlyScanning();
        wifiReceiver = mainActivity.getWifiReceiver();
        service = mainActivity.getService();
        locationDeviceName = mainActivity.getLocationSpeakerName();
        fileSystem = mainActivity.getFileSystem();

        if (mainActivity.getInUseDataCollection()) { scan = true; startScanning(); }
    }

    private void addLocationToSettings(String room) {
        Settings settings = fileSystem.loadSettings();

        if (settings != null) {
            String[] locations = settings.getLocations();
            String[] newLocations;
            if (locations != null) {
                newLocations = Arrays.copyOf(locations, locations.length + 1);
            } else {
                newLocations = new String[1];
            }

            newLocations[newLocations.length - 1] = room;
            settings.setLocations(newLocations);
        }
        fileSystem.storeSettings(settings);
    }

    private void addLocationSpeakerNameToSettings() {
        Settings settings = fileSystem.loadSettings();
        if (settings != null) {
            settings.setLocationSpeakerName(locationDeviceName);
        }
        fileSystem.storeSettings(settings);
    }



    private void updateTextViewDataFile() {
        if (!FILE_NAME.equals("")) {
            String date = FILE_NAME.split("T")[0];
            textViewDataFile.setText("Nuværende datafil er fra " + date);
        }
    }

    private void updateTextViewRoom(String room) {
        if (room != null)
        {
            String displayStartScanning = "Scanning startet af: ";
            textViewRoom.setText(displayStartScanning + room);
            buttonStartScanning.setEnabled(false);
            buttonStopScanning.setEnabled(true);
        }
    }

    private void setupSpeakerRoomSelection(Spinner spinSpeaker, Spinner spinRoom) {
        devices = service.getAvailableDevices();
        List<String> deviceNames = new ArrayList<>();

        if (!devices.isEmpty()) {
            for (Device d : devices) {
                deviceNames.add(d.getName());
                devicesNameID.put(d.getName(), d.getId());
            }
        }

        String[] deviceNamesArray = new String[deviceNames.size()];
        deviceNames.toArray(deviceNamesArray);

        if (deviceNamesArray != null) {
            adapterDevices = new ArrayAdapter<String>(mainActivity, R.layout.list_item, deviceNamesArray);
            adapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinSpeaker.setAdapter(adapterDevices);
        }

        if (locations != null) {
            adapterRoom = new ArrayAdapter<String>(mainActivity, R.layout.list_item, locations);
            adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinRoom.setAdapter(adapterRoom);
        }

        spinSpeaker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private void changeLocationSpeakerName() {
        Boolean containsChosenSpeaker = locationDeviceName.containsValue(chosenDeviceReadableName);
        while (containsChosenSpeaker) {
            Iterator<String> iterator = locationDeviceName.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (locationDeviceName.get(key).equals(chosenDeviceReadableName)) {
                    iterator.remove();
                }
            }
            containsChosenSpeaker = locationDeviceName.containsValue(chosenDeviceReadableName);
        }
        locationDeviceName.put(chosenRoom, chosenDeviceReadableName);
    }

    public void update() {
        scanResults = wifiReceiver.getScanResult();
        if(lastScanResults.equals("")) {
            String result = makeScanResultString(scanResults);
            lastScanResults = result;
            save(new View(mainActivity), result, FILE_NAME);
        }
        else {
            String resultElse = makeScanResultString(scanResults);
            Boolean newAndLastScanAreEqual = !lastScanResults.equals(resultElse);
            if(newAndLastScanAreEqual) {
                System.out.println("***************");
                System.out.println("lastscanresults: " + lastScanResults);
                System.out.println("elseResult: " + resultElse);
                System.out.println("--------------");
                System.out.println(lastScanResults.equals(resultElse));
                System.out.println("--------------");
                System.out.println("***************");
                save(new View(mainActivity), resultElse, FILE_NAME);
            }
            lastScanResults = resultElse;
        }

        if(scan) {
            wifiReceiver.clearScanResult();
            startScanning();
        }

    }



    private String makeScanResultString(List<ScanResult> scanResults) {

        String scanResultString = "Scanning: " + room + "\n\n";

        for (ScanResult result : scanResults) {
            scanResultString += "SSID: " + result.SSID + "\n";
            scanResultString += "BSSID: " + result.BSSID + "\n";
            scanResultString += "ResultLevel: " + result.level + "\n";
            scanResultString += "Frequency: " + result.frequency + "\n";
            scanResultString += "Timestamp: " + result.timestamp + "\n\n\n";
        }
        scanResultString += "************** \n\n";
        return scanResultString;
    }

    private void startScanning() {
        mainActivity.startScan();
    }

    public void makeFile(View v, String fileName) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(fileName, MODE_PRIVATE);
            fos.write("WIFI DATA \n\n\n".getBytes());

            Toast.makeText(mainActivity, "Fil oprettet med navn: " + fileName, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save(View v, String data, String fileName) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(fileName, MODE_APPEND);
            fos.write(data.getBytes());
            Toast.makeText(mainActivity, "Data tilføjet til: " + fileName, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}