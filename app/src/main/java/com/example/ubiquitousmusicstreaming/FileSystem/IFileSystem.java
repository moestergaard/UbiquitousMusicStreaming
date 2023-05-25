package com.example.ubiquitousmusicstreaming.FileSystem;

import com.example.ubiquitousmusicstreaming.Settings;

import java.io.File;

public interface IFileSystem {
    File createSettingFile();
    Settings loadSettings();
    void storeSettings(Settings settings);

}
