package CrossValidation;

import MainPackage.DecisionTree;
import RecordPackage.PredictedRecord;
import RecordPackage.RecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrossValidator {

    private int numSets;
    public static DecisionTree decisionTreeForRecordPrediction;
    public static RecordSet fullRecordSet;
    public List<ValidatorPartition> validatorPartitions = new ArrayList<>();


    public CrossValidator(RecordSet recordSet, int numSets) {
        this.numSets = numSets;
        fullRecordSet = recordSet;
        setValidatorPartitions();
        predictRemovedRecords();
    }

    public void setValidatorPartitions() {
        for (int setIndex = 0; setIndex < numSets; ++setIndex) {
            validatorPartitions.add(getValidatorRecordSet(setIndex, numSets));
        }
    }

    public void predictRemovedRecords() {
        for (ValidatorPartition validatorPartition: validatorPartitions) {
            decisionTreeForRecordPrediction = validatorPartition.decisionTree;
            validatorPartition.predictRecords();
        }
    }



    public ValidatorPartition getValidatorRecordSet(int setIndex, int numSets) {
        int  totalNumRecords = fullRecordSet.size();
        RecordSet trainingRecordSet = fullRecordSet.getClone();
        List<Integer> recordIdsToPredict;
        List<Integer> removedRecordIds = new ArrayList<>();


        int currentIndexToRemove = totalNumRecords - setIndex - 1;

        while(currentIndexToRemove > -1) {
            trainingRecordSet.remove(currentIndexToRemove);
            removedRecordIds.add(currentIndexToRemove);
            currentIndexToRemove -= numSets;
        }

        //Toggle commenting on next to lines to change which records used for validation during tree-building
        recordIdsToPredict = removedRecordIds;
//        recordIdsToPredict = trainingRecordSet.getRandom25PercentRecords().stream().map(record -> record.recordId).collect(Collectors.toList());

        return new ValidatorPartition(trainingRecordSet, fullRecordSet.getRecordSubSetList(recordIdsToPredict));
    }

    public double getPredictionError() {
        return validatorPartitions.stream().mapToDouble(ValidatorPartition::getPredictionError).average().getAsDouble();
    }

    public double getAveragePessimisticError() {
        return validatorPartitions.stream().mapToDouble(ValidatorPartition::getPessimisticError).average().getAsDouble();
    }

    public double getAverageTrainingError() {
        return validatorPartitions.stream().mapToDouble(ValidatorPartition::getTrainingError).average().getAsDouble();
    }

    public double getAverageMinDescLength() {
        return validatorPartitions.stream().mapToDouble(ValidatorPartition::getMinDescLength).average().getAsDouble();
    }

    public double getAverageNumNodes() {
        return validatorPartitions.stream().mapToInt(validatorPartition -> validatorPartition.decisionTree.allNodes.size())
                .average().getAsDouble();
    }

    public double getSimpleAccuracy() {
        return 1.0 * (getNumTruePositivePredictions() + getNumTrueNegativePredictions()) / fullRecordSet.size();
    }

    public double getFMeasure() {
        double precision = getPrecision();
        double recall = getRecall();
        return (2 * recall * precision) / (recall + precision);
    }

    public double getPrecision() {
        int numTruePositivePredictions = getNumTruePositivePredictions();
        //int numFalsePositivePredictions = getNumFalsePositivePredictions();
        return 1.0 * numTruePositivePredictions / (getPositivePredictions().size());
    }

    public double getRecall() {
        int numTruePositivePredictions = getNumTruePositivePredictions();
        return 1.0 * numTruePositivePredictions / (numTruePositivePredictions + getNumFalseNegativePredictions());
    }

    public int getNumTruePositivePredictions() {
        List<PredictedRecord> positivePredictions = getPositivePredictions();
        return (int)positivePredictions.stream()
                .filter(predictedRecord -> predictedRecord.isSameClass(fullRecordSet.getRecord(predictedRecord.recordId))).count();
    }

    public int getNumFalsePositivePredictions() {
        return getPositivePredictions().size() - getNumTruePositivePredictions();
    }

    public int getNumTrueNegativePredictions() {
        List<PredictedRecord> negativePredictions = getNegativePredictions();
        return (int)negativePredictions.stream()
                .filter(predictedRecord -> predictedRecord.isSameClass(fullRecordSet.getRecord(predictedRecord.recordId))).count();
    }

    public int getNumFalseNegativePredictions() {
        return getNegativePredictions().size() - getNumTrueNegativePredictions();
    }

    public double getFalsePositiveRate() {
        return 1.0 * getNumFalsePositivePredictions() / (getNumFalsePositivePredictions() + getNumTrueNegativePredictions());
    }

    public double getTruePositiveRate() {
        double value = 1.0 * getNumTruePositivePredictions() / (getNumTruePositivePredictions() + getNumFalseNegativePredictions());
        return value;
    }

    public double getTrueNegativeRate() {
        return 1.0 * getNumTrueNegativePredictions() / (getNumTrueNegativePredictions() + getNumFalsePositivePredictions());
    }

    public double getBalancedSampleAccuracy() {
        return (getTruePositiveRate() + getTrueNegativeRate()) / 2;
    }


    private List<PredictedRecord> getPositivePredictions() {
        List<PredictedRecord> predictedRecords = getPredictedRecords();
        return predictedRecords.stream().filter(predictedRecord -> predictedRecord.isClassLarge)
                .collect(Collectors.toList());
    }

    private List<PredictedRecord> getNegativePredictions() {
        List<PredictedRecord> predictedRecords = getPredictedRecords();
        return predictedRecords.stream().filter(predictedRecord -> !predictedRecord.isClassLarge)
                .collect(Collectors.toList());
    }

    private List<PredictedRecord> getPredictedRecords() {
        List<PredictedRecord> predictedRecords = new ArrayList<>();
        for (ValidatorPartition validatorPartition: validatorPartitions) {
            predictedRecords.addAll(validatorPartition.predictedRecords);
        }
        return predictedRecords;
    }

}