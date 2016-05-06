package RecordPackage;

public class RecordBase {

    public int recordId;
    public boolean isClassLarge;

    public boolean isSameClass(RecordBase recordBase) {
        return recordBase.isClassLarge == isClassLarge;
    }

}
