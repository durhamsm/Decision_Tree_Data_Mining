package TreeBranchPackage;

import NodePackage.NodeScore;

import java.util.ArrayList;
import java.util.List;

public abstract class FeatureSplitScore {

    public double score = -1;
    protected NodeScore[] nodeScores;

    public FeatureSplitScore() {

    }

    abstract public FeatureSplitScore getFeatureSplitScore(double[] scores, int[] recordCounts);
    abstract public double calculateScore(int largeClassCount, int smallClassCount);

    public FeatureSplitScore(double[] giniScores, int[] recordCounts) {
        nodeScores = new NodeScore[giniScores.length];
        for (int scoreIndex = 0; scoreIndex < giniScores.length; ++scoreIndex) {
            nodeScores[scoreIndex] = new NodeScore(giniScores[scoreIndex], recordCounts[scoreIndex]);
        }
        score = getScore();
    }

    public double getScore() {
        double totalScore = 0;

        for (int scoreIndex = 0; scoreIndex < nodeScores.length; ++scoreIndex) {
            totalScore += nodeScores[scoreIndex].getWeightedScore();
        }

        return totalScore / getTotalNumRecords();
    }

    public List<FeatureSplitScore> getScoresForFeatureThresholdCounts(int[] belowThresholdCounts, int[] classLargeCountsBelowThreshold) {
        int numRecords, numRecordsWithLargeClass, classLargeCountBelowThreshold, classSmallCountBelowThreshold, aboveThresholdCount,
                classLargeCountAboveThreshold, classSmallCountAboveThreshold;
        double belowThresholdGiniScore, aboveThresholdGiniScore;

        numRecords = belowThresholdCounts[belowThresholdCounts.length - 1];
        numRecordsWithLargeClass = classLargeCountsBelowThreshold[classLargeCountsBelowThreshold.length - 1];

        List<FeatureSplitScore> featureSplitScores = new ArrayList<>();

        for (int index = 0; index < belowThresholdCounts.length; ++index) {
            classLargeCountBelowThreshold = classLargeCountsBelowThreshold[index];
            classSmallCountBelowThreshold = belowThresholdCounts[index] - classLargeCountBelowThreshold;

            aboveThresholdCount = numRecords - belowThresholdCounts[index];
            classLargeCountAboveThreshold = numRecordsWithLargeClass - classLargeCountBelowThreshold;
            classSmallCountAboveThreshold = aboveThresholdCount - classLargeCountAboveThreshold;

            belowThresholdGiniScore = calculateScore(classLargeCountBelowThreshold, classSmallCountBelowThreshold);
            aboveThresholdGiniScore = calculateScore(classLargeCountAboveThreshold, classSmallCountAboveThreshold);

            featureSplitScores.add(getFeatureSplitScore(
                    new double[]{belowThresholdGiniScore, aboveThresholdGiniScore},
                    new int[]{belowThresholdCounts[index], aboveThresholdCount}));
        }

        return featureSplitScores;
    }

    public double getNodeScore(int nodeIndex) {
        return nodeScores[nodeIndex].score;
    }

    protected int getTotalNumRecords() {
        int totalNumRecords = 0;

        for (NodeScore nodeScore : nodeScores) {
            totalNumRecords += nodeScore.recordCounts;
        }

        return totalNumRecords;

    }

}

