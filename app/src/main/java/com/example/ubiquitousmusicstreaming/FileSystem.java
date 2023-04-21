package com.example.ubiquitousmusicstreaming;

import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class FileSystem {

    public static Settings loadSettings(View v) {
        Settings settings = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("Settings.txt"));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            settings = (Settings) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            // Do something with the loaded settings object
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return settings;
        /*
        if (settings == null) {
            // Failed to load settings from file
            return;
        }



        FileInputStream fis = null;

        try {
            fis = mainActivity.openFileInput(fileName);
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

         */
    }
}
