import java.util.ArrayList;
import java.util.Collections;

public class Solver {
    private int N, NCage;
    private Cage[] cages;
    private int[][] board;
    private int[][] cageBoard;

    public Solver(int boardSize, Cage[] cages, int[][] cageBoard) {
        this.N = boardSize;
        this.cages = cages;
        this.NCage = cages.length;;
        this.cageBoard = cageBoard;
    }

    public void initialize(){
        board = new int[N+1][N+1];
        for(int i = 0 ; i < N ; i++){
            for(int j = 0 ; j < N ; j++){
                board[i][j] = 0;
                Location cell = new Location(i,j);
                cages[cageBoard[i][j]-1].addSquares(cell);
            }
        }
    }

    public boolean solve() {
        int x, y;
        Location empty = findEmpty();
        if (empty != null) {
            for (int i = 1; i <= N; i++) {
                if (isValid(empty.getX(), empty.getY(), i)) {
                    board[empty.getX()][empty.getY()] = i;
                    if(checkCage(cages[cageBoard[empty.getX()][empty.getY()]-1])) {
                        if (solve()) {
                            return true;
                        } else {
                            board[empty.getX()][empty.getY()] = 0;
                        }
                    }else{
                        board[empty.getX()][empty.getY()] = 0;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean isValid(int row, int col, int num) {
        return checkRow(row, num) && checkColumn(col, num);
    }

    public boolean checkRow(int row, int num) {
        for (int col = 0; col < N; col++) {
            if (board[row][col] == num) {
                return false;
            }
        }
        return true;
    }

    public boolean checkColumn(int col, int num) {
        for (int row = 0; row < N; row++) {
            if (board[row][col] == num) {
                return false;
            }
        }
        return true;
    }

    public boolean isFull(Cage cage){
        for(Integer cell : cage.getCellsNumber()){
            for(int i = 0 ; i < N ; i++){
                for(int j = 0 ; j < N ;j ++){
                    if(i * N + j == cell){
                        if(board[i][j] == 0){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkCage(Cage cage){
        if(isFull(cage)){
            char operator = cage.getOperand();
            int result = -1;
            if(operator == '+'){
                result = 0;
                for(Integer cell : cage.getCellsNumber()){
                    for(int i = 0 ; i < N ; i++){
                        for(int j = 0 ; j < N ;j ++){
                            if(i * N + j == cell){
                                result += board[i][j];
                            }
                        }
                    }
                }
                if(result == cage.getTarget()){
                    return true;
                }else{
                    return false;
                }
            }else if(operator == 'x'){
                result = 1;
                for(Integer cell : cage.getCellsNumber()){
                    for(int i = 0 ; i < N ; i++){
                        for(int j = 0 ; j < N ;j ++){
                            if(i * N + j == cell){
                                result *= board[i][j];
                            }
                        }
                    }
                }
                if(result == cage.getTarget()){
                    return true;
                }else{
                    return false;
                }
            }else if(operator == '-'){
                ArrayList<Integer> minusArray = new ArrayList<>();
                for(Integer integer : cage.getCellsNumber()){
                    for(int i = 0 ; i < N ; i++) {
                        for (int j = 0; j < N; j++) {
                            if (i * N + j == integer) {
                                minusArray.add(board[i][j]);
                            }
                        }
                    }
                }
                minusArray.sort(Collections.reverseOrder());
                result = minusArray.get(0);
                for(int i = 1 ; i < minusArray.size() ; i++){
                    result = result - minusArray.get(i);
                }
                if(result == cage.getTarget()){
                    return true;
                }else{
                    return false;
                }
            }else if(operator == '\u00F7') {
                boolean zeroFound = false;
                ArrayList<Integer> divideArray = new ArrayList<>();
                for(Integer integer : cage.getCellsNumber()){
                    for(int i = 0 ; i < N ; i++){
                        for(int j = 0 ; j < N ; j ++){
                            if(i * N + j == integer){
                                if(board[i][j] != 0){
                                    divideArray.add(board[i][j]);
                                }else{
                                    zeroFound = true;
                                    break;
                                }
                            }
                            if(zeroFound) break;
                        }
                        if(zeroFound) break;
                    }
                }
                if(!zeroFound) {
                    divideArray.sort(Collections.reverseOrder());
                    result = divideArray.get(0);
                    for (int i = 1; i < divideArray.size(); i++) {
                        result = result / divideArray.get(i);
                    }
                }
                if (result == cage.getTarget()) {
                    return true;
                } else {
                    return false;
                }
            }
            else if(operator == '='){
                result = cage.getTarget();
                return true;
            }
        }else{
            return true;
        }
        return false;
    }
    public Location findEmpty(){
        int i , j;
        for(i = 0 ; i < N; i++){
            for(j = 0 ; j < N; j++){
                if(board[i][j] == 0){
                    return new Location(i,j);
                }
            }
        }
        return null;
    }


    public int[][] getBoard() {
        return board;
    }
}
