/* package com.example.ubiquitousmusicstreaming.DataManagement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

import android.content.Context;
import android.net.wifi.ScanResult;

public class DataManagementSVM2 implements IDataManagement {

    private final String modelFilePath = "path/to/model.json";
    private String[] distinctBSSID;

    private Context context;

    public DataManagementSVM2(Context context) {
        this.context = context;
    }

    public double[] getPrediction(List<android.net.wifi.ScanResult> scanResults) {
        double[] newDataPoint = getNewDataPoint(scanResults);
        double prediction = predictClass(newDataPoint);
        return new double[0];
        // return getClassLabel(prediction);
    }

    private double[] getNewDataPoint(List<android.net.wifi.ScanResult> scanResults) {
        double[] newDataPoint = new double[distinctBSSID.length];
        for (android.net.wifi.ScanResult result : scanResults) {
            for (int i = 0; i < distinctBSSID.length; i++) {
                if (distinctBSSID[i].equals(result.BSSID)) {
                    newDataPoint[i] = result.level;
                }
            }
        }
        return newDataPoint;
    }

    private double predictClass(double[] newDataPoint) {
        double prediction = 0.0;
        try {
            String modelJson = readModelFile();
            Gson gson = new Gson();
            JsonObject modelData = JsonParser.parseString(modelJson).getAsJsonObject();

            // Load model parameters
            JsonObject params = modelData.getAsJsonObject("params");
            double C = params.get("C").getAsDouble();
            String kernel = params.get("kernel").getAsString();
            double gamma = params.get("gamma").getAsDouble();
            double coef0 = params.get("coef0").getAsDouble();
            int degree = params.get("degree").getAsInt();
            String classWeight = params.get("class_weight").getAsString();
            String decisionFunctionShape = params.get("decision_function_shape").getAsString();
            double tol = params.get("tol").getAsDouble();
            int maxIter = params.get("max_iter").getAsInt();
            long randomState = params.get("random_state").getAsLong();
            int verbose = params.get("verbose").getAsInt();
            double cacheSize = params.get("cache_size").getAsDouble();
            double nu = params.get("svm_nu").getAsDouble();

            // Load support vectors and coefficients
            double[][] supportVectors = gson.fromJson(modelData.getAsJsonArray("support_vectors"), double[][].class);
            double[][] coefficients = gson.fromJson(modelData.getAsJsonArray("coefficients"), double[][].class);
            int[] classIndices = gson.fromJson(modelData.getAsJsonArray("classIndices"), int[].class);
            int[] nSV = gson.fromJson(modelData.getAsJsonArray("nSV"), int[].class);
            double[] rho = gson.fromJson(modelData.getAsJsonArray("rho"), double[].class);

            // Perform prediction using the loaded model
            // Add your prediction logic here

        } catch (IOException e) {
            e.printStackTrace();
        }
        return prediction;
    }

    private String readModelFile() throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(modelFilePath));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }

    private String getClassLabel(double prediction) {
        // Add your logic to map the prediction value to the corresponding class label
        // Return the predicted class label
        return null;
    }

    private class ScanResult {
        String BSSID;
        int level;

        public ScanResult(String BSSID, int level) {
            this.BSSID = BSSID;
            this.level = level;
        }
    }

}

 */
