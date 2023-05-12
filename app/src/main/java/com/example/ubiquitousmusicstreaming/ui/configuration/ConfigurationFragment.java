package com.example.ubiquitousmusicstreaming.ui.configuration;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.FileSystem;
import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.Spotify;
import com.example.ubiquitousmusicstreaming.WifiReceiver;
import com.example.ubiquitousmusicstreaming.databinding.FragmentConfigurationBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.ubiquitousmusicstreaming.Settings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class ConfigurationFragment extends Fragment {

    private FragmentConfigurationBinding binding;

    MainActivity mainActivity;
    WifiReceiver wifiReceiver;
    EditText editTextRoom;
    Button buttonStartScanning, buttonStopScanning, buttonNewDataFile, buttonStoreSpeakerRoom;
    TextView textViewRoom, textViewDataFile;
    Spinner spinSpeaker, spinRoom;
    ArrayAdapter<String> adapterSpeakers, adapterRoom;
    String room, lastScanResults = "";
    String FILE_NAME = "";
    String[] locations = new String[]{};
    List<Device> devices = new ArrayList<>();
    String chosenSpeakerUniqueName = "", chosenSpeakerReadableName = "", chosenRoom = "";
    Hashtable<String, String> locationSpeakerName = new Hashtable<>();
    Hashtable<String, String> speakersNameID = new Hashtable<>();

    List<ScanResult> scanResults;
    Boolean scan = false;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Spotify spotify;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConfigurationViewModel configurationViewModel =
                new ViewModelProvider(this).get(ConfigurationViewModel.class);

        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getParentFragment().getActivity();
        mainActivity.attachConfigurationFragment(this);
        FILE_NAME = mainActivity.getFileName();
        locations = mainActivity.getLocation();
        room = mainActivity.getRoomCurrentlyScanning();


        wifiReceiver = mainActivity.getWifiReceiver();
        spotify = mainActivity.getSpotify();

        editTextRoom = binding.editTextRoom;
        buttonStartScanning = binding.btnStartScanning;
        buttonStopScanning = binding.btnStopScanning;
        buttonNewDataFile = binding.btnNewFile;
        buttonStoreSpeakerRoom = binding.btnStoreSpeakerRoom;
        textViewRoom = binding.textRoom;
        textViewDataFile = binding.textFileName;
        spinSpeaker = binding.spinnerSpeaker;
        spinRoom = binding.spinnerRoom;


        setupSpeakerRoomSelection(spinSpeaker, spinRoom);

        updateTextViewDataFile();
        updateTextViewRoom(room);

        if (mainActivity.getInUseDataCollection()) { scan = true; startScanning(); }


        buttonStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FILE_NAME != null) {
                    room = editTextRoom.getText().toString();
                    if (!room.isEmpty()) {
                        List<String> l;
                        if (locations != null) {
                            l = new ArrayList<String>(Arrays.asList(locations));
                        } else { l = new ArrayList<>(); }

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
                    }
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
                        FileSystem.createSettingFile(mainActivity);
                        Settings settings = new Settings();
                        settings.setFileName(FILE_NAME);
                        FileSystem.writeObjectToFile(mainActivity, settings);

                        mainActivity.loadSettings();
                        locations = new String[]{};
                        locationSpeakerName = new Hashtable<>();
                        speakersNameID = new Hashtable<>();
                        setupSpeakerRoomSelection(spinSpeaker, spinRoom);
                        settings.setSpeakerNameId(speakersNameID);
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

        buttonStoreSpeakerRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!chosenSpeakerUniqueName.equals("") && !chosenRoom.equals("")) {
                    locationSpeakerName.put(chosenRoom, chosenSpeakerReadableName);
                    addLocationSpeakerName(chosenRoom, chosenSpeakerReadableName);
                    mainActivity.updateLocationSpeakerName(locationSpeakerName);
                    Toast.makeText(mainActivity, "Rum: " + chosenRoom + "\nHøjtaler: " + chosenSpeakerReadableName, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mainActivity, "Vælg både en højtaler og et rum.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }

    private void addLocationToSettings(String room) {
        Settings settings = FileSystem.readObjectFromFile(mainActivity);

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
        FileSystem.writeObjectToFile(mainActivity, settings);
    }

    private void addLocationSpeakerName(String room, String speakerName) {
        Settings settings = FileSystem.readObjectFromFile(mainActivity);

        if (settings != null) {
            Hashtable<String, String> locationSpeakerName = settings.getLocationSpeakerName();
            /*
            while (locationSpeakerID.containsKey())
            {

            }
             */
            locationSpeakerName.put(room, speakerName);
            settings.setLocationSpeakerName(locationSpeakerName);
        }
        FileSystem.writeObjectToFile(mainActivity, settings);
    }

    private void addSpeakerNameId(String speakerName, String speakerId) {
        Settings settings = FileSystem.readObjectFromFile(mainActivity);

        if (settings != null) {
            Hashtable<String, String> speakerNameId = settings.getSpeakerNameId();
            /*
            while (locationSpeakerID.containsKey())
            {

            }
             */
            speakerNameId.put(speakerName, speakerId);
            settings.setSpeakerNameId(speakerNameId);
        }
        FileSystem.writeObjectToFile(mainActivity, settings);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        }
    }

    private void setupSpeakerRoomSelection(Spinner spinSpeaker, Spinner spinRoom) {
        devices = spotify.getAvailableSpeakers();
        List<String> deviceNames = new ArrayList<>();

        if (!devices.isEmpty()) {
            for (Device d : devices) {
                deviceNames.add(d.getName());
                speakersNameID.put(d.getName(), d.getId());
            }
        }

        mainActivity.setSpeakerNameId(speakersNameID);
        String[] deviceNamesArray = new String[deviceNames.size()];
        deviceNames.toArray(deviceNamesArray);

        if (deviceNamesArray != null) {
            adapterSpeakers = new ArrayAdapter<String>(mainActivity, R.layout.list_item, deviceNamesArray);
            adapterSpeakers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinSpeaker.setAdapter(adapterSpeakers);
        }

        if (locations != null) {
            adapterRoom = new ArrayAdapter<String>(mainActivity, R.layout.list_item, locations);
            adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinRoom.setAdapter(adapterRoom);
        }



        spinSpeaker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenSpeakerReadableName = adapterView.getItemAtPosition(i).toString();
                chosenSpeakerUniqueName = speakersNameID.get(chosenSpeakerReadableName);

                // int indexRoom = deviceNames.indexOf(chosenSpeakerReadableName);
                // chosenSpeakerUniqueName = devices.get(indexRoom).getId();
                System.out.println("readable name: " + chosenSpeakerReadableName);
                System.out.println("unique name: " + chosenSpeakerUniqueName);
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

    private void getAvailableSpeakers() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Noget gik galt.");
            }
            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    try {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        JsonArray devicesJson = json.getAsJsonArray("devices");

                        List<Device> tmpDevices = new ArrayList<>();

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            tmpDevices.add(device);
                        }

                        devices = tmpDevices;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
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