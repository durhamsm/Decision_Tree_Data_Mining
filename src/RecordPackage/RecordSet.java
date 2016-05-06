package RecordPackage;

import Exceptions.MissingEntryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RecordSet extends ArrayList<Record> {

    public void addRecord(int recordIndex, String[] columns) throws MissingEntryException {
        add(new Record(recordIndex, columns));
    }

    public void addRecord(Record record) {
        add(record);
    }

    public RecordSet() {

    }

    public RecordSet getClone() {
        RecordSet recordSet = new RecordSet();

        for (Record record: this) {
            recordSet.addRecord(record);
        }

        return recordSet;
    }

    public Record getRecord(int recordId) {
        return stream().filter(record -> record.recordId == recordId).findAny().get();
    }

    public RecordSet getRecordSubSetList(List<Integer> recordIds) {
        RecordSet recordSubSet = new RecordSet();
        for (int recordId: recordIds) {
            recordSubSet.addRecord(getRecord(recordId));
        }
        return recordSubSet;
    }

    public List<Record> getRandom25PercentRecords() {
        long seed = System.nanoTime();
        List<Record> recordsToRandomize = stream().collect(Collectors.toList());
        Collections.shuffle(recordsToRandomize, new Random(seed));
        return recordsToRandomize.subList(0, recordsToRandomize.size() / 4);
    }


    public RecordSet(BufferedReader bufferedReader) {
        fillRecordSetFromReader(bufferedReader);
        calculateDiscreteClassValues();
    }

    public void calculateDiscreteClassValues() {
        if (Record.classIsAlreadyDiscrete) {
            forEach(record -> record.setIsClassLarge(record.classValue == 1));
        } else {
            double medianClassValue = stream()
                    .sorted((record1, record2) -> Double.valueOf(record1.classValue).compareTo(record2.classValue))
                    .collect(Collectors.toList())
                    .get(size() / 2).classValue;
            forEach(record -> record.setIsClassLarge(record.classValue > medianClassValue));
        }

    }



    public void sortOnFeature(int featureIndex) {
        sort((record1, record2) -> new Double(record1.featureValues[featureIndex]).compareTo(record2.featureValues[featureIndex]));
    }

    public void fillRecordSetFromReader(BufferedReader buf) {
        String line;
        int recordIndex = 0;

        try {
            line = buf.readLine();
            while (line != null) {
                line.replaceAll("^\\s+", "");
                String[] columns = line.split("\\t+");

                try {
                    addRecord(recordIndex, columns);
                    ++recordIndex;
                } catch (MissingEntryException ex){}

                line = buf.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}