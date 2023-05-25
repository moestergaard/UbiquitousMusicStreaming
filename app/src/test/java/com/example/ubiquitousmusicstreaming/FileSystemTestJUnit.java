package com.example.ubiquitousmusicstreaming;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.example.ubiquitousmusicstreaming.FileSystem.FileSystem;
import com.example.ubiquitousmusicstreaming.FileSystem.IFileSystem;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


public class FileSystemTestJUnit extends TestCase {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testCreateSettingFile() {
        File settingFile = new File(context.getFilesDir(), "Setting");
        IFileSystem fileSystem = new FileSystem(context);
        fileSystem.createSettingFile();
        assertTrue(settingFile.exists());
    }

    public void testReadObjectFromFile() {
    }

    public void testWriteObjectToFile() {
    }
}