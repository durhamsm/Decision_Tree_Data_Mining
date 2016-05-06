package MainPackage;

import TreeBranchPackage.FeatureSplitScore;
import TreeBranchPackage.GiniFeatureSplitScore;
import TreeBranchPackage.InfoFeatureSplitScore;

public class ClassificationParameters {

    //Toggle commenting for following two lines in order to use Gini Gain or Information Gain.
//    public static final FeatureSplitScore featureSplitScore = new GiniFeatureSplitScore();
    public static final FeatureSplitScore featureSplitScore = new InfoFeatureSplitScore();



    private static int m = 1;
    private static final int numValidatorPartitions = 10;

    public static double minFeatureSplitGain = 0.00;
    private static final boolean useTreeOverFittingPrevention = false;

    public int getM() {
        return m;
    }

    public static void setM(int m) {
        ClassificationParameters.m = m;
    }

    public boolean isUseTreeOverFittingPrevention() {
        return useTreeOverFittingPrevention;
    }

    public int getNumValidatorPartitions() {
        return numValidatorPartitions;
    }

    public void setMinFeatureSplitGain(double newGain) {
        minFeatureSplitGain = newGain;
    }

}
