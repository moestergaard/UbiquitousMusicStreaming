package com.example.ubiquitousmusicstreaming;
import android.content.Context;
import android.content.res.AssetManager;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import libsvm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

//import libsvm.svm_model;
//import libsvm.svm;
//import libsvm.svm_node;
//import libsvm.svm_parameter;
//import org.apache.commons.io.IOUtils;

public class DataManagementSVM {
    private svm_model model;
    private String[] distinctBSSID = new String[]{"8:d9:4d:67:05:76", "f0:87:56:19:1a:12", "f0:87:56:19:1a:11", "50:e0:39:70:e3:b1", "30:93:bc:b0:01:1c", "50:e0:39:70:e3:b2", "30:93:bc:b0:01:1d", "52:e0:39:70:e3:b2", "d8:d7:75:ed:03:9a", "e2:24:c8:36:d8:5d", "d8:d7:75:ed:03:9b", "0:b1:28:bf:ca:07", "f2:87:56:99:1a:11", "88:41:fc:c6:40:72", "88:41:fc:c6:40:2a", "a4:08:f5:1f:09:3b", "e2:24:c8:36:d8:65", "e2:24:c8:36:d8:61", "ac:3b:77:a3:46:0e", "b8:d9:4d:67:05:77", "28:9e:fc:08:d3:9a", "f4:17:b8:69:b4:5a", "8:1e:19:bd:2d:36", "6c:ba:b8:ee:06:d3", "58:90:43:54:6f:ba", "88:41:fc:c6:40:2b", "88:41:fc:c6:40:73", "3c:7c:3f:e6:1f:e8", "98:42:65:5c:d9:b6", "f4:6b:ef:d6:86:14", "b0:bb:e5:ff:d0:a6", "f4:17:b8:69:b4:5b", "88:57:1d:ed:ab:ed", "0:1e:80:78:f3:f8", "24:7f:20:01:20:57", "30:93:bc:af:88:e9", "d8:d7:75:eb:d5:e4", "00:1d:73:8e:ec:4a", "4c:19:5d:cd:58:ff", "24:7f:20:01:20:56", "00:cb:51:f0:56:43", "00:cb:51:61:ed:d0", "00:1e:80:78:f3:fc", "44:e9:dd:92:3a:e6", "0:93:bc:af:88:e8", "b8:ee:0e:46:62:33", "00:cb:51:62:a8:92", "44:e9:dd:92:3a:ea", "3c:52:82:fc:1d:cf", "dc:8d:8a:b2:b8:6e", "6a:8d:8a:b2:b8:69", "44:d4:54:6d:ef:19", "a4:08:f5:1f:09:3c", "08:3e:5d:48:fd:86", "f4:6b:ef:d7:3f:47", "0:1e:80:a4:0e:04", "fa:8f:ca:8e:04:53", "18:28:61:a6:b4:ed", "d0:6d:c9:d2:a7:f6", "62:8d:8a:b2:b8:6d", "4:49:5b:89:06:a1", "38:a0:67:da:41:7a", "6a:a0:67:da:41:7d", "6c:ba:b8:ee:06:d4", "b8:d9:4d:64:54:d3", "44:ad:b1:2c:70:7c"};
    private String samplesString = "[-84.0, -73.0, -68.0, -73.0, -65.0, -90.0, -83.0, -91.0, -75.0, -81.0, -82.0, 0.0, -72.0, 0.0, 0.0, 0.0, -86.0, -88.0, 0.0, -91.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -86.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -58.0, -56.0, -71.0, -68.0, -91.0, -86.0, -90.0, -86.0, 0.0, -76.0, 0.0, -57.0, -85.0, 0.0, -86.0, -93.0, -92.0, 0.0, -92.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -71.0, -66.0, 0.0, -70.0, -91.0, 0.0, -90.0, -69.0, 0.0, -79.0, 0.0, -71.0, 0.0, 0.0, -84.0, -87.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-80.0, -45.0, -46.0, -61.0, -42.0, -78.0, -56.0, -80.0, 0.0, -89.0, 0.0, 0.0, -44.0, -79.0, -83.0, 0.0, 0.0, 0.0, 0.0, -88.0, -89.0, 0.0, 0.0, 0.0, 0.0, -87.0, -87.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-82.0, -58.0, -47.0, -66.0, -58.0, -78.0, -72.0, -78.0, -83.0, 0.0, -79.0, 0.0, -57.0, 0.0, 0.0, 0.0, -88.0, -87.0, 0.0, -94.0, -89.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -62.0, -52.0, -64.0, -58.0, -80.0, -58.0, -80.0, 0.0, -89.0, 0.0, -34.0, 0.0, 0.0, -86.0, 0.0, 0.0, 0.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -70.0, -61.0, -77.0, -72.0, -91.0, -86.0, -93.0, -69.0, 0.0, -76.0, 0.0, -71.0, 0.0, 0.0, -78.0, -88.0, -88.0, -86.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -83.0, 0.0, 0.0, 0.0, 0.0, -91.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -79.0, 0.0, -72.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -70.0, -60.0, -75.0, -65.0, -92.0, -83.0, -92.0, -79.0, -80.0, -82.0, 0.0, -71.0, -87.0, 0.0, -85.0, -92.0, -90.0, 0.0, -86.0, -87.0, 0.0, 0.0, -86.0, 0.0, 0.0, 0.0, -86.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -87.0, 0.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-77.0, -46.0, -54.0, -56.0, -49.0, -81.0, -66.0, -80.0, -79.0, -83.0, -90.0, 0.0, -46.0, 0.0, -86.0, 0.0, 0.0, 0.0, 0.0, -93.0, -89.0, 0.0, 0.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -80.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -87.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -71.0, -62.0, -72.0, -63.0, -90.0, -84.0, -89.0, -75.0, -86.0, -83.0, 0.0, -69.0, 0.0, 0.0, 0.0, -91.0, -91.0, 0.0, -90.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0, -86.0, 0.0, 0.0, 0.0, 0.0, -93.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -47.0, -41.0, -57.0, -67.0, -74.0, -70.0, -74.0, -85.0, -86.0, -86.0, -69.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -92.0, -88.0, 0.0, 0.0, 0.0, -87.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-85.0, -58.0, -49.0, 0.0, -69.0, -81.0, -78.0, -80.0, -84.0, -88.0, -85.0, -64.0, 0.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -73.0, -61.0, -76.0, -74.0, -89.0, -86.0, -91.0, -70.0, -78.0, -76.0, 0.0, -73.0, 0.0, 0.0, -81.0, -88.0, -87.0, -86.0, 0.0, 0.0, -87.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -82.0, 0.0, -87.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -73.0, -59.0, -69.0, -67.0, -91.0, -71.0, -91.0, -83.0, -88.0, -91.0, -57.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -51.0, -47.0, -70.0, -65.0, -82.0, -72.0, -83.0, 0.0, 0.0, -93.0, 0.0, -50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-86.0, -53.0, -58.0, -65.0, -65.0, -78.0, -72.0, -78.0, -85.0, -89.0, 0.0, 0.0, -52.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -73.0, -72.0, 0.0, -64.0, -94.0, -81.0, -92.0, -73.0, -73.0, -80.0, 0.0, -74.0, 0.0, 0.0, 0.0, -87.0, -86.0, -88.0, -90.0, -89.0, 0.0, 0.0, -87.0, 0.0, 0.0, 0.0, -83.0, 0.0, 0.0, -82.0, 0.0, 0.0, 0.0, 0.0, -92.0, 0.0, -87.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -93.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -67.0, -60.0, -74.0, -73.0, -89.0, -79.0, -86.0, -87.0, -88.0, -86.0, -47.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-83.0, -74.0, -58.0, -72.0, -70.0, -92.0, -79.0, -92.0, -81.0, -84.0, -84.0, 0.0, -74.0, 0.0, 0.0, -85.0, 0.0, 0.0, 0.0, -92.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -93.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-78.0, -65.0, -55.0, -65.0, -69.0, -89.0, -80.0, -90.0, -77.0, -78.0, -84.0, 0.0, -64.0, -79.0, 0.0, -82.0, 0.0, 0.0, -80.0, -92.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -93.0, 0.0, 0.0, 0.0, 0.0, -80.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -94.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -73.0, -59.0, -68.0, -67.0, -90.0, -70.0, -92.0, -82.0, -87.0, -89.0, -57.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-85.0, -68.0, -60.0, -71.0, -66.0, -89.0, -77.0, -87.0, -71.0, 0.0, -83.0, 0.0, -67.0, 0.0, 0.0, 0.0, -89.0, -91.0, 0.0, -91.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-84.0, -67.0, -60.0, -71.0, -65.0, -89.0, -78.0, -89.0, -71.0, 0.0, -85.0, 0.0, -67.0, 0.0, 0.0, 0.0, -90.0, -90.0, 0.0, -91.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -87.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -52.0, -58.0, -63.0, -65.0, -78.0, -74.0, -78.0, -89.0, 0.0, -92.0, 0.0, -51.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-87.0, -44.0, -48.0, -63.0, -68.0, -82.0, -77.0, -80.0, -79.0, 0.0, -92.0, -55.0, 0.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, -94.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -60.0, -48.0, -62.0, -65.0, -78.0, -73.0, -77.0, -87.0, -91.0, 0.0, 0.0, -60.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-86.0, -47.0, -53.0, -69.0, -70.0, -86.0, -82.0, -85.0, -85.0, -91.0, 0.0, 0.0, -47.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -95.0, -88.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -60.0, -49.0, -61.0, -64.0, -77.0, -72.0, -77.0, -87.0, -90.0, 0.0, 0.0, -60.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -57.0, -56.0, -71.0, -69.0, -91.0, -86.0, -90.0, -84.0, 0.0, -76.0, 0.0, -58.0, -85.0, 0.0, -88.0, -91.0, -91.0, 0.0, -92.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-82.0, -72.0, -68.0, -74.0, -63.0, -90.0, -85.0, -90.0, -75.0, -81.0, -82.0, 0.0, -72.0, 0.0, 0.0, -90.0, -87.0, -87.0, 0.0, -90.0, 0.0, -85.0, 0.0, 0.0, -86.0, 0.0, 0.0, -86.0, -89.0, 0.0, -84.0, 0.0, 0.0, 0.0, 0.0, 0.0, -86.0, -89.0, 0.0, 0.0, -89.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [0.0, -69.0, -60.0, -76.0, -72.0, -92.0, -86.0, 0.0, -69.0, -84.0, -77.0, 0.0, -69.0, 0.0, 0.0, -79.0, -87.0, -89.0, -85.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -82.0, 0.0, 0.0, 0.0, 0.0, -90.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -79.0, 0.0, 0.0, 0.0, -94.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ] [-84.0, -45.0, -50.0, -62.0, -47.0, -79.0, -55.0, -79.0, 0.0, 0.0, -90.0, 0.0, -49.0, -80.0, -83.0, 0.0, 0.0, 0.0, 0.0, -85.0, -89.0, 0.0, 0.0, 0.0, 0.0, -92.0, -91.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ]";
    private String labelsString = "0.0, 2.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 2.0, 0.0, 2.0, 1.0, 1.0, 0.0, 2.0, 0.0, 0.0, 2.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 1.0, 2.0, 0.0, 0.0, 1.0,";


    private double[][] trainingSamples;
    private double[] trainingLabels;

    public DataManagementSVM() {
        trainingSamples = generateSample(samplesString);
        trainingLabels = generateLabels(labelsString);
        model = trainModel(trainingSamples, trainingLabels);

        // getModel();
        // model = loadModel("svm_model.libsvm");
        System.out.println(model);
    }

    private void getModel() {

        /*
        try {
            // Load the saved model from disk
            FileInputStream fileIn = new FileInputStream("svr_model.pkl");
            byte[] modelBytes = IOUtils.toByteArray(fileIn);
            fileIn.close();

            // Deserialize the model using pickle
            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(modelBytes));
            Object obj = objIn.readObject();
            objIn.close();

            // Cast the deserialized object to a SVR model
            SVR svr = (SVR) obj;

            // Use the SVR object to make predictions
            double[] input = {1.0, 2.0, 3.0};
            double output = svr.predict(input);

            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Load the SVM model from the asset file
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("svm_model.pkl");
            byte[] modelBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            // Convert the byte array to a Joblib object
            joblib.PythonObject svmModel = (joblib.PythonObject) joblib.load(modelBytes);

            // Use the SVM model in your Java code
            ...

        } catch (IOException e) {
            e.printStackTrace();
        }

         */
    }

    /*
    // Load a trained SVM model from a pickle file
    private svm_model loadModel(String filename) {
        try {
            /*
            Settings settings = FileSystem.readObjectFromFile(this);

            FileInputStream fileInputStream = context.openFileInput("Setting");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Settings settings = (Settings) objectInputStream.readObject();

             */

    /*
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);
            svm_model model = (svm_model) in.readObject();
            in.close();
            file.close();
            return model;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
        e.printStackTrace();
            return null;
        }
    }

     */


/*
    public static svm_model loadModel(String filePath) {
        svm_model model = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            Scanner scanner = new Scanner(content);

            // Read SVM parameters from the file
            svm_parameter param = new svm_parameter();
            param.parse(scanner.nextLine());

            // Read the support vectors from the file
            int svCount = Integer.parseInt(scanner.nextLine());
            svm_node[][] sv = new svm_node[svCount][];
            double[] svCoefs = new double[svCount];
            for (int i = 0; i < svCount; i++) {
                String[] line = scanner.nextLine().trim().split("\\s+");
                svCoefs[i] = Double.parseDouble(line[0]);
                sv[i] = new svm_node[line.length - 1];
                for (int j = 1; j < line.length; j++) {
                    String[] field = line[j].split(":");
                    svm_node node = new svm_node();
                    node.index = Integer.parseInt(field[0]);
                    node.value = Double.parseDouble(field[1]);
                    sv[i][j - 1] = node;
                }
            }

            // Create the SVM model and set its parameters
            model = new svm_model();
            model.param = param;
            model.SV = sv;
            model.sv_coef = new double[][]{svCoefs};
            model.nr_sv = new int[]{svCount};

            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return model;
    }

 */


    public int getPredictionSVM(List<ScanResult> scanResult) {
        double[] newDataPoint = getNewDataPoint(scanResult);

        double prediction = predict(newDataPoint);
        int predictionIndex = (int) prediction;

        //double[] predictedLocation = (newDataPoint);

        //String location = "test";
        return predictionIndex;
    }

    private double[] getNewDataPoint(List<ScanResult> scanResults) {
        double[] newDataPoint = new double[distinctBSSID.length];
        for(ScanResult result : scanResults) {
            for (int i = 0; i < distinctBSSID.length; i++) {
                if(distinctBSSID[i].equals(result.BSSID)) {
                    newDataPoint[i] = result.level;
                }
            }
        }
        return newDataPoint;
    }

    private double predict(double[] newDataPoint) {
        // Test the SVM model on new data
        // double[] new_data = ...; // New datapoint to classify
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i+1;
            node.value = newDataPoint[i];
            nodes[i] = node;
        }
        double prediction = svm.svm_predict(model, nodes);
        System.out.println("Prediction: " + prediction);

        return prediction;
    }

    private static svm_model trainModel(double[][] trainingSamples, double[] trainingLabels) {

        // Set SVM parameters
        svm_parameter parameters = new svm_parameter();
        parameters.kernel_type = svm_parameter.RBF;
        parameters.C = 1;
        parameters.gamma = 0.5;

        // Set class weight to balanced
        double[] classWeights = new double[2];
        classWeights[0] = 1.0;
        classWeights[1] = 1.0;
        parameters.weight = classWeights;

        // Set one-to-rest approach
        parameters.nr_weight = 2;
        parameters.weight_label = new int[]{0, 1};

        // Create SVM problem and fill it with data
        svm_problem problem = new svm_problem();
        problem.l = trainingLabels.length;
        problem.x = new svm_node[problem.l][];
        problem.y = new double[problem.l];
        for (int i = 0; i < problem.l; i++) {
            problem.x[i] = new svm_node[trainingSamples[i].length];
            for (int j = 0; j < trainingSamples[i].length; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = trainingSamples[i][j];
                problem.x[i][j] = node;
            }
            problem.y[i] = trainingLabels[i];
        }

        // Train SVM model
        svm_model model = svm.svm_train(problem, parameters);

        // Test model and return score
        //double[] predictedLabels = new double[problem.l];
        //svm.svm_predict_values(model, problem.x, predictedLabels);
        //double score = svm.svm_get_accuracy(problem.y, predictedLabels);

        return model;
    }

    /*
    private static double[][] generateSample(String input) {
        // Remove the brackets and split the string into separate arrays
        String[] arrayStrings = input.replace("[", "").replace("]", "").split("\\n");

        // Initialize the matrix to hold the arrays
        double[][] matrix = new double[arrayStrings.length][];

        // Convert each array string to a double array and add it to the matrix
        for (int i = 0; i < arrayStrings.length; i++) {
            String[] values = arrayStrings[i].trim().split("\\s+");
            double[] row = new double[values.length];
            for (int j = 0; j < values.length; j++) {
                row[j] = Double.parseDouble(values[j]);
            }
            matrix[i] = row;
        }

        return matrix;
    }

     */

    private static double[][] generateSample(String input) {
        // Remove all whitespace characters from the input string
        input = input.replaceAll("\\s", "");

        // Split the input string into separate arrays
        String[] arrayStrings = input.split("\\]\\[");

        // Initialize the matrix to hold the arrays
        double[][] matrix = new double[arrayStrings.length][];

        // Convert each array string to a double array and add it to the matrix
        for (int i = 0; i < arrayStrings.length; i++) {
            // Remove the brackets from the current array string
            String currentArrayString = arrayStrings[i].replaceAll("\\[|\\]", "");

            // Split the current array string into separate values
            String[] values = currentArrayString.split(",");

            // Convert each value to a double and add it to the current row
            double[] row = new double[values.length];
            for (int j = 0; j < values.length; j++) {
                row[j] = Double.parseDouble(values[j]);
            }

            // Add the current row to the matrix
            matrix[i] = row;
        }

        return matrix;
    }


    private static double[] generateLabels(String input) {
        String[] parts = input.split(",");
        double[] labels = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            labels[i] = Double.parseDouble(parts[i].trim());
        }
        return labels;
    }


}
