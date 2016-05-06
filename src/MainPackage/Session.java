package MainPackage;

import Exceptions.AllTreesDoneException;
import Exceptions.NoNodesToSplitException;
import Exceptions.UnableToSplitTreeException;
import RecordPackage.RecordSet;
import TreeBranchPackage.FeatureSplitScore;
import TreeBranchPackage.GiniFeatureSplitScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Session {

    public static final ClassificationParameters classificationParams = new ClassificationParameters();

    public HashMap<Integer, DecisionTree> decisionTrees = new HashMap<>();
    private static Session ourInstance = new Session();
    public static Session getInstance() {
        return ourInstance;
    }

    public int[] values;

    private Session() {

    }

    public void buildMBestTrees(int m) {
        try {
            while(true) {
                splitTrees(m);
            }
        } catch (AllTreesDoneException ex) {}
    }

    public void splitTrees(int m) throws AllTreesDoneException {
        boolean isAllTreesDoneSplitting = true;
        List<DecisionTree> decisionTreesFromSplit = new ArrayList<>();
        List<DecisionTree> decisionTreesFromAllSplits = new ArrayList<>();
        List<DecisionTree> decisionTreesPriorToSplitting = new ArrayList<>(decisionTrees.values());

        for (DecisionTree decisionTree: decisionTreesPriorToSplitting) {
            try {
                decisionTreesFromSplit = decisionTree.getMBestTreesFromAllPossibleSplits(m);
                decisionTreesFromAllSplits.addAll(DecisionTree.getAllTreesWithBetterScore(decisionTree.getTreeScore(), decisionTreesFromSplit));
                isAllTreesDoneSplitting = false;
            } catch (UnableToSplitTreeException ex) {
                decisionTree.setUnSplitNodesAsLeafAndSplit();
                decisionTreesFromAllSplits.add(decisionTree);
            }
        }

        Session.getInstance().setTrees(DecisionTree.getMBestTrees(m, decisionTreesFromAllSplits));

        if (isAllTreesDoneSplitting) {
            throw new AllTreesDoneException();
        }

    }

    public void addDecisionTree(DecisionTree decisionTree) {
        decisionTrees.put(decisionTree.treeId, decisionTree);
    }

    public RecordSet getDecisionTreeRecordSet(int treeId) {
        return decisionTrees.get(treeId).recordSet;
    }

    public void removeTrees(List<DecisionTree> decisionTreesToRemove) {
        for (DecisionTree decisionTreeToRemove: decisionTreesToRemove) {
            decisionTrees.remove(decisionTreeToRemove.treeId);
        }
    }

    public void removeAllTrees() {
        decisionTrees.clear();
    }

    public void setTrees(List<DecisionTree> treesToSet) {
        decisionTrees.clear();
        for (DecisionTree decisionTreeToAdd: treesToSet) {
            decisionTrees.put(decisionTreeToAdd.treeId, decisionTreeToAdd);
        }
    }

    public DecisionTree getBestTree() {
        return DecisionTree.sortDecisionTreesByScore(new ArrayList<DecisionTree>(decisionTrees.values())).get(0);
    }

    public void setValues(int[] values) {
        this.values = values;
    }



}

