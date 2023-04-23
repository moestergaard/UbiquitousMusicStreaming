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
    Button buttonStartScanning, buttonGetResultScanning, buttonNewDataFile, buttonStoreSpeakerRoom;
    TextView textViewStartScanning, textViewStopScanning, textViewDataFile;
    Spinner spinSpeaker, spinRoom;
    ArrayAdapter<String> adapterSpeakers, adapterRoom;
    String room, displayGetScanResult, lastScanResults = "";
    String FILE_NAME = "";
    String[] locations = new String[]{};//{"","Kontor", "Stue", "Køkken"};
    String choosenSpeaker = "", choosenRoom = "";
    Hashtable<String, String> locationSpeakerID = new Hashtable<>();
    List<ScanResult> scanResults;
    Boolean scan = false;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConfigurationViewModel configurationViewModel =
                new ViewModelProvider(this).get(ConfigurationViewModel.class);

        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getParentFragment().getActivity();
        mainActivity.attachConfigurationFragment(this);

        wifiReceiver = mainActivity.getWifiReceiver();

        editTextRoom = binding.editTextRoom;
        buttonStartScanning = binding.btnStartScanning;
        buttonGetResultScanning = binding.btnStopScanning;
        buttonNewDataFile = binding.btnNewFile;
        buttonStoreSpeakerRoom = binding.btnStoreSpeakerRoom;
        textViewStartScanning = binding.textRoom;
        textViewStopScanning = binding.textStopScanning;
        textViewDataFile = binding.textFileName;
        spinSpeaker = binding.spinnerSpeaker;
        spinRoom = binding.spinnerRoom;


        setupSpeakerRoomSelection(spinSpeaker, spinRoom);

        updateTextViewDataFile();

        buttonStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                room = editTextRoom.getText().toString();
                // System.out.println(Arrays.asList(locations));
                List<String> l = new ArrayList<String>(Arrays.asList(locations));
                l.add(room);
                locations = l.toArray(locations);
                //System.out.println(Arrays.asList(locations));
                editTextRoom.getText().clear();
                String displayStartScanning = "Scanning startet af: ";
                textViewStopScanning.setText("");
                textViewStartScanning.setText(displayStartScanning + room);
                addLocationToSettings(room);

                scan = true;
                startScanning();
            }
        });

        buttonGetResultScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan = false;
                String message = "Scanning stoppet for: ";
                textViewStartScanning.setText("");
                textViewStopScanning.setText(message + room);
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
                        updateTextViewDataFile();
                        //makeFileSettings(new View(mainActivity), "Settings");
                        //Settings settings = new Settings();
                        //makeSettings();
                        //saveSettings(new Settings());
                        FileSystem.createSettingFile(mainActivity);
                        FileSystem.writeObjectToFile(mainActivity, new Settings());

                        File result = new File("Settings").getAbsoluteFile();
                        System.out.println("Path name: " + result);
                        boolean canRead = new File("Settings").canRead();
                        System.out.println("File can read: " + canRead);

                        // mainActivity.loadSettings();
                        //save(new View(mainActivity), Settings.convertToString(), FILE_NAME);
                        makeFile(new View(mainActivity), FILE_NAME);
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
                if(!choosenSpeaker.equals("") && !choosenRoom.equals("")) {
                    locationSpeakerID.put(choosenRoom, choosenSpeaker);
                    addLocationSpeakerID(choosenRoom, choosenSpeaker);
                    mainActivity.updateLocationSpeakerID(locationSpeakerID);
                }
                else {
                    Toast.makeText(mainActivity, "Vælg både en højtaler og et rum.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return root;
    }

    private void addLocationToSettings(String room) {
        //Settings settings = loadSettings(new View(mainActivity));
        Settings settings = FileSystem.readObjectFromFile(mainActivity);

        /*
        String newLocation = "New Location";
        String newSpeakerID = "New Speaker ID";
        settings.getLocationSpeakerID().put(newLocation, newSpeakerID);
         */
        /*
        if (settings == null) {
            String[] locations = new String[]{};
        } else { String[] locations = settings.getLocations(); }

         */

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


        //locations = newLocations;


        //List<String> locationList
        //settings.setLocations(Arrays.copyOf(settings.getLocations(), settings.getLocations().length + 1));
        //settings.getLocations()[settings.getLocations().length - 1] = room;

        FileSystem.writeObjectToFile(mainActivity, settings);
        //saveSettings(settings);

    }

    private void addLocationSpeakerID(String room, String speakerID) {
        //Settings settings = loadSettings(new View(mainActivity));
        Settings settings = FileSystem.readObjectFromFile(mainActivity);

        /*
        if (settings == null) {
            Hashtable<String, String> locationsSpeakerID = new Hashtable<String, String>();
        } else { Hashtable<String, String> locationSpeakerID = settings.getLocationSpeakerID(); }

         */

        if (settings != null) {
            Hashtable<String, String> locationSpeakerID = settings.getLocationSpeakerID();
            locationSpeakerID.put(room, speakerID);
            settings.setLocationSpeakerID(locationSpeakerID);
        }

        FileSystem.writeObjectToFile(mainActivity, settings);
        //saveSettings(settings);
    }

    private void makeSettings() {
        File settingsFile = new File("Settings");
        settingsFile.setReadable(true);
        settingsFile.setWritable(true);
        try {
            settingsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveSettings(Settings settings) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("Settings"));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(settings);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateTextViewDataFile() {
        textViewDataFile.setText("Nuværende datafil er: " + FILE_NAME);
    }

    private void setupSpeakerRoomSelection(Spinner spinSpeaker, Spinner spinRoom) {
        List<Device> devices = getAvailableSpeakers();
        List<String> rooms = new ArrayList<>();

        for (Device d : devices) {
            rooms.add(d.getName());
        }

        String[] roomsArray = new String[rooms.size()];
        rooms.toArray(roomsArray);


        adapterSpeakers = new ArrayAdapter<String>(mainActivity, R.layout.list_item, locations);
        adapterSpeakers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSpeaker.setAdapter(adapterSpeakers);

        adapterRoom = new ArrayAdapter<String>(mainActivity, R.layout.list_item, roomsArray);
        adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSpeaker.setAdapter(adapterRoom);

        spinSpeaker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choosenSpeaker = adapterView.getItemAtPosition(i).toString();
                //Toast.makeText(mainActivity, "Højtaler: " + item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int indexRoom = rooms.indexOf(adapterView.getItemAtPosition(i).toString());
                choosenRoom = devices.get(indexRoom).getId();
                //adapterView.getItemAtPosition(i).toString();
                //Toast.makeText(mainActivity, "Højtaler: " + item, Toast.LENGTH_SHORT).show();
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
            // scanResultString += "SignalLevel: " + wifiManager.calculateSignalLevel(result.level) + "\n";
            scanResultString += "Frequency: " + result.frequency + "\n";
            scanResultString += "Timestamp: " + result.timestamp + "\n\n\n";
        }

        scanResultString += "************** \n\n";

        return scanResultString;
    }

    private void startScanning() {
        mainActivity.startScan();
    }

    private void writeUsingFiles(String data) {
        String filename = "WifiData";
        Context context = getActivity().getApplicationContext();
        try (FileOutputStream fos = context.openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(data.getBytes());
            System.out.println(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Device> getAvailableSpeakers() {
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/devices")
                .addHeader("Authorization","Bearer " + mainActivity.getAccessToken())
                .build();

        List<Device> devices = new ArrayList<>();

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

                        for (JsonElement deviceJson : devicesJson) {
                            Device device = gson.fromJson(deviceJson, Device.class);
                            devices.add(device);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return devices;
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

    public void makeFileSettings(View v, String fileName) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(fileName, MODE_PRIVATE);

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
            //System.out.println(data);

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

    public Settings loadSettings(View v) {
        Settings settings = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("Settings"));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            settings = (Settings) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            // Do something with the loaded settings object
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public void setFileName(String fileName) {
        FILE_NAME = fileName;
    }

    public void setLocations(String[] settingLocations) {
        locations = settingLocations;
    }
}