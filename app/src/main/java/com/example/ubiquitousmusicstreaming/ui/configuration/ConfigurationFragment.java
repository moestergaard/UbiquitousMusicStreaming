package com.example.ubiquitousmusicstreaming.ui.configuration;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
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

import com.example.ubiquitousmusicstreaming.MainActivity;
import com.example.ubiquitousmusicstreaming.R;
import com.example.ubiquitousmusicstreaming.WifiReceiver;
import com.example.ubiquitousmusicstreaming.databinding.FragmentConfigurationBinding;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    String[] locations = new String[]{"","Kontor", "Stue", "Køkken"};
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
                System.out.println(Arrays.asList(locations));
                List<String> l = new ArrayList<String>(Arrays.asList(locations));
                l.add(room);
                locations = l.toArray(locations);
                System.out.println(Arrays.asList(locations));
                editTextRoom.getText().clear();
                String displayStartScanning = "Scanning startet af: ";
                textViewStopScanning.setText("");
                textViewStartScanning.setText(displayStartScanning + room);

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
                FILE_NAME = LocalDateTime.now().toString() + ".txt";
                updateTextViewDataFile();
                makeFile(new View(mainActivity));
            }
        });

        buttonStoreSpeakerRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!choosenSpeaker.equals("") && !choosenRoom.equals("")) {
                    locationSpeakerID.put(choosenRoom, choosenSpeaker);
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
            save(new View(mainActivity), result);
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
                save(new View(mainActivity), resultElse);
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
                Gson gson = new Gson();
                try {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    JsonArray devicesJson = json.getAsJsonArray("devices");

                    for (JsonElement deviceJson : devicesJson) {
                        Device device = gson.fromJson(deviceJson, Device.class);
                        devices.add(device);
                    }

                    /*
                    for (Device device : devices) {
                        System.out.println(device.getName());
                    }

                     */
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //JsonEquals.jsonEquals(response.body().toString());

                /*
                response.body();
                System.out.println(response.toString());
                System.out.println("****");
                System.out.println(response.body());
                System.out.println("****");
                ObjectMapper om = new ObjectMapper();
                */

                /*
                try {
                    Speakers speakers = om.readValue((DataInput) response.body(), Speakers.class);
                } catch (IOException e) {
                    System.out.println("*** FEJL!! ***");
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
                */
                //JSONObject speakers = new JSONObject(response.body());

            }
        });

        return devices;
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public void makeFile(View v) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write("WIFI DATA \n\n\n".getBytes());

            Toast.makeText(mainActivity, "Fil oprettet med navn: " + FILE_NAME, Toast.LENGTH_LONG).show();
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

    public void save(View v, String data) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(FILE_NAME, MODE_APPEND);
            fos.write(data.getBytes());
            System.out.println(data);

            Toast.makeText(mainActivity, "Data tilføjet til: " + FILE_NAME, Toast.LENGTH_LONG).show();
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

    public void load(View v) {
        FileInputStream fis = null;

        try {
            fis = mainActivity.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            textViewStopScanning.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}