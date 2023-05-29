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
    private String[] distinctBSSID;
    private String samplesString;
    private String labelsString;
    private String testSamples;
    private String testLabels;
    private double[][] trainingSamples;
    private double[] trainingLabels;
    private Context context;

    List<double[]> coefficients;
    List<Double> intercepts;
    List<Double> classes;

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

        loadModelFromFile("svm_model3.json");

        testMethod();
    }

    public double[] getPrediction(List<ScanResult> scanResult) {
        double[] newDataPoint = getNewDataPoint(scanResult);
        // double prediction = predictClass(newDataPoint);
        double prediction = predict(newDataPoint);

        return new double[]{prediction};
    }

    private void testMethod() {
        double[] arrayTestLabels = generateLabels(testLabels);
        List<double[]> arrayTestPoints = parseArrays(testSamples);
        model = loadModel(context, "svm_model4.json");

        int i = 0;
        int correct = 0;
        for (double[] newDataPoint : arrayTestPoints) {
            double result = predict(model, newDataPoint);
            // int result = (int) predictClass(newDataPoint);
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

    public static double predict(svm_model model, double[] datapoint) {
        svm_node[] nodes = new svm_node[datapoint.length];

        for (int i = 0; i < datapoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i + 1; // Index starts from 1 in LibSVM
            node.value = datapoint[i];
            nodes[i] = node;
        }

        return svm.svm_predict(model, nodes);
    }


    public double predictClass(double[] newDataPoint) {
        double maxScore = Double.NEGATIVE_INFINITY;
        double predictedClass = Double.NaN;

        for (int i = 0; i < classes.size(); i++) {
            double intercept = intercepts.get(i);
            double[] coef = coefficients.get(i);

            double score = intercept;

            for (int j = 0; j < coef.length; j++) {
                score += coef[j] * newDataPoint[j];
            }

            if (score > maxScore) {
                maxScore = score;
                predictedClass = classes.get(i);
            }
        }

        return predictedClass;
    }

    public static svm_model loadModel(Context context, String filePath) {
        svm_model model = null;
        try {
            File file = new File(context.getFilesDir(), filePath);
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            model = (svm_model) inputStream.readObject();
            inputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("SVM Model", "Error loading SVM model: " + e.getMessage());
        }
        return model;
    }


    private void loadModelFromFile(String filePath) {
        File file = new File(context.getFilesDir(), filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonDataBuilder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                jsonDataBuilder.append(line);
                line = reader.readLine();
            }

            String jsonData = jsonDataBuilder.toString();

            JSONTokener tokener = new JSONTokener(jsonData);
            JSONObject data = new JSONObject(tokener);

            JSONArray interceptsArray = data.getJSONArray("intercepts");
            intercepts = new ArrayList<>();
            for (int i = 0; i < interceptsArray.length(); i++) {
                intercepts.add(interceptsArray.getDouble(i));
            }

            JSONArray classArray = data.getJSONArray("classIndices");
            classes = new ArrayList<>();
            for (int i = 0; i < classArray.length(); i++) {
                classes.add(classArray.getDouble(i));
            }

            /*
            // Set the support vectors and coefficients of the model
            JSONArray supportVectorsJson = data.getJSONArray("support_vectors");
            supportVectors = new ArrayList<>();
            for (int i = 0; i < supportVectorsJson.length(); i++) {
                JSONArray supportVectorJson = supportVectorsJson.getJSONArray(i);
                double[] supportVector = new double[supportVectorJson.length()];
                for (int j = 0; j < supportVectorJson.length(); j++) {
                    supportVector[j] = supportVectorJson.getDouble(j);
                }
                supportVectors.add(supportVector);
            }

             */

            JSONArray coefficientsJson = data.getJSONArray("coefficients");
            coefficients = new ArrayList<>();
            for (int i = 0; i < coefficientsJson.length(); i++) {
                JSONArray coefficientJson = coefficientsJson.getJSONArray(i);
                double[] coefficient = new double[coefficientJson.length()];
                for (int j = 0; j < coefficientJson.length(); j++) {
                    coefficient[j] = coefficientJson.getDouble(j);
                }
                coefficients.add(coefficient);
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /*
    private void loadModelFromFile2(String filePath) {
        File file = new File(context.getFilesDir(), filePath);
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


            JSONArray rhoArray = data.getJSONArray("rho");
            intercepts = new double[rhoArray.length()];
            for (int i = 0; i < rhoArray.length(); i++) {
                intercepts[i] = rhoArray.getDouble(i);
            }

            // Set the support vectors and coefficients of the model
            JSONArray supportVectorsJson = data.getJSONArray("support_vectors");
            supportVectors = new double[supportVectorsJson.length()][];
            for (int i = 0; i < supportVectorsJson.length(); i++) {
                JSONArray supportVectorJson = supportVectorsJson.getJSONArray(i);
                double[] supportVector = new double[supportVectorJson.length()];
                for (int j = 0; j < supportVectorJson.length(); j++) {
                    supportVector[j] = supportVectorJson.getDouble(j);
                }
                supportVectors[i] = supportVector;
            }

            JSONArray coefficientsJson = data.getJSONArray("coefficients");
            coefficients = new double[coefficientsJson.length()][];
            for (int i = 0; i < coefficientsJson.length(); i++) {
                JSONArray coefficientJson = coefficientsJson.getJSONArray(i);
                double[] coefficient = new double[coefficientJson.length()];
                for (int j = 0; j < coefficientJson.length(); j++) {
                    coefficient[j] = coefficientJson.getDouble(j);
                }
                coefficients[i] = coefficient;
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

     */
    /*
        try (FileReader fileReader = new FileReader(filePath)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(fileReader).getAsJsonObject();

            JsonObject supportVectorsObject = jsonObject.getAsJsonObject("support_vectors");
            supportVectors = new Gson().fromJson(supportVectorsObject, double[][].class);

            JsonObject coefficientsObject = jsonObject.getAsJsonObject("coefficients");
            coefficients = new Gson().fromJson(coefficientsObject, double[][].class);

            intercepts = new Gson().fromJson(jsonObject.getAsJsonArray("rho"), double[].class);

            // Assign the loaded values to the variables
            // supportVectors, coefficients, intercepts
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

     */
    /*
    public double predictClass(double[] newDataPoint) {
        double maxScore = Double.NEGATIVE_INFINITY;
        double predictedClass = Double.NaN;

        for (int i = 0; i < classes.size(); i++) {
            double intercept = intercepts.get(i);
            double[] coefficients = this.coefficients.get(i);

            double score = intercept;

            for (int j = 0; j < supportVectors.size(); j++) {
                double[] supportVector = supportVectors.get(j);
                double distance = calculateRbfKernelDistance(supportVector, newDataPoint);
                score += coefficients[j] * distance;
            }

            if (score > maxScore) {
                maxScore = score;
                predictedClass = classes.get(i);
            }
        }

        return predictedClass;
    }

     */

    /*
    private double calculateRbfKernelDistance(double[] vector1, double[] vector2) {
        double distanceSquared = calculateEuclideanDistance(vector1, vector2);
        double distance = Math.exp(-gamma * distanceSquared);
        return distance;
    }

    private double calculateEuclideanDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector dimensions do not match");
        }

        double distanceSquared = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            distanceSquared += diff * diff;
        }

        return Math.sqrt(distanceSquared);
    }

     */

    /*
    private int predictClass2(double[] sample) {
        double maxDistance = Double.NEGATIVE_INFINITY;
        int predictedClass = -1;

        for (int classIndex = 0; classIndex < intercepts.length; classIndex++) {
            double distance = 0.0;

            for (int i = 0; i < supportVectors.length; i++) {
                double dotProduct = 0.0;
                for (int j = 0; j < supportVectors[i].length; j++) {
                    dotProduct += supportVectors[i][j] * sample[j];
                }
                distance += dotProduct * coefficients[classIndex][i];
            }

            distance += intercepts[classIndex];

            if (distance > maxDistance) {
                maxDistance = distance;
                predictedClass = classIndex;
            }
        }

        return predictedClass;
    }

     */



    private svm_model loadModel5(String filename) throws IOException, org.json.simple.parser.ParseException, JSONException {
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
