package TreeBranchPackage;

public class GiniFeatureSplitScore extends FeatureSplitScore {

    public GiniFeatureSplitScore() {
        super();
    }

    public GiniFeatureSplitScore(double[] scores, int[] recordCounts) {
        super(scores, recordCounts);
    }

    @Override
    public FeatureSplitScore getFeatureSplitScore(double[] scores, int[] recordCounts) {
        return new GiniFeatureSplitScore(scores, recordCounts);
    }

    public double calculateScore(int largeClassCount, int smallClassCount) {
        int totalCount = smallClassCount + largeClassCount;
        return 1 - Math.pow((1.0 * largeClassCount / totalCount), 2)
                - Math.pow((1.0 * smallClassCount / totalCount), 2);
    }

}


