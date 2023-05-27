package com.example.ubiquitousmusicstreaming.FileSystem;

import com.example.ubiquitousmusicstreaming.Settings;


public interface IFileSystem {
    void createSettingFile();
    Settings loadSettings();
    void storeSettings(Settings settings);
    void makeFile(String fileName);
    void writeToFile(String data, String fileName);

}
