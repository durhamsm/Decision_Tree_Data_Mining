package RecordPackage;

import Exceptions.MissingEntryException;
import TreeBranchPackage.FeatureSplit;

public class Record extends RecordBase {
    //One of these sets of three lines should be uncommented in order to load the appropriate settings for the data set.

    //Columns and class indices for housing_trimmed.data
//    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
//    public static final int classIndex = 12;
//    public static final boolean classIsAlreadyDiscrete = false;

    //Columns and class indices for Columns and class indices for mammographic_masses.data
    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3, 4};
    public static final int classIndex = 5;
    public static final boolean classIsAlreadyDiscrete = true;

    //Columns and class indices for Columns and class indices for data_banknote_authentication.data
//    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3};
//    public static final int classIndex = 4;
//    public static final boolean classIsAlreadyDiscrete = true;

    //Columns and class indices for Columns and class indices for transfusion.data
//    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3};
//    public static final int classIndex = 4;
//    public static final boolean classIsAlreadyDiscrete = true;

    //Columns and class indices for Columns and class indices for airfoil_noise.data
//    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3, 4};
//    public static final int classIndex = 5;
//    public static final boolean classIsAlreadyDiscrete = false;

    //Columns and class indices for Columns and class indices for cycle_power_plant.data
//    public static final int[] featureColumnIndices = new int[]{0, 1, 2, 3};
//    public static final int classIndex = 4;
//    public static final boolean classIsAlreadyDiscrete = false;

    public double[] featureValues;
    public double classValue;


    public Record(int recordId, String[] columns) throws MissingEntryException {
        this.recordId = recordId;
        featureValues = new double[featureColumnIndices.length];

        if (columns.length <= classIndex || columns[classIndex].equalsIgnoreCase("?")) {
            throw new MissingEntryException();
        }

        classValue = Double.valueOf(columns[classIndex]);

        int featureIndex = 0;
        for (int featureColumnIndex: featureColumnIndices) {

            if (columns[featureColumnIndex].equalsIgnoreCase("?")) {
                throw new MissingEntryException();
            }

            featureValues[featureIndex++] = Double.valueOf(columns[featureColumnIndex]);
        }
    }

    public void setIsClassLarge(boolean isClassLarge) {
        this.isClassLarge = isClassLarge;
    }

    public boolean isAboveFeatureSplitThreshold(FeatureSplit featureSplit) {
        return featureValues[featureSplit.featureId] >= featureSplit.splitValue;
    }



}