package TreeBranchPackage;

import CrossValidation.CrossValidator;
import MainPackage.Session;
import NodePackage.Node;
import RecordPackage.Record;
import sun.reflect.generics.tree.Tree;

public class TreeBranch {

    public FeatureSplit featureSplit;
    public boolean isBranchValuesAboveThreshold;

    public int nodeId;
    public int treeId;


    public TreeBranch(FeatureSplit featureSplit, boolean isBranchValuesAboveThreshold, int nodeId, int treeId) {
        this.featureSplit = featureSplit;
        this.isBranchValuesAboveThreshold = isBranchValuesAboveThreshold;
        this.nodeId = nodeId;
        this.treeId = treeId;
    }



    public TreeBranch(TreeBranch treeBranch, int treeId) {
        this(treeBranch.featureSplit, treeBranch.isBranchValuesAboveThreshold, treeBranch.nodeId, treeId);
    }

    public boolean doesRecordGoDownBranch(Record record) {
        boolean isRecordValueAboveThreshold = record.isAboveFeatureSplitThreshold(featureSplit);
        return isRecordValueAboveThreshold == isBranchValuesAboveThreshold;
    }

    public static TreeBranch[] getTreeBranchesCopy(TreeBranch[] treeBranchesToCopy, int treeId) {
        TreeBranch[] copiedTreeBranches = new TreeBranch[2];

        if (treeBranchesToCopy[0] != null) {
            copiedTreeBranches[0] = new TreeBranch(treeBranchesToCopy[0], treeId);
            copiedTreeBranches[1] = new TreeBranch(treeBranchesToCopy[1], treeId);
        }

        return copiedTreeBranches;
    }

    public Node getNode() {
        return CrossValidator.decisionTreeForRecordPrediction.allNodes.get(nodeId);
    }

}