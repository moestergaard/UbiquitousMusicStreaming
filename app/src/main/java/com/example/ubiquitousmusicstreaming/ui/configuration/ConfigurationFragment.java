package com.example.ubiquitousmusicstreaming.ui.configuration;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ubiquitousmusicstreaming.MainActivity;
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
import java.util.List;
import java.lang.Math;

public class ConfigurationFragment extends Fragment {

    private FragmentConfigurationBinding binding;

    MainActivity mainActivity;
    WifiReceiver wifiReceiver;
    EditText editTextRoom;
    Button buttonStartScanning, buttonGetResultScanning;
    TextView textViewStartScanning, textViewStopScanning;
    String room, displayGetScanResult, lastScanResults = "";
    String FILE_NAME = "WifiData230421_9-12-" + Math.random() + ".txt";
    String[] locations = new String[]{"Kontor", "Stue", "Køkken"};
    List<ScanResult> scanResults;
    Boolean scan = false;

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
        textViewStartScanning = binding.textRoom;
        textViewStopScanning = binding.textStopScanning;

        makeFile(new View(mainActivity));

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

        buttonGetResultScanning.setOnClickListener(arg0 -> {
            scan = false;
            String message = "Scanning stoppet for: ";
            textViewStartScanning.setText("");
            textViewStopScanning.setText(message + room);
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    public void makeFile(View v) {
        FileOutputStream fos = null;

        try {
            fos = mainActivity.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write("WIFI DATA \n\n\n".getBytes());

            Toast.makeText(mainActivity, "Fil oprettet i " + mainActivity.getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
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

            Toast.makeText(mainActivity, "Tilføjet til " + mainActivity.getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
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