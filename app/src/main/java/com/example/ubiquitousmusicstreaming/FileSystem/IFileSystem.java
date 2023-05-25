package com.example.ubiquitousmusicstreaming.FileSystem;

import android.view.View;

import com.example.ubiquitousmusicstreaming.Settings;

import java.io.File;

public interface IFileSystem {
    File createSettingFile();
    Settings loadSettings();
    void storeSettings(Settings settings);
    void makeFile(String fileName);
    void writeToFile(String data, String fileName);

}
