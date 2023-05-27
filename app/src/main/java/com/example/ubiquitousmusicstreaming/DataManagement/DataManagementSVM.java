package com.example.ubiquitousmusicstreaming.DataManagement;

import android.content.Context;
import com.example.ubiquitousmusicstreaming.Models.LongStrings;
import java.io.BufferedReader;
import android.net.wifi.ScanResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import libsvm.*;
import java.io.File;
import java.io.FileReader;

import org.json.simple.parser.ParseException;

public class DataManagementSVM implements IDataManagement {
    private svm_model model;
    private String[] distinctBSSID;
    private String samplesString;
    private String labelsString;
    private String testSamples;
    private String testLabels;
    private double[][] trainingSamples;
    private double[] trainingLabels;
    private Context context;

    public DataManagementSVM(Context context) {

        this.context = context;
        LongStrings longStrings = new LongStrings();
        distinctBSSID = longStrings.getDistinctBSSID();
        samplesString = longStrings.getSamplesString();
        labelsString = longStrings.getLabelsString();
        testSamples = longStrings.getTestSamples();
        testLabels = longStrings.getTestLabels();

        trainingSamples = generateSample(samplesString);
        trainingLabels = generateLabels(labelsString);
        // model = trainModel(trainingSamples, trainingLabels);

        try {
            model = loadModel("svm_model1.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // getModel();
        // model = loadModel("svm_model.libsvm");
        // System.out.println(model);

        testMethod();
    }

    public double[] getPrediction(List<ScanResult> scanResult) {
        double[] newDataPoint = getNewDataPoint(scanResult);
        double prediction = predict(newDataPoint);

        return new double[]{prediction};
    }

    private void testMethod() {
        double[] arrayTestLabels = generateLabels(testLabels);
        List<double[]> arrayTestPoints = parseArrays(testSamples);

        int i = 0;
        int correct = 0;
        for (double[] newDataPoint : arrayTestPoints) {
            int result = predict(newDataPoint);
            if (result == (int) arrayTestLabels[i]) {correct += 1;}
            System.out.println("Result: " + result + " Correct: " + (int) arrayTestLabels[i]);
            System.out.println(newDataPoint[0]);
            System.out.println("i: " + i);
            i++;
        }

        System.out.println();
        System.out.println("Correct: " + correct);
        System.out.println("Total: " + arrayTestLabels.length);
    }


    private svm_model loadModel(String filename) throws IOException, org.json.simple.parser.ParseException, JSONException {
        File file = new File(context.getFilesDir(), filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String jsonData = "";
            String line = reader.readLine();
            while (line != null) {
                jsonData += line;
                line = reader.readLine();
            }


            JSONTokener tokener = new JSONTokener(jsonData);
            JSONObject data = new JSONObject(tokener);
            JSONObject paramsData = data.getJSONObject("params");

            System.out.println(jsonData);
            System.out.println(data);
            System.out.println(data.get("params"));


            // Extract the parameters of the SVM model
            int svmType = 0; // It is a C_SVC
            String kernelType = (String) paramsData.get("kernel");
            double gamma = (double) paramsData.get("gamma");
            double coef0 = (double) paramsData.get("coef0");
            double nu = (double) paramsData.get("svm_nu");
            int cacheSize = (int) paramsData.get("cache_size");
            JSONArray classIndexArray = data.getJSONArray("classIndices");
            int[] classIndex = new int[classIndexArray.length()];
            for (int i = 0; i < classIndexArray.length(); i++) {
                classIndex[i] = classIndexArray.getInt(i);
            }

            JSONArray nSVArray = data.getJSONArray("nSV");
            int[] nSV = new int[nSVArray.length()];
            for (int i = 0; i < nSVArray.length(); i++) {
                nSV[i] = nSVArray.getInt(i);
            }


            JSONArray rhoArray = data.getJSONArray("rho");
            double[] rho = new double[rhoArray.length()];
            for (int i = 0; i < rhoArray.length(); i++) {
                rho[i] = rhoArray.getDouble(i);
            }

            // Create a new SVM model with the extracted parameters
            svm_parameter params = new svm_parameter();
            params.svm_type = svmType;
            params.kernel_type = 2; // Corresponds to 'RBF'
            params.gamma = gamma;
            params.coef0 = coef0;
            params.nu = nu;
            params.cache_size = cacheSize;
            params.eps = 0.1;

            svm_model model = new svm_model();
            model.param = params;
            model.nr_class = classIndex.length;
            model.label = classIndex;
            model.nSV = nSV;
            model.rho = rho;

            // Set the support vectors and coefficients of the model
            JSONArray supportVectorsJson = data.getJSONArray("support_vectors");
            double[][] supportVectors = new double[supportVectorsJson.length()][];
            for (int i = 0; i < supportVectorsJson.length(); i++) {
                JSONArray supportVectorJson = supportVectorsJson.getJSONArray(i);
                double[] supportVector = new double[supportVectorJson.length()];
                for (int j = 0; j < supportVectorJson.length(); j++) {
                    supportVector[j] = supportVectorJson.getDouble(j);
                }
                supportVectors[i] = supportVector;
            }

            JSONArray coefficientsJson = data.getJSONArray("coefficients");
            double[][] coefficients = new double[coefficientsJson.length()][];
            for (int i = 0; i < coefficientsJson.length(); i++) {
                JSONArray coefficientJson = coefficientsJson.getJSONArray(i);
                double[] coefficient = new double[coefficientJson.length()];
                for (int j = 0; j < coefficientJson.length(); j++) {
                    coefficient[j] = coefficientJson.getDouble(j);
                }
                coefficients[i] = coefficient;
            }

            model.l = supportVectors.length;
            model.SV = new svm_node[model.l][];
            model.sv_coef = new double[model.nr_class - 1][model.l];

            for (int i = 0; i < supportVectors.length; i++) {
                svm_node[] nodes = new svm_node[supportVectors[i].length];
                for (int j = 0; j < supportVectors[i].length; j++) {
                    svm_node node = new svm_node();
                    node.index = j + 1;
                    node.value = supportVectors[i][j];
                    nodes[j] = node;
                }
                model.SV[i] = nodes;
            }

            for (int i = 0; i < coefficients.length; i++) {
                for (int j = 0; j < coefficients[i].length; j++) {
                    model.sv_coef[i][j] = coefficients[i][j];
                }
            }

            return model;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
        int predictedClass = (int) svm.svm_predict(model, nodes);
        return predictedClass;
    }

    private double[][] generateSample(String input) {
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
}
