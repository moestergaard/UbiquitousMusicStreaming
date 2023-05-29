package com.example.ubiquitousmusicstreaming.DataManagement;

import android.content.Context;
import com.example.ubiquitousmusicstreaming.Models.LongStrings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import libsvm.*;
import java.io.File;
import java.io.FileReader;
import java.util.ListIterator;

import org.json.simple.parser.ParseException;

public class DataManagementSVM implements IDataManagement {
    private svm_model model;
    private final String[] distinctBSSID;
    private final String testSamples;
    private final String testLabels;
    private final Context context;


    public DataManagementSVM(Context context) {

        this.context = context;
        LongStrings longStrings = new LongStrings();
        distinctBSSID = longStrings.getDistinctBSSID();
        testSamples = longStrings.getTestSamples();
        testLabels = longStrings.getTestLabels();

        model = loadModel("svm_model4.json");

        testMethod();
    }

    public double[] getPrediction(List<ScanResult> scanResult) {
        double[] newDataPoint = getNewDataPoint(scanResult);

        double prediction = predict(newDataPoint);

        System.out.println("Prediction: " + prediction);

        return new double[]{prediction};
    }

    public double predict(svm_model model, double[] datapoint) {
        svm_node[] nodes = new svm_node[datapoint.length];

        for (int i = 0; i < datapoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i + 1; // Index starts from 1 in LibSVM
            node.value = datapoint[i];
            nodes[i] = node;
        }

        return svm.svm_predict(model, nodes);
    }

    public svm_model loadModel(String filePath) {
        svm_model model = null;
        try {
            File file = new File(context.getFilesDir(), filePath);
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            // ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(file.toPath()));
            model = (svm_model) inputStream.readObject();
            inputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("SVM Model", "Error loading SVM model: " + e.getMessage());
        }
        return model;
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

    private int predict(double[] newDataPoint) {
        // Create an svm_node array from the input data
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i + 1;
            node.value = newDataPoint[i];
            nodes[i] = node;
        }

        // Make the prediction
        return (int) svm.svm_predict(model, nodes);
    }

    private double[] generateLabels(String input) {
        String[] parts = input.split(",");
        double[] labels = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            labels[i] = Double.parseDouble(parts[i].trim());
        }
        return labels;
    }

    private List<double[]> parseArrays(String input) {
        List<double[]> arrays = new ArrayList<>();

        String[] arrayStrings = input.split("\\] \\[");
        for (String arrayString : arrayStrings) {
            double[] array = parseArray(arrayString);
            arrays.add(array);
        }
        return arrays;
    }

    private double[] parseArray(String arrayString) {
        String[] numberStrings = arrayString
                .replace("[", "")
                .replace("]", "")
                .split(", ");

        double[] array = new double[numberStrings.length];
        for (int i = 0; i < numberStrings.length; i++) {
            array[i] = Double.parseDouble(numberStrings[i]);
        }
        return array;
    }

    private void testMethod() {
        double[] arrayTestLabels = generateLabels(testLabels);
        List<double[]> arrayTestPoints = parseArrays(testSamples);

        int i = 0;
        int correct = 0;
        for (double[] newDataPoint : arrayTestPoints) {
            double result = predict(model, newDataPoint);
            if (result == arrayTestLabels[i]) {correct += 1;}
            System.out.println("Result: " + result + " Correct: " + arrayTestLabels[i]);
            System.out.println(newDataPoint[0]);
            System.out.println("i: " + i);
            i++;
        }

        System.out.println();
        System.out.println("Correct: " + correct);
        System.out.println("Total: " + arrayTestLabels.length);
    }
}
