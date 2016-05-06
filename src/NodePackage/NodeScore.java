package NodePackage;

public class NodeScore {

    public double score;
    public int recordCounts;

    public NodeScore(double score, int recordCounts) {
        this.score = score;
        this.recordCounts = recordCounts;
    }

    public double getWeightedScore() {
        return score * recordCounts;
    }


}
