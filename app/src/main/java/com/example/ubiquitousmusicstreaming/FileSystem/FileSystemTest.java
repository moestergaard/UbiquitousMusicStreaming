package com.example.ubiquitousmusicstreaming.FileSystem;

import android.content.Context;

import com.example.ubiquitousmusicstreaming.Settings;

import java.io.File;

public class FileSystemTest {
    private Context context;
    IFileSystem fileSystem;

    public FileSystemTest(Context _context) {
        context = _context;
        fileSystem = new FileSystem(context);
    }


    public void overAllTestMethod() {

        Settings settings = fileSystem.loadSettings();
        String fileName = settings.getFileName();
        System.out.println();
        System.out.println("****");
        System.out.println(fileName);
        System.out.println();
        System.out.println("****");

        boolean fileExists = TestCreate();
        System.out.println("File exists: " + fileExists);
        boolean fileExistsStill = TestCreate();
        System.out.println("File exists still: " + fileExists);

        boolean fileWriteRead = TestWriteAndRead();
        System.out.println("File write and read empty settings: " + fileWriteRead);

        boolean setFileName = TestSetFileName();
        System.out.println("Correct set file name: " + setFileName);
    }


    public boolean TestCreate() {
        File settingFile = new File(context.getFilesDir(), "Setting");
        fileSystem.createSettingFile();
        return settingFile.exists();
    }



    public boolean TestWriteAndRead() {
        Settings settings = new Settings();
        fileSystem.storeSettings(settings);
        Settings settingsRead = fileSystem.loadSettings();

        boolean settingsAreEqual = true;
        String settingFileName = settings.getFileName();
        String settingFileNameRead = settingsRead.getFileName();
        //if (!setFileName.equals(setFileNameRead)) { settingsAreEqual = false; }

        return settingsAreEqual;
    }

    public boolean TestRead() {
        return false;
    }

    public boolean TestSetFileName() {
        Settings settings = new Settings();
        settings.setFileName("TestFileName");
        fileSystem.storeSettings(settings);
        Settings settingsRead = fileSystem.loadSettings();
        boolean correctFileName = (settingsRead.getFileName().equals("TestFileName"));

        return correctFileName;
    }

    public boolean TestGetFileName() {
        return false;
    }
}
