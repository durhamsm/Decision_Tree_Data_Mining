package MainPackage;

import CrossValidation.CrossValidator;
import RecordPackage.RecordSet;
import TreeBranchPackage.GiniFeatureSplitScore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        //Change the .data file to appropriate name, in order to analyze a given data set.
        BufferedReader bufferedReader = FileReaderUtility.getBufferedReader("data/mammographic_masses.data");
        RecordSet fullRecordSet = new RecordSet(bufferedReader);

        CrossValidator crossValidator = new CrossValidator(fullRecordSet, Session.getInstance().classificationParams.getNumValidatorPartitions());

        //Get errors and accuracies and write them to "output.txt" file.
        double pessimisticError = crossValidator.getAveragePessimisticError();
        double trainingError = crossValidator.getAverageTrainingError();
        double predictionError = crossValidator.getPredictionError();
        double mdl = crossValidator.getAverageMinDescLength();
        double averageNumNodes = crossValidator.getAverageNumNodes();
        double precision = crossValidator.getPrecision();
        double recall = crossValidator.getRecall();
        double falsePositiveRate = crossValidator.getFalsePositiveRate();
        double truePositiveRate = crossValidator.getTruePositiveRate();
        double balancedSampleAccuracy = crossValidator.getBalancedSampleAccuracy();
        double simpleAccuracy = crossValidator.getSimpleAccuracy();
        double fMeasure = crossValidator.getFMeasure();

        double[] scores = new double[] {pessimisticError, trainingError, predictionError, mdl, averageNumNodes,
                precision, recall, falsePositiveRate, truePositiveRate, balancedSampleAccuracy, simpleAccuracy, fMeasure};

        printScoresToFile(scores, "output.txt");


    }

    public static void printScoresToFile(double[] values, String fileName) {

        try {
            PrintWriter out = new PrintWriter(fileName);

            for (double value: values) {
                out.println(value + "\t");
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}




















