package com.example.ubiquitousmusicstreaming;
import android.content.Context;
import android.content.res.AssetManager;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import android.net.wifi.ScanResult;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.ObjectInputStream;
import java.util.ArrayList;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import libsvm.*;

public class DataManagementSVM {
    private static svm_model model;
    private static String[] distinctBSSID;
    private String samplesString;
    private String labelsString;
    private String testSamples;
    private String testLabels;

    private double[][] trainingSamples;
    private double[] trainingLabels;
    private static Context context;

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
            model = loadModel("svm_model.json");
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


    public static svm_model loadModel(String filename) throws IOException, org.json.simple.parser.ParseException, JSONException {
        // FileInputStream fileInputStream = context.openFileInput("Setting");
        // ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        // Settings settings = (Settings) objectInputStream.readObject();
        // Load the JSON data from the file
        /*
        JSONParser parser = new JSONParser();
        File file = new File(context.getFilesDir(), filename);
        JSONObject data = (JSONObject) parser.parse(new FileReader(file));

         */

        // JSONTokener tokener = new JSONTokener(new FileReader(new File(context.getFilesDir(), filename)));
        // JSONObject data = new JSONObject(tokener);

        // File file = new File(context.getFilesDir(), filename);
        // JSONTokener tokener = new JSONTokener(new FileReader(file));
        // JSONObject data = new JSONObject(tokener);

        File file = new File(context.getFilesDir(), filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String jsonData = "";
        String line = reader.readLine();
        while (line != null) {
            jsonData += line;
            line = reader.readLine();
        }
        reader.close();

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
        // double eps = (double) data.get("eps");
        /*
        int[] classIndex = ((JSONObject) data.get("classIndex")).entrySet().stream()
                .sorted((a, b) -> Integer.compare(Integer.parseInt(a.getKey().toString()),
                        Integer.parseInt(b.getKey().toString())))
                .mapToInt(e -> Integer.parseInt(e.getValue().toString()))
                .toArray();

         */

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
        // model.rho = data.getDouble("rho");

        /*
        JSONObject jsonRho = data.getJSONObject("rho");
        double value = jsonRho.getDouble("value");
        double[] rho = new double[]{value};
        model.rho = rho;

         */

        // Set the support vectors and coefficients of the model
        // double[][] supportVectors = parseDoubleArray((String) data.get("support_vectors"));
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

        // double[][] coefficients = parseDoubleArray((String) data.get("coefficients"));

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
    }

    private static double[][] parseDoubleArray(String str) {
        String[] rows = str.split(";");
        double[][] array = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(" ");
            array[i] = new double[cols.length];
            for (int j = 0; j < cols.length; j++) {
                array[i][j] = Double.parseDouble(cols[j]);
            }
        }
        return array;
    }


    public static svm_model loadModel3(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        svm_model model = (svm_model) objectIn.readObject();
        objectIn.close();
        return model;
    }

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
        // System.out.println(newDataPoint);
        return newDataPoint;
    }

    public static int predict(double[] newDataPoint) {
        // Create an svm_node array from the input data
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i + 1;
            node.value = newDataPoint[i];
            nodes[i] = node;
            // System.out.println("node " + i + ": " + nodes[i].value);
        }

        // Predict the class using the svm_predict method of the libsvm library
        double[] probEstimates = new double[model.nr_class];
        // double[] probEstimates = new double[]{0.3333, 0.3333, 0.3333};
        // int nrClasses = model.nr_class;
        // double[] probEstimates = new double[nrClasses];
        /*
        System.out.println("model_nr_class: " + model.nr_class);
        System.out.println("nodes: " + nodes);
        System.out.println("probestimate: " + probEstimates.length);
        System.out.println("probestimate: " + probEstimates[0] + ", " + probEstimates[1] + ", " + probEstimates[2]);

         */

        // try {
        // double result = svm.svm_predict_probability(model, nodes, probEstimates);
        // System.out.println("Result: " + result);
        // System.out.println("probestimate efter: " + probEstimates[0] + ", " + probEstimates[1] + ", " + probEstimates[2]);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }


        // System.out.println("probestimate efter2: " + probEstimates[0] + ", " + probEstimates[1] + ", " + probEstimates[2]);
        // Check if model and nodes are not null
        // if (model != null && nodes != null) {
            // Make the prediction
        int predictedClass = (int) svm.svm_predict(model, nodes);
        System.out.println("predictedClass: " + predictedClass);
        // } else {
        //    System.out.println("Model or nodes is null. Cannot make prediction.");
        //}
        //int predictedClass = (int) svm.svm_predict(model, nodes);
        //System.out.println("predictedClass: " + predictedClass);
        return predictedClass;
    }


    private double predict1(double[] newDataPoint) {
        // Test the SVM model on new data
        // double[] new_data = ...; // New datapoint to classify
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i+1;
            node.value = newDataPoint[i];
            nodes[i] = node;
        }
        // System.out.println(nodes);
        double prediction = svm.svm_predict(model, nodes);
        // System.out.println("Prediction: " + prediction);

        return prediction;
    }

    private int predict4(double[] newDataPoint) {
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i+1;
            node.value = newDataPoint[i];
            nodes[i] = node;
        }
        // System.out.println(nodes);

        int[] labels = new int[3];
        svm.svm_get_labels(model, labels);
        double[] prob_estimates = new double[3];
        // System.out.println("probabiliy estimat før: " + prob_estimates[0] + ", " + prob_estimates[1] + ", " + prob_estimates[2]);
        svm.svm_predict_probability(model, nodes, prob_estimates);
        // System.out.println("probabiliy estimat efter: " + prob_estimates.toString());

        // Find the label with the highest probability and return it
        double maxProb = 0.0;
        int prediction = 0;
        for (int i = 0; i < 3; i++) {
            // System.out.println("propestimat efter: " + prob_estimates[i]);
            if (prob_estimates[i] > maxProb) {
                maxProb = prob_estimates[i];
                prediction = labels[i];
            }
        }

        // System.out.println("Prediction: " + prediction);
        return prediction;
    }

    private double predict3(double[] newDataPoint) {
        // Test the SVM model on new data
        svm_node[] nodes = new svm_node[newDataPoint.length];
        for (int i = 0; i < newDataPoint.length; i++) {
            svm_node node = new svm_node();
            node.index = i+1;
            node.value = newDataPoint[i];
            nodes[i] = node;
        }
        // System.out.println(nodes);

        int[] labels = new int[3]; // Array to store the labels
        svm.svm_get_labels(model, labels); // Get the labels from the model
        double[] prob_estimates = new double[3]; // Array to store the probabilities
        svm.svm_predict_probability(model, nodes, prob_estimates); // Predict the probabilities

        // Find the label with the highest probability and return it
        double maxProb = 0.0;
        int prediction = 0;
        for (int i = 0; i < 3; i++) {
            if (prob_estimates[i] > maxProb) {
                maxProb = prob_estimates[i];
                prediction = labels[i];
            }
        }

        // System.out.println("Prediction: " + prediction);
        return prediction;
    }


    private static svm_model trainModel(double[][] trainingSamples, double[] trainingLabels) {

        // Set SVM parameters
        svm_parameter parameters = new svm_parameter();
        parameters.kernel_type = svm_parameter.RBF;
        parameters.C = 1;
        parameters.gamma = 0.1;
        // double gamma = 1.0/(trainingLabels.length * distinctBSSID.length);
        // System.out.println("Gamma: " + gamma);
        // parameters.gamma = gamma;
        parameters.cache_size = 1000.0;

        // Set class weight to balanced
        // double[] classWeights = new double[trainingLabels.length];
        double[] classWeights = new double[3];
        Arrays.fill(classWeights, 1.0);
        parameters.weight = classWeights;

        // Set one-to-rest approach
        // parameters.nr_weight = trainingLabels.length;
        parameters.nr_weight = 3;
        parameters.weight_label = new int[]{0, 1, 2};
        // parameters.weight_label = new int[trainingLabels.length];
        // for (int i = 0; i < trainingLabels.length; i++) {
        //    parameters.weight_label[i] = (int) trainingLabels[i];
        // }

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

        return model;
    }


    private static svm_model trainModel2(double[][] trainingSamples, double[] trainingLabels) {

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

    public static List<double[]> parseArrays(String input) {
        List<double[]> arrays = new ArrayList<>();

        String[] arrayStrings = input.split("\\] \\[");
        for (String arrayString : arrayStrings) {
            double[] array = parseArray(arrayString);
            arrays.add(array);
        }

        return arrays;
    }

    private static double[] parseArray(String arrayString) {
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

    /*
    public static SMO loadModel(String filename) throws IOException, ParseException {
        // Load the JSON data from the file
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(new FileReader(new File(filename)));

        // Extract the parameters of the SMO model
        double C = (double) data.get("C");
        int kernelType = (int) data.get("kernelType");
        double gamma = (double) data.get("gamma");
        double coef0 = (double) data.get("coef0");
        int degree = (int) data.get("degree");
        int numClasses = (int) data.get("numClasses");
        int[] classIndex = ((JSONObject) data.get("classIndex")).entrySet().stream()
                .sorted((a, b) -> Integer.compare(Integer.parseInt(a.getKey().toString()),
                        Integer.parseInt(b.getKey().toString())))
                .mapToInt(e -> Integer.parseInt(e.getValue().toString()))
                .toArray();

        // Create a new SMO model with the extracted parameters
        SMO model = new SMO();
        model.setC(C);
        model.setKernelType(new SelectedTag(kernelType, SMO.TAGS_KERNELTYPE));
        model.setGamma(gamma);
        model.setCoef0(coef0);
        model.setDegree(degree);
        model.setNumClasses(numClasses);
        model.setClassIndex(classIndex);

        // Set the support vectors and coefficients of the model
        double[][] supportVectors = parseDoubleArray((String) data.get("supportVectors"));
        double[][] coefficients = parseDoubleArray((String) data.get("coefficients"));
        model.setSupportVectors(supportVectors);
        model.setCoefficients(coefficients);

        return model;
    }

    private static double[][] parseDoubleArray(String str) {
        String[] rows = str.split(";");
        double[][] array = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            array[i] = new double[cols.length];
            for (int j = 0; j < cols.length; j++) {
                array[i][j] = Double.parseDouble(cols[j]);
            }
        }
        return array;
    }

     */

}
