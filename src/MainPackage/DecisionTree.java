package MainPackage;

import Exceptions.*;
import NodePackage.NodeSet;
import NodePackage.Node;

import RecordPackage.Record;
import RecordPackage.RecordSet;
import TreeBranchPackage.FeatureSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DecisionTree {

    public static int nextTreeId = 0;

    public int nextNodeId = 0;
    public int treeId;
    public int rootNodeId = 0;
    public RecordSet recordSet;

    public NodeSet allNodes = new NodeSet();

    private DecisionTree() {
        this.treeId = getNextTreeId();
    }

    public DecisionTree(DecisionTree decisionTreeToCopy) {
        this();
        this.nextNodeId = decisionTreeToCopy.nextNodeId;
        this.rootNodeId = decisionTreeToCopy.rootNodeId;
        this.recordSet = decisionTreeToCopy.recordSet;
        this.allNodes = new NodeSet(decisionTreeToCopy.allNodes, treeId);
    }

    public DecisionTree(RecordSet recordSet) {
        this();
        this.recordSet = recordSet;
    }

    public static int getNextTreeId() {
        return nextTreeId++;
    }

    public void buildDecisionTree() {
        Node rootNode = createRootNode();
        rootNode.recursiveSplit();
    }

    public Node createRootNode() {
        List<Integer> rootRecordIds = recordSet.stream().map(record -> record.recordId).collect(Collectors.toList());
        return new Node(rootRecordIds, Record.featureColumnIndices, treeId);
    }

    public List<DecisionTree> getMBestTreesFromAllPossibleSplits(int m) throws NoNodesToSplitException {
        boolean wasANodeSplit = false;

        List<DecisionTree> decisionTreesFromAllSplits = new ArrayList<>();
        List<Node> nodesToSplit = allNodes.values().stream().filter(node -> node.isToBeSplit).collect(Collectors.toList());

        if (nodesToSplit.size() == 0) {
            throw new NoNodesToSplitException();
        }

        for (Node node: nodesToSplit) {
            try {
                decisionTreesFromAllSplits.addAll(getTreesFromFeatureSplits(node));
                wasANodeSplit = true;
            } catch (UnableToSplitNodeException ex){}
        }

        if (!wasANodeSplit) {
            throw new NoNodesToSplitException();
        }

        return getMBestTrees(m, decisionTreesFromAllSplits);

    }

    public void setUnSplitNodesAsLeafAndSplit() {
        allNodes.values().stream().filter(node1 -> node1.isToBeSplit)
                .collect(Collectors.toList()).forEach(node -> {node.setIsToBeSplit(false); node.setIsLeafNode(true);});
    }

    public static List<DecisionTree> getMBestTrees(int m, List<DecisionTree> decisionTrees) {
        int numTreesToReturn = decisionTrees.size() >= m ? m : decisionTrees.size();
        List<DecisionTree> topMTrees = sortDecisionTreesByScore(decisionTrees).subList(0, numTreesToReturn);
        return topMTrees.subList(0, numTreesToReturn);
    }

    public static List<DecisionTree> getAllTreesWithBetterScore(double score, List<DecisionTree> decisionTrees) throws NoSplitFromDecisionTreeImprovesErrorException {
        List<DecisionTree> decisionTreesWithBetterScore = decisionTrees;

        if (Session.getInstance().classificationParams.isUseTreeOverFittingPrevention()) {
            decisionTreesWithBetterScore = decisionTrees.stream()
                    .filter(decisionTree -> decisionTree.getTreeScore() < score).collect(Collectors.toList());
        }

        if (decisionTreesWithBetterScore.size() == 0) {
            throw new NoSplitFromDecisionTreeImprovesErrorException();
        }

        return decisionTreesWithBetterScore;

    }

    public static List<DecisionTree> sortDecisionTreesByScore(List<DecisionTree> decisionTrees) {
        return decisionTrees.stream()
                .sorted((decisionTree1, decisionTree2) -> new Double(decisionTree1.getTreeScore()).compareTo(decisionTree2.getTreeScore()))
                .collect(Collectors.toList());
    }

    public List<DecisionTree> getTreesFromFeatureSplits(Node node) throws UnableToSplitNodeException {
        boolean existsASplitOnNode = false;
        DecisionTree decisionTree = null;
        List<DecisionTree> decisionTrees = new ArrayList<>();
        List<Integer> featuresToSplit = node.remainingFeatureIndices;


        if (featuresToSplit.size() == 0) {
            throw new NoFeaturesLeftToSplitException();
        } else if (node.isAllRecordsOfSameClass()) {
            throw new AllRecordsOfSameClassException();
        }

        for (int featureId: featuresToSplit) {
            decisionTree = new DecisionTree(this);
            Session.getInstance().addDecisionTree(decisionTree);

            try {
                decisionTree.splitNodeOnFeature(node, featureId);
                decisionTrees.add(decisionTree);
                existsASplitOnNode = true;
            } catch (UnableToSplitFeatureException ex) {}
        }

        if (!existsASplitOnNode) {
            throw new UnableToSplitNodeException();
        }

        return decisionTrees;

    }

    public void splitNodeOnFeature(Node node, int featureId) throws UnableToSplitFeatureException {
        FeatureSplit bestSplitForFeature = FeatureSplit.getBestSplitForFeature(featureId, node.nodeRecords);
        allNodes.get(node.nodeId).splitNode(bestSplitForFeature);
    }


    public boolean predictClass(Record record) {
        Node currentNode = getRootNode();

        while (!currentNode.isLeafNode()) {
            try {
                currentNode = currentNode.getNextNode(record);
            } catch (Exception ex) {
                System.out.print(ex.getMessage());
            }

        }

        return currentNode.getMajorityClass();
    }

    public double getTreeScore() {
        //Uncomment one of these four lines of code to alter which error measure is used to determine when to stop splitting
        //a decision tree (for overfitting prevention).
        //This method will also determine which of the m trees are considered "best" for question 6 solution.
//
//        return getTreeScoreFromPessimisticError();
        return getTreeScoreFromMinDescLength();
//        return getTreeScoreFromFeatureSplitScore();
//        return getTreeScoreFromTrainingError();
    }

    public double getTreeScoreFromFeatureSplitScore() {
        double sumOfWeightedScores = allNodes.values().stream().filter(Node::isLeafNode)
                .mapToDouble(node1 -> node1.nodeScore * node1.nodeRecords.size()).sum();
        return sumOfWeightedScores / recordSet.size();
    }

    public double getTreeScoreFromPessimisticError() {
        double error = (1.0 * getNumClassificationErrors() + 0.5 * allNodes.size())  / recordSet.size();
        return error;
    }

    public double getTreeScoreFromTrainingError() {
        double error = 1.0 * getNumClassificationErrors() / recordSet.size();
        return error;
    }

    public double getTreeScoreFromMinDescLength() {
        int numInternalNodes = allNodes.getNonLeafNodes().size();
        int numLeafNodes = allNodes.size() - numInternalNodes;
        int numClassificationErrors = getNumClassificationErrors();

        return getCostOfInternalNode() * numInternalNodes + getLeafNodeCost() * numLeafNodes + getClassificationErrorCost() * numClassificationErrors;
    }

    public int getNextNodeId() {
        return nextNodeId++;
    }

    public Node getRootNode() {
        return allNodes.get(0);
    }

    public void addNode(int nodeId, Node node) {
        allNodes.put(nodeId, node);
    }

    private double getCostOfInternalNode() {
        return Math.log(allNodes.get(0).remainingFeatureIndices.size())/Math.log(2);
    }

    private double getClassificationErrorCost() {
        return Math.log(recordSet.size())/Math.log(2);
    }

    private int getNumClassificationErrors() {
        return allNodes.values().stream().filter(Node::isLeafNode)
                .mapToInt(Node::getNumRecordsInMinorityClass).sum();
    }

    private double getLeafNodeCost() {
        return Math.log(2)/Math.log(2);
    }

}

