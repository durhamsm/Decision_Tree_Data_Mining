package CrossValidation;

import MainPackage.DecisionTree;
import MainPackage.Session;
import RecordPackage.PredictedRecord;
import RecordPackage.RecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidatorPartition {


    public DecisionTree decisionTree;
    RecordSet trainingSet;
    RecordSet recordSetToPredict;
    List<PredictedRecord> predictedRecords = new ArrayList<>();

    public ValidatorPartition(RecordSet trainingSet, RecordSet recordSetToPredict) {
        this.trainingSet = trainingSet;
        this.recordSetToPredict = recordSetToPredict;

        initializeDecisionTreeList();
        Session.getInstance().buildMBestTrees(Session.getInstance().classificationParams.getM());
        this.decisionTree = Session.getInstance().getBestTree();
        Session.getInstance().removeAllTrees();
    }

    private void initializeDecisionTreeList() {
        DecisionTree decisionTree = new DecisionTree(trainingSet);
        Session.getInstance().addDecisionTree(decisionTree);
        decisionTree.createRootNode();
    }

    public List<PredictedRecord> getMissingRecordPredictions() {
            return recordSetToPredict.stream().map(record ->
                    new PredictedRecord(record.recordId, decisionTree.predictClass(record)))
                    .collect(Collectors.toList());
    }

    public double getPessimisticError() {
        return decisionTree.getTreeScoreFromPessimisticError();
    }

    public double getTrainingError() {
        return decisionTree.getTreeScoreFromTrainingError();
    }

    public double getMinDescLength() {
        return decisionTree.getTreeScoreFromMinDescLength();
    }


    public double getPredictionError() {
        int numCorrect = (int)predictedRecords.stream()
                .map(predictedRecord -> predictedRecord.isSameClass(CrossValidator.fullRecordSet.getRecord(predictedRecord.recordId)))
                .filter(aBoolean -> aBoolean).count();
        return 1.0 - 1.0 * numCorrect / predictedRecords.size();
    }


    public void predictRecords() {
        predictedRecords.addAll(getMissingRecordPredictions());
    }

}
