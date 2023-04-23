package com.example.ubiquitousmusicstreaming;

import android.content.Context;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSystem {

    public static File createSettingFile(Context context) {
        File settingFile = new File(context.getFilesDir(), "Setting");

        if (settingFile.exists()) {
            if (!settingFile.delete()) {
                System.out.println("Problem with deleting the old Setting file.");
                //Log.e(TAG, "Error deleting existing setting file");
                return null;
            }
        }

        try {
            if (settingFile.createNewFile()) {
                System.out.println("Setting file created.");
                // Log.d(TAG, "Setting file created");
            } else {
                System.out.println("Setting file NOT created.");
                // Log.d(TAG, "Setting file already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Log.e(TAG, "Error creating setting file", e);
        }

        return settingFile;
    }

    public static Settings readObjectFromFile(Context context) {
        try {
            FileInputStream fileInputStream = context.openFileInput("Setting");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Settings settings = (Settings) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            System.out.println("Object read from file.");
            //Log.d(TAG, "Object read from file");
            return settings;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading object from file.");
            //Log.e(TAG, "Error reading object from file", e);
            return null;
        }
    }

    public static void writeObjectToFile(Context context, Settings settings) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("Setting", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(settings);
            objectOutputStream.close();
            fileOutputStream.close();
            System.out.println("Object written to file.");
            //Log.d(TAG, "Object written to file");
        } catch (IOException e) {
            System.out.println("Error writing object to file.");
            //Log.e(TAG, "Error writing object to file", e);
        }
    }

    public static Settings loadSettings(View v) {
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
