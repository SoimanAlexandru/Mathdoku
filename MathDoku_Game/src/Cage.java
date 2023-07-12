import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

public class Cage {
    private Shape cage;
    private int target;
    private char operand;
    private ArrayList<Integer> cellsNumber = new ArrayList<>();
    private ArrayList<Shape> cageCells = new ArrayList<>();
    private ArrayList<Cage> allCages = new ArrayList<>();
    private ArrayList<Location> squares = new ArrayList<>();
    public Cage(int result , char operand , ArrayList<Shape> array , ArrayList<Integer> cellsNumber){
        this.target = result;
        this.operand = operand;
        this.cageCells = array;
        this.cellsNumber = cellsNumber;
    }

    public Shape createCage(){
        if(cageCells.size() != 1) {
            for (int i = 0; i < cageCells.size() - 1; i++) {
                if (i == 0) {
                    cageCells.get(i).setFill(Color.WHITE);
                    cageCells.get(i + 1).setFill(Color.WHITE);
                    cage = Shape.union(cageCells.get(i), cageCells.get(i + 1));
                } else {
                    cageCells.get(i + 1).setFill(Color.WHITE);
                    cage = Shape.union(cage, cageCells.get(i + 1));
                }
            }
        }else{
            cageCells.get(0).setFill(Color.WHITE);
            cage = Shape.union(cageCells.get(0) , cageCells.get(0));
        }
        cage.setStrokeWidth(4);
        cage.setStroke(Color.BLACK);
        cage.setFill(Color.TRANSPARENT);
        cage.setMouseTransparent(true);
        allCages.add(this);
        return cage;
    }

    public char getOperand() {
        return operand;
    }

    public int getTarget() {
        return target;
    }

    public ArrayList<Integer> getCellsNumber() {
        return cellsNumber;
    }
    public void addSquares(Location location){
        squares.add(location);
    }

}
