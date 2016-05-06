package TreeBranchPackage;

public class InfoFeatureSplitScore extends FeatureSplitScore {


    public InfoFeatureSplitScore() {

    }

    public InfoFeatureSplitScore(double[] scores, int[] recordCounts) {
        super(scores, recordCounts);
    }


    @Override
    public FeatureSplitScore getFeatureSplitScore(double[] scores, int[] recordCounts) {
        return new InfoFeatureSplitScore(scores, recordCounts);
    }

    @Override
    public double calculateScore(int largeClassCount, int smallClassCount) {
        int totalCount = largeClassCount + smallClassCount;
        double probLarge = (largeClassCount == 0) ? 1 : (1.0 * largeClassCount / totalCount);
        double probSmall = (smallClassCount == 0) ? 1 : (1.0 * smallClassCount / totalCount);

        return - 1.0 * probLarge * Math.log(probLarge) / Math.log(2)
                - 1.0 * probSmall * Math.log(probSmall) / Math.log(2);
    }
}
