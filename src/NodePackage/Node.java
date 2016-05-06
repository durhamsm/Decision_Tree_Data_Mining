package NodePackage;

import Exceptions.FeatureSplitGainBelowMinimumException;
import Exceptions.FeatureHasNoRangeException;
import Exceptions.LeafNodeException;
import MainPackage.DecisionTree;
import MainPackage.Session;
import RecordPackage.Record;
import RecordPackage.RecordSet;
import TreeBranchPackage.FeatureSplit;
import TreeBranchPackage.TreeBranch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;




public class Node {

    private int treeId;
    public List<Integer> remainingFeatureIndices = new ArrayList<>();
    public TreeBranch[] treeBranches = new TreeBranch[2];
    public RecordSet nodeRecords;
    public double nodeScore;
    public int nodeId;

    public boolean isToBeSplit = true;
    private boolean isLeafNode = true;

    private Node(int treeId) {
        this.treeId = treeId;
        DecisionTree decisionTree = Session.getInstance().decisionTrees.get(treeId);
        nodeId = decisionTree.getNextNodeId();
        decisionTree.addNode(nodeId, this);
    }

    private Node(List<Integer> recordIds, int treeId) {
        this(treeId);
        this.nodeRecords = Session.getInstance().getDecisionTreeRecordSet(treeId).getRecordSubSetList(recordIds);
    }

    private Node(List<Integer> recordIds, double nodeScore, int treeId) {
        this(recordIds, treeId);
        this.nodeScore = nodeScore;
    }

    public Node(List<Integer> recordIds, List<Integer> featureIndices, double nodeScore, int treeId) {
        this(recordIds, nodeScore, treeId);
        this.remainingFeatureIndices = featureIndices;
    }

    public Node(List<Integer> recordIds, int[] featureIndices, int treeId) {
        this(recordIds, treeId);
        for (int featureIndex: featureIndices) {
            remainingFeatureIndices.add(featureIndex);
        }
        this.nodeScore = getNodeScore();
    }

    public Node(Node nodeToCopy, int treeId) {
        this.treeId = treeId;
        this.remainingFeatureIndices = new ArrayList<>(nodeToCopy.remainingFeatureIndices);
        this.nodeRecords = nodeToCopy.nodeRecords;
        this.nodeScore = nodeToCopy.nodeScore;
        this.nodeId = nodeToCopy.nodeId;
        this.isToBeSplit = nodeToCopy.isToBeSplit;
        this.isLeafNode = nodeToCopy.isLeafNode;
        this.treeBranches = TreeBranch.getTreeBranchesCopy(nodeToCopy.treeBranches, treeId);
    }

    private double getNodeScore() {
        int countLargeClass = (int)nodeRecords.stream().filter(record -> record.isClassLarge).count();
        int countTotalRecords = nodeRecords.size();
        int countSmallClass = countTotalRecords - countLargeClass;

        return Session.getInstance().classificationParams.featureSplitScore.calculateScore(countLargeClass, countSmallClass);
    }

    public Node getNextNode(Record record) throws Exception {

        for (TreeBranch treeBranch: treeBranches) {
            if (treeBranch.doesRecordGoDownBranch(record)) {
                return treeBranch.getNode();
            }
        }

        throw new Exception("Error: No Valid branches");

    }

    public void recursiveSplit() {
        setIsToBeSplit(false);

        try {
            makeBestSplit();
            setIsLeafNode(false);
        } catch (LeafNodeException ex) {
            System.out.print(ex.getMessage());
        }

        if (!isLeafNode) {
            for (int treeBranchIndex = 0; treeBranchIndex < treeBranches.length; ++treeBranchIndex) {
                treeBranches[treeBranchIndex].getNode().recursiveSplit();
            }
        }
    }

    public List<FeatureSplit> getBestFeatureSplits() throws LeafNodeException {
        List<FeatureSplit> bestFeatureSplits = new ArrayList<>();

        for (int featureIndex: remainingFeatureIndices) {
            try {
                bestFeatureSplits.add(FeatureSplit.getBestSplitForFeature(featureIndex, nodeRecords));
            } catch (FeatureHasNoRangeException ex) {
                System.out.print(ex.getMessage());
            }
        }

        if (bestFeatureSplits.size() == 0) {
            throw new LeafNodeException("None of remaining features have non-zero range");
        }

        return bestFeatureSplits;
    }

    public void makeBestSplit() throws LeafNodeException {
        List<FeatureSplit> bestFeatureSplits;
        FeatureSplit bestFeatureSplit;


        if (isAllRecordsOfSameClass()) {
            throw new LeafNodeException("All records of same class");
        }

        bestFeatureSplits = getBestFeatureSplits();

        bestFeatureSplit = bestFeatureSplits.stream()
                .sorted((featureSplit1, featureSplit2) -> new Double(featureSplit1.featureSplitScore.getScore()).compareTo(featureSplit2.featureSplitScore.getScore()))
                .collect(Collectors.toList()).get(0);

        if (!isGainAboveMinimum(bestFeatureSplit)) {
            throw new LeafNodeException("Score gain was not above minimum");
        }

        splitNode(bestFeatureSplit);

    }

    private boolean isGainAboveMinimum(FeatureSplit featureSplit) {

        if (nodeScore - featureSplit.featureSplitScore.getScore() > Session.getInstance().classificationParams.minFeatureSplitGain) {
            return true;
        }

        return false;
    }

    public boolean getMajorityClass() {
        return nodeRecords.stream().filter(record -> record.isClassLarge).count() >= nodeRecords.size()/2;
    }

    public boolean isAllRecordsOfSameClass() {
        boolean firstRecordClass = nodeRecords.get(0).isClassLarge;
        return !nodeRecords.stream().filter(record -> record.isClassLarge != firstRecordClass).findAny().isPresent();
    }

    public void setIsLeafNode(boolean isLeafNode) {
        this.isLeafNode = isLeafNode;
    }

    public void setIsToBeSplit(boolean isToBeSplit) {
        this.isToBeSplit = isToBeSplit;
    }

    public boolean isLeafNode() {
        return isLeafNode;
    }

    public void splitNode(FeatureSplit featureSplit) throws FeatureSplitGainBelowMinimumException {
        setIsToBeSplit(false);
        setIsLeafNode(false);

        if (!isGainAboveMinimum(featureSplit)) {
            throw new FeatureSplitGainBelowMinimumException();
        }

        List<Integer> remainingFeatureIndicesForChildren = new ArrayList<Integer>(remainingFeatureIndices);
        remainingFeatureIndicesForChildren.remove(new Integer(featureSplit.featureId));

        List<Integer> recordIdsAboveThreshold = nodeRecords.stream()
                .filter(record -> record.isAboveFeatureSplitThreshold(featureSplit))
                .map(record -> record.recordId).collect(Collectors.toList());
        List<Integer> recordIdsBelowThreshold = nodeRecords.stream()
                .filter(record -> !record.isAboveFeatureSplitThreshold(featureSplit))
                .map(record -> record.recordId).collect(Collectors.toList());


        treeBranches[0] = new TreeBranch(featureSplit, false,
                new Node(recordIdsBelowThreshold, remainingFeatureIndicesForChildren, featureSplit.featureSplitScore.getNodeScore(0), treeId).nodeId, treeId);
        treeBranches[1] = new TreeBranch(featureSplit, true,
                new Node(recordIdsAboveThreshold, remainingFeatureIndicesForChildren, featureSplit.featureSplitScore.getNodeScore(1), treeId).nodeId, treeId);
    }

    public int getNumRecordsInMinorityClass() {
        int numRecords = nodeRecords.size();
        int numIsLargeClass = (int)nodeRecords.stream().filter(record -> record.isClassLarge).count();
        return numIsLargeClass < (1.0 * numRecords / 2) ? numIsLargeClass : numRecords - numIsLargeClass;
    }

//    public int getNextNodeId() {
//        MainPackage.DecisionTree decisionTree = MainPackage.Session.getInstance().decisionTrees.get(treeId);
//        if (decisionTree == null) {
//            return 0;
//        } else {
//            return decisionTree.getNextNodeId();
//        }
//    }

}
