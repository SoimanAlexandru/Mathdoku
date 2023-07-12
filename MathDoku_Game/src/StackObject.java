public class StackObject {
    int oldValue , newValue ,  row , column;
    public StackObject(int oldValue , int newValue , int row , int column){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.row = row;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getNewValue() {
        return newValue;
    }

    public int getOldValue() {
        return oldValue;
    }
}
