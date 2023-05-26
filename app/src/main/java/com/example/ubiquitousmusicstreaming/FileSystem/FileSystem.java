package com.example.ubiquitousmusicstreaming.FileSystem;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ubiquitousmusicstreaming.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSystem implements IFileSystem {
    Context context;

    public FileSystem(Context context) {
        this.context = context;
    }

    public void createSettingFile() {
        File settingFile = new File(context.getFilesDir(), "Setting");

        if (settingFile.exists()) {
            if (!settingFile.delete()) {
                Log.e(TAG, "Error deleting existing setting file");
            }
        }
        try {
            if (settingFile.createNewFile()) {
                Log.d(TAG, "Setting file created");
            } else {
                Log.d(TAG, "Setting file already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error creating setting file", e);
        }
    }

    public Settings loadSettings() {
        File settingFile = new File(context.getFilesDir(), "Setting");

        if (!settingFile.exists()) {
            return null;
            // createSettingFile();
        }
        try {
            FileInputStream fileInputStream = context.openFileInput("Setting");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Settings settings = (Settings) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            Log.d(TAG, "Object read from file");
            return settings;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error reading object from file", e);
            return null;
        }
    }

    public void storeSettings(Settings settings) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("Setting", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(settings);
            objectOutputStream.close();
            fileOutputStream.close();
            Log.d(TAG, "Object written to file");
        } catch (IOException e) {
            Log.e(TAG, "Error writing object to file", e);
        }
    }

    public void makeFile(String fileName) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, MODE_PRIVATE);
            fos.write("WIFI DATA \n\n\n".getBytes());
            Toast.makeText(context, "Fil oprettet med navn: " + fileName, Toast.LENGTH_LONG).show();
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

    public void writeToFile(String data, String fileName) {
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(fileName, MODE_APPEND);
            fos.write(data.getBytes());
            Toast.makeText(context, "Data tilføjet til: " + fileName, Toast.LENGTH_LONG).show();
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

    /*
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

     */
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
