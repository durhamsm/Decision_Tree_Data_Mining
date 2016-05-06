package TreeBranchPackage;

import Exceptions.FeatureHasNoRangeException;
import MainPackage.Session;
import RecordPackage.Record;
import RecordPackage.RecordSet;

import java.util.List;
import java.util.stream.Collectors;

public class FeatureSplit {
    public static final int numThresholdDiv = 100;

    public int featureId;
    public double splitValue;
    public FeatureSplitScore featureSplitScore;

    public FeatureSplit(int featureId, double splitValue, FeatureSplitScore featureSplitScore) {
        this.featureId = featureId;
        this.splitValue = splitValue;
        this.featureSplitScore = featureSplitScore;
    }

    public static FeatureSplit  getBestSplitForFeature(int featureIndex, RecordSet nodeRecords) throws FeatureHasNoRangeException {

        nodeRecords.sortOnFeature(featureIndex);

        int[] classLargeCountsBelowThreshold = new int[numThresholdDiv + 2];
        int[] belowThresholdCounts = new int[numThresholdDiv + 2];
        int currentThresholdIndex = 0;

        double minFeatureValue = nodeRecords.get(0).featureValues[featureIndex];
        double featureValueRange = nodeRecords.get(nodeRecords.size() - 1).featureValues[featureIndex] - minFeatureValue;
        double lowestThreshold = minFeatureValue - 1.0 / numThresholdDiv / 2.0 * featureValueRange;
        double currentThreshold = lowestThreshold;

        if (featureValueRange == 0) {
            throw new FeatureHasNoRangeException();
        }

        for (Record record: nodeRecords) {

            while (record.featureValues[featureIndex] >= currentThreshold) {
                ++currentThresholdIndex;
                belowThresholdCounts[currentThresholdIndex] = belowThresholdCounts[currentThresholdIndex - 1];
                classLargeCountsBelowThreshold[currentThresholdIndex] = classLargeCountsBelowThreshold[currentThresholdIndex - 1];
                currentThreshold = lowestThreshold + featureValueRange * currentThresholdIndex / numThresholdDiv;
            }

            ++belowThresholdCounts[currentThresholdIndex];

            if (record.isClassLarge) {
                ++classLargeCountsBelowThreshold[currentThresholdIndex];
            }

        }

        List<FeatureSplitScore> giniScores = Session.getInstance().classificationParams.featureSplitScore.getScoresForFeatureThresholdCounts(belowThresholdCounts, classLargeCountsBelowThreshold);
        int thresholdIndexWithBestScore = getThresholdIndexOfBestScore(giniScores);

        FeatureSplit featureSplit = new FeatureSplit(featureIndex, lowestThreshold + featureValueRange * thresholdIndexWithBestScore / numThresholdDiv, giniScores.get(thresholdIndexWithBestScore));

        return featureSplit;
    }

    private static int getThresholdIndexOfBestScore(List<FeatureSplitScore> giniScores) {
        List<FeatureSplitScore> giniScoresSorted = giniScores.stream()
                .sorted((giniScore1, giniScore2) -> new Double(giniScore1.getScore()).compareTo(giniScore2.getScore()))
                .collect(Collectors.toList());
        return giniScores.indexOf(giniScoresSorted.get(0));
    }

}
