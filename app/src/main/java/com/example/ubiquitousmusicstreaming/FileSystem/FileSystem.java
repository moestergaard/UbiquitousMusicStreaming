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
        }
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = context.openFileInput("Setting");
            objectInputStream = new ObjectInputStream(fileInputStream);
            Settings settings = (Settings) objectInputStream.readObject();
            Log.d(TAG, "Object read from file");
            return settings;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error reading object from file", e);
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void storeSettings(Settings settings) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("Setting", Context.MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(settings);
            Log.d(TAG, "Object written to file");
        } catch (IOException e) {
            Log.e(TAG, "Error writing object to file", e);
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void makeFile(String fileName) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, MODE_PRIVATE);
            fileOutputStream.write("WIFI DATA \n\n\n".getBytes());
            Toast.makeText(context, "Fil oprettet med navn: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void writeToFile(String data, String fileName) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, MODE_APPEND);
            fileOutputStream.write(data.getBytes());
            Toast.makeText(context, "Data tilføjet til: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
