import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class MathdokuGUI extends Application{
    private BorderPane root = new BorderPane();
    private Pane pane;
    private FlowPane wrap;
    private int selectedRow = -1, selectedColumn = -1 , valueX = 30 , valueY = 46;
    private Text[][] textMatrix;
    private ArrayList<Shape> recs = new ArrayList<Shape>();
    private ArrayList<Shape> cellsForCageArray = new ArrayList<>() ;
    private int[][] inputMatrix , cageBoard , solvedBoard;
    private Button number1 , number2 , number3 , number4 , number5 , number6 , number7 , number8 , clear;
    private Button undoButton , redoButton , clearButton , showMistakes , solveButton , hintButton;
    private boolean isCorrect = false , showDialog = true , isSolved=false;
    private Rectangle[][] rectangles;
    private ArrayList<Label> labelArrayList = new ArrayList<>();
    private BufferedReader readerDimension , readerCages , checkCages;
    private int dimension;
    private int[] cellArray ;
    private ArrayList<Cage> allCages = new ArrayList<>();
    private Stack<StackObject> undoStack = new Stack<>();
    private Stack<StackObject> redoStack = new Stack<>();
    private Font font = new Font(25);
    private int hintNumber = 0;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        VBox dimensionVbox = new VBox();
        dimensionVbox.setAlignment(Pos.CENTER);
        dimensionVbox.setSpacing(50);
        Label label = new Label("Select dimension: ");
        Button smallFont = new Button("Small Font");
        smallFont.setOnAction(e -> {
            for (int i = 0 ; i < textMatrix.length ; i++){
                for(int j = 0 ; j < textMatrix.length ; j++) {
                    font = Font.font(20);
                    valueX = 32;
                    valueY = 45;
                    if (textMatrix[i][j] != null) {
                        textMatrix[i][j].setFont(new Font(20));
                        textMatrix[i][j].setX(j * 75 + valueX);
                        textMatrix[i][j].setY(i * 75 +valueY);
                    }
                }
            }
            for(Label counter : labelArrayList){
                counter.setFont(new Font(12));
            }
        });
        Button mediumFont = new Button("Medium Font");
        mediumFont.setOnAction(e -> {
            for (int i = 0 ; i < textMatrix.length ; i++){
                for(int j = 0 ; j < textMatrix.length ; j++){
                    font = Font.font(25);
                    valueX = 30;
                    valueY = 46;
                    if(textMatrix[i][j] != null){
                        textMatrix[i][j].setFont(new Font(25));
                        textMatrix[i][j].setX(j * 75 + valueX);
                        textMatrix[i][j].setY(i * 75 +valueY);
                    }
                }
            }
            for(Label counter : labelArrayList){
                counter.setFont(new Font(15));
            }
        });
        Button largeFont = new Button("Large font");
        largeFont.setOnAction(e -> {
            for (int i = 0 ; i < textMatrix.length ; i++){
                for(int j = 0 ; j < textMatrix.length ; j++){
                    font = Font.font(45);
                    valueX = 25;
                    valueY = 55;
                    if(textMatrix[i][j] != null) {
                        textMatrix[i][j].setFont(new Font(45));
                        textMatrix[i][j].setX(j * 75 + valueX);
                        textMatrix[i][j].setY(i * 75 +valueY);
                    }
                }
            }
            for(Label counter : labelArrayList){
                counter.setFont(new Font(17));
            }
        });

        dimensionVbox.getChildren().addAll(label,smallFont,mediumFont,largeFont);

        HBox optionBox = new HBox();
        optionBox.setAlignment(Pos.CENTER);
        optionBox.setSpacing(10);
        undoButton = new Button("Undo");
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> {
            redoButton.setDisable(false);
            StackObject stackObject = undoStack.pop();
            if(undoStack.size() == dimension*dimension || undoStack.size() == 0){
                undoButton.setDisable(true);
            }else{
                undoButton.setDisable(false);
            }
            redoStack.push(stackObject);
            if(redoStack.size() == 0){
                redoButton.setDisable(true);
            } else {
                redoButton.setDisable(false);
            }
            inputMatrix[stackObject.getRow()][stackObject.getColumn()] = stackObject.getOldValue();
            textMatrix[stackObject.getRow()][stackObject.getColumn()].setStroke(Color.BLACK);
            if(stackObject.getOldValue() != 0) {
                textMatrix[stackObject.getRow()][stackObject.getColumn()].setText(String.valueOf(stackObject.getOldValue()));
            } else {
                textMatrix[stackObject.getRow()][stackObject.getColumn()].setText("");
            }
       });

        redoButton = new Button("Redo");
        redoButton.setOnAction(e -> {
            if(redoStack.size() == 1){
                redoButton.setDisable(true);
            } else {
                redoButton.setDisable(false);
            }
            StackObject stackObject = redoStack.pop();
            undoStack.push(stackObject);
            if(undoStack.size() == dimension*dimension){
                undoButton.setDisable(true);
            }else{
                undoButton.setDisable(false);
            }
            inputMatrix[stackObject.getRow()][stackObject.getColumn()] = stackObject.getNewValue();
            textMatrix[stackObject.getRow()][stackObject.getColumn()].setStroke(Color.BLACK);
            if(stackObject.getNewValue() != 0){
                textMatrix[stackObject.getRow()][stackObject.getColumn()].setText(String.valueOf(stackObject.getNewValue()));
            } else {
                textMatrix[stackObject.getRow()][stackObject.getColumn()].setText("");
            }
        });

        redoButton.setDisable(true);
        clearButton = new Button("Clear Board");
        showMistakes = new Button("Show mistakes");
        showMistakes.setOnAction((e -> {
            showDialog = false;
            Mistakes();
        }));

        optionBox.getChildren().addAll(undoButton,redoButton,clearButton, showMistakes);

        HBox loadBox = new HBox();
        loadBox.setAlignment(Pos.CENTER);
        loadBox.setSpacing(10);
        Button loadFromFile = new Button("Load game from file");
        loadFromFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File to Load");
            File file = fileChooser.showSaveDialog(stage);
            try {
                readerDimension = new BufferedReader(new InputStreamReader(new FileInputStream(file) , "UTF8"));
                readerCages = new BufferedReader(new InputStreamReader(new FileInputStream(file) , "UTF8"));
                checkCages = new BufferedReader(new InputStreamReader(new FileInputStream(file) , "UTF8"));
                setFileDimension();
                if(!checkCorrectFile(checkCages)){
                    Alert alert = new Alert(Alert.AlertType.ERROR , "WRONG INPUT IN FILE!");
                    alert.showAndWait();
                } else {
                    undoButton.setDisable(true);
                    undoStack.clear();
                    redoButton.setDisable(true);
                    redoStack.clear();
                    hintNumber = 0;
                    hintButton.setDisable(false);
                    root.getChildren().remove(wrap);
                    recs.clear();
                    allCages.clear();
                    cellArray = new int[dimension * dimension];
                    createRectangles(dimension);
                    createFileCages();
                    textMatrix = new Text[dimension][dimension];
                    pane = createGrid(recs);
                    for (int i = 0; i < labelArrayList.size(); i++) {
                        pane.getChildren().add(labelArrayList.get(i));
                    }
                    wrap = new FlowPane();
                    wrap.setAlignment(Pos.CENTER);
                    wrap.getChildren().add(pane);
                    root.setCenter(wrap);
                    setInputButtonsAction();

                }


            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        });

        Button loadFromInput = new Button("Load game from text input");
        loadFromInput.setOnAction(e -> {
            final Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            TextArea textArea = new TextArea();
            textArea.setPrefHeight(400);
            textArea.setPrefWidth(400);
            Button submit = new Button("Submit");
            VBox box = new VBox(textArea);
            box.getChildren().add(submit);
            submit.setOnAction(ev -> {
                String input = textArea.getText();
                setInputDimension(input);
                if(!checkCorrectInput(input)){
                    Alert alert = new Alert(Alert.AlertType.ERROR , "WRONG INPUT!");
                    alert.showAndWait();
                }else {
                    undoButton.setDisable(true);
                    undoStack.clear();
                    redoButton.setDisable(true);
                    redoStack.clear();
                    hintNumber = 0;
                    hintButton.setDisable(false);
                    root.getChildren().remove(wrap);
                    recs.clear();
                    allCages.clear();
                    cellArray = new int[dimension * dimension];
                    createRectangles(dimension);
                    createInputCages(input);
                    if (dimension < 3) {
                        number3.setDisable(true);
                        number4.setDisable(true);
                        number5.setDisable(true);
                        number6.setDisable(true);
                        number7.setDisable(true);
                        number8.setDisable(true);
                    } else if (dimension == 3) {
                        number3.setDisable(false);
                        number4.setDisable(true);
                        number5.setDisable(true);
                        number6.setDisable(true);
                        number7.setDisable(true);
                        number8.setDisable(true);
                    } else if (dimension == 4) {
                        number3.setDisable(false);
                        number4.setDisable(false);
                        number5.setDisable(true);
                        number6.setDisable(true);
                        number7.setDisable(true);
                        number8.setDisable(true);
                    } else if (dimension == 5) {
                        number3.setDisable(false);
                        number4.setDisable(false);
                        number5.setDisable(false);
                        number6.setDisable(true);
                        number7.setDisable(true);
                        number8.setDisable(true);
                    } else if (dimension == 6) {
                        number3.setDisable(false);
                        number4.setDisable(false);
                        number5.setDisable(false);
                        number6.setDisable(false);
                        number7.setDisable(true);
                        number8.setDisable(true);
                    } else if (dimension == 7) {
                        number3.setDisable(false);
                        number4.setDisable(false);
                        number5.setDisable(false);
                        number6.setDisable(false);
                        number7.setDisable(false);
                        number8.setDisable(true);
                    } else if (dimension == 8) {
                        number3.setDisable(false);
                        number4.setDisable(false);
                        number5.setDisable(false);
                        number6.setDisable(false);
                        number7.setDisable(false);
                        number8.setDisable(false);
                    }
                    textMatrix = new Text[dimension][dimension];
                    pane = createGrid(recs);
                    for (int i = 0; i < labelArrayList.size(); i++) {
                        pane.getChildren().add(labelArrayList.get(i));
                    }
                    wrap = new FlowPane();
                    wrap.setAlignment(Pos.CENTER);
                    wrap.getChildren().add(pane);
                    root.setCenter(wrap);
                    dialog.hide();
                }
            });
            Scene dialogScene = new Scene(box , 400 , 400);
            dialog.setScene(dialogScene);
            dialog.show();
        });
        solveButton = new Button("Solve!");
        solveButton.setOnAction(event -> {
            boolean worked = false;
            Cage[] cageArray = allCages.toArray(new Cage[0]);
            Solver solver = new Solver(dimension , cageArray , cageBoard);
            solver.initialize();
            solvedBoard = new int[dimension+1][dimension+1];
            if(solver.solve()){
                 solvedBoard = solver.getBoard();
                 worked = true;
            }else{
                System.out.println("OOPS");
            }
            if(worked) {
                undoButton.setDisable(true);
                undoStack.clear();
                redoButton.setDisable(true);
                redoStack.clear();
                hintButton.setDisable(true);
                hintNumber = dimension * dimension;
                for (int row = 0; row < dimension; row++) {
                    for (int col = 0; col < dimension; col++) {
                        if (textMatrix[row][col] != null) {
                            textMatrix[row][col].setStroke(Color.BLACK);
                            textMatrix[row][col].setText(String.valueOf(solvedBoard[row][col]));
                            inputMatrix[row][col] = solvedBoard[row][col];
                            cellArray[row * dimension + col] = solvedBoard[row][col];
                        } else {
                            Text text1 = new Text(col * 75 + valueX, row * 75 + valueY, "");
                            text1.setFont(font);
                            text1.setStroke(Color.BLACK);
                            text1.setText(String.valueOf(solvedBoard[row][col]));
                            pane.getChildren().add(text1);
                            textMatrix[row][col] = text1;
                            inputMatrix[row][col] = solvedBoard[row][col];
                            cellArray[row * dimension + col] = solvedBoard[row][col];

                        }
                    }
                }
            }
        });
        hintButton = new Button("Hint");
        hintButton.setOnAction(even -> {
            boolean worked = false , hintGiven = false;
            Cage[] cageArray = allCages.toArray(new Cage[0]);
            Solver solver = new Solver(dimension , cageArray , cageBoard);
            solver.initialize();
            solvedBoard = new int[dimension+1][dimension+1];
            if(solver.solve()){
                solvedBoard = solver.getBoard();
                worked = true;
            }else{
                System.out.println("OOPS");
            }
            if(worked) {
                Random random = new Random();
                while(!hintGiven && hintNumber < dimension * dimension) {
                    int row = random.nextInt(dimension);
                    int col = random.nextInt(dimension);
                    if (textMatrix[row][col] == null || textMatrix[row][col].getText().equals("")) {
                        int oldValue = 0;
                        Text text1 = new Text(col * 75 + valueX, row * 75 + valueY, "");
                        text1.setFont(font);
                        text1.setStroke(Color.BLACK);
                        text1.setText(String.valueOf(solvedBoard[row][col]));
                        int newValue = solvedBoard[row][col];
                        StackObject object = new StackObject(oldValue , newValue , row , col);
                        undoStack.push(object);
                        undoButton.setDisable(false);
                        pane.getChildren().add(text1);
                        textMatrix[row][col] = text1;
                        inputMatrix[row][col] = solvedBoard[row][col];
                        cellArray[row * dimension + col] = solvedBoard[row][col];
                        hintGiven = true;
                        hintNumber++;

                    }
                }
            }
            if(hintNumber == dimension*dimension){
                hintButton.setDisable(true);
            }
        });
        loadBox.getChildren().addAll(loadFromFile , loadFromInput , solveButton , hintButton);

        VBox numberVbox = new VBox();
        numberVbox.setAlignment(Pos.CENTER);
        numberVbox.setSpacing(10);
        Label label1 = new Label("Select number you want to enter: ");

        number1 = new Button("1");
        number2 = new Button("2");
        number3 = new Button("3");
        number4 = new Button("4");
        number5 = new Button("5");
        number6 = new Button("6");
        number7 = new Button("7");
        number8 = new Button("8");
        clear = new Button("Clear cell");

        numberVbox.getChildren().addAll(label1,number1,number2,number3,number4,number5,number6,number7,number8,clear);

        dimension = 6;
        createRectangles(dimension);
        textMatrix = new Text[dimension][dimension];
        cellArray = new int[dimension*dimension];
        Cages();
        pane = createGrid(recs);
        displayLabels();
        wrap = new FlowPane();
        wrap.setAlignment(Pos.CENTER);
        wrap.getChildren().add(pane);

        handleKeyPressEvent();
        setInputButtonsAction();
        handleClearButton();


        root.setPadding(new Insets(30,10,30,10));
        root.setTop(optionBox);
        root.setLeft(dimensionVbox);
        root.setRight(numberVbox);
        root.setBottom(loadBox);
        root.setCenter(wrap);

        stage.setScene(new Scene(root, 1100, 750));
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.show();
    }
    public void createRectangles(int n){
        rectangles = new Rectangle[n][n];
        inputMatrix = new int[n][n];
        double width = 75.0;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                final int row = i;
                final int column = j;
                inputMatrix[i][j] = 0;
                StackObject obj = new StackObject(0 , 0 , i , j);
                undoStack.push(obj);
                rectangles[i][j] = new Rectangle();
                rectangles[i][j].setOnMouseClicked(e -> {
                    if(selectedRow == -1) {
                        rectangles[row][column].setFill(Color.YELLOW);
                        selectedRow = row;
                        selectedColumn = column;
                    }else{
                        rectangles[selectedRow][selectedColumn].setFill(Color.WHITE);
                        selectedColumn = column;
                        selectedRow = row;
                        rectangles[selectedRow][selectedColumn].setFill(Color.YELLOW);

                    }
                });
                rectangles[i][j].setX(width * j);
                rectangles[i][j].setY(width * i);
                rectangles[i][j].setHeight(width);
                rectangles[i][j].setWidth(width);
                rectangles[i][j].setFill(null);
                rectangles[i][j].setStroke(Color.BLACK);
                recs.add(rectangles[i][j]);
            }
        }
    }
    public Pane createGrid(ArrayList<Shape> elem){
        Pane pane = new Pane();
        for(int i = 0; i < recs.size() ; i++){
            pane.getChildren().add(elem.get(i));
        }
        return pane;
    }
    private void createFileCages(){
        String line , text , c="";
        int result , cell , min=-1;
        char operand = ' ';
        String cells;
        labelArrayList.clear();
        try{
            line = getLine(readerCages);
            while(line != null){
                ArrayList<Integer> cellsNumber = new ArrayList<>();
                cellsForCageArray = new ArrayList<>();
                text = line.split(" " )[0];
                if(text.length()!=1) {
                    if (text.charAt(text.length() - 1) == '+' || text.charAt(text.length() - 1) == '-' || text.charAt(text.length() - 1) == 'x' || text.charAt(text.length() - 1) == '\u00F7') {
                        operand = text.charAt(text.length() - 1);
                    }
                    result = Integer.parseInt(text.substring(0, text.length() - 1));
                    cells = line.split(" ")[1];
                    String[] splitter = cells.split(",");
                    for (int i = 0; i < splitter.length; i++) {
                        cell = Integer.parseInt(splitter[i]) - 1;
                        if (i == 0) {
                            min = cell;
                        } else {
                            if (cell < min) {
                                min = cell;
                            }
                        }
                        cellsNumber.add(cell);
                        cellsForCageArray.add(recs.get(cell));
                    }
                }else{
                    result = Integer.parseInt(text);
                    operand = '=';
                    c = line.split(" ")[1];
                    cellsNumber.add(Integer.parseInt(c)-1);
                    cellsForCageArray.add(recs.get(Integer.parseInt(c)-1));
                    min = Integer.parseInt(c)-1;
                }
                Label label;
                if(operand != ' ') {
                     label = new Label(String.valueOf(result) + operand);
                }else{
                     label = new Label(String.valueOf(result));
                }
                label.relocate(recs.get(min).getBoundsInParent().getMinX()+4 , recs.get(min).getBoundsInParent().getMinY()+1);
                label.setFont(new Font(15));
                labelArrayList.add(label);
                Cage cage = new Cage(result, operand, cellsForCageArray, cellsNumber);
                allCages.add(cage);
                recs.add(cage.createCage());
                line = getLine(readerCages);
                createCageBoard();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void createInputCages(String input){
        String[] lines = input.split("\\r?\\n");
        int result , cell , min;
        char operand = ' ';
        String cells , text , c="";
        labelArrayList.clear();
        for(int i = 0 ; i < lines.length ; i++){
            ArrayList<Integer> cellsNumber = new ArrayList<>();
            cellsForCageArray = new ArrayList<>();
            text = lines[i].split(" " )[0];
            if(text.length()!=1) {
                if (text.charAt(text.length() - 1) == '+' || text.charAt(text.length() - 1) == '-' || text.charAt(text.length() - 1) == 'x' || text.charAt(text.length() - 1) == '\u00F7') {
                    operand = text.charAt(text.length() - 1);
                }
                result = Integer.parseInt(text.substring(0, text.length() - 1));
                cells = lines[i].split(" ")[1];
                String[] splitter = cells.split(",");
                min = -1;
                for (int j = 0; j < splitter.length; j++) {
                    cell = Integer.parseInt(splitter[j]) - 1;
                    if (j == 0) {
                        min = cell;
                    } else {
                        if (cell < min) {
                            min = cell;
                        }
                    }
                    cellsNumber.add(cell);
                    cellsForCageArray.add(recs.get(cell));
                }
            }else{
                result = Integer.parseInt(text);
                operand = '=';
                c = lines[i].split(" ")[1];
                cellsNumber.add(Integer.parseInt(c)-1);
                cellsForCageArray.add(recs.get(Integer.parseInt(c)-1));
                min = Integer.parseInt(c)-1;
            }
            Label label;
            if(operand != ' ') {
                label = new Label(String.valueOf(result) + operand);
            }else{
                label = new Label(String.valueOf(result));
            }
            label.relocate(recs.get(min).getBoundsInParent().getMinX()+4 , recs.get(min).getBoundsInParent().getMinY()+1);
            label.setFont(new Font(15));
            labelArrayList.add(label);
            Cage cage = new Cage(result , operand , cellsForCageArray , cellsNumber);
            allCages.add(cage);
            recs.add(cage.createCage());
            createCageBoard();
        }
    }
    private void Cages(){
        ArrayList<Integer> cellsNumber;
        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(0); cellsNumber.add(6);
        cellsForCageArray.add(recs.get(0)); cellsForCageArray.add(recs.get(6));
        Cage cage = new Cage(11 , '+' , cellsForCageArray , cellsNumber);
        allCages.add(cage);
        recs.add(cage.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(1); cellsNumber.add(2);
        cellsForCageArray.add(recs.get(1)); cellsForCageArray.add(recs.get(2));
        Cage cage1 = new Cage(2 , '\u00F7' , cellsForCageArray , cellsNumber);
        allCages.add(cage1);
        recs.add(cage1.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(3); cellsNumber.add(9);
        cellsForCageArray.add(recs.get(3)); cellsForCageArray.add(recs.get(9));
        Cage cage2 = new Cage(20 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage2);
        recs.add(cage2.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(4); cellsNumber.add(5); cellsNumber.add(11); cellsNumber.add(17);
        cellsForCageArray.add(recs.get(4)); cellsForCageArray.add(recs.get(5)); cellsForCageArray.add(recs.get(11));cellsForCageArray.add(recs.get(17));
        Cage cage3 = new Cage(6 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage3);
        recs.add(cage3.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(7); cellsNumber.add(8);
        cellsForCageArray.add(recs.get(7)); cellsForCageArray.add(recs.get(8));
        Cage cage4 = new Cage(3 , '-' , cellsForCageArray , cellsNumber);
        allCages.add(cage4);
        recs.add(cage4.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(10); cellsNumber.add(16);
        cellsForCageArray.add(recs.get(10)); cellsForCageArray.add(recs.get(16));
        Cage cage5 = new Cage(3 , '\u00F7' , cellsForCageArray , cellsNumber);
        allCages.add(cage5);
        recs.add(cage5.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(12); cellsNumber.add(13); cellsNumber.add(18);cellsNumber.add(19);
        cellsForCageArray.add(recs.get(12)); cellsForCageArray.add(recs.get(13));cellsForCageArray.add(recs.get(18));cellsForCageArray.add(recs.get(19));
        Cage cage6 = new Cage(240 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage6);
        recs.add(cage6.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(14); cellsNumber.add(15);
        cellsForCageArray.add(recs.get(14)); cellsForCageArray.add(recs.get(15));
        Cage cage7 = new Cage(6 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage7);
        recs.add(cage7.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(20); cellsNumber.add(26);
        cellsForCageArray.add(recs.get(20)); cellsForCageArray.add(recs.get(26));
        Cage cage8 = new Cage(6 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage8);
        recs.add(cage8.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(21); cellsNumber.add(27); cellsNumber.add(28);
        cellsForCageArray.add(recs.get(21)); cellsForCageArray.add(recs.get(27)); cellsForCageArray.add(recs.get(28));
        Cage cage9 = new Cage(7 , '+' , cellsForCageArray , cellsNumber);
        allCages.add(cage9);
        recs.add(cage9.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(22); cellsNumber.add(23);
        cellsForCageArray.add(recs.get(22)); cellsForCageArray.add(recs.get(23));
        Cage cage10 = new Cage(30 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage10);
        recs.add(cage10.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(24); cellsNumber.add(25);
        cellsForCageArray.add(recs.get(24)); cellsForCageArray.add(recs.get(25));
        Cage cage11 = new Cage(6 , 'x' , cellsForCageArray , cellsNumber);
        allCages.add(cage11);
        recs.add(cage11.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(29); cellsNumber.add(35);
        cellsForCageArray.add(recs.get(29)); cellsForCageArray.add(recs.get(35));
        Cage cage12 = new Cage(9 , '+' , cellsForCageArray , cellsNumber);
        allCages.add(cage12);
        recs.add(cage12.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(30); cellsNumber.add(31); cellsNumber.add(32);
        cellsForCageArray.add(recs.get(30)); cellsForCageArray.add(recs.get(31)); cellsForCageArray.add(recs.get(32));
        Cage cage13 = new Cage(8 , '+' , cellsForCageArray , cellsNumber);
        allCages.add(cage13);
        recs.add(cage13.createCage());

        cellsNumber = new ArrayList<>();
        cellsForCageArray = new ArrayList<>();
        cellsNumber.add(33); cellsNumber.add(34);
        cellsForCageArray.add(recs.get(33)); cellsForCageArray.add(recs.get(34));
        Cage cage14 = new Cage(2 , '\u00F7' , cellsForCageArray , cellsNumber);
        allCages.add(cage14);
        recs.add(cage14.createCage());

        createCageBoard();
    }
    private void setFileDimension(){
        String line;
        String text , cells;
        int result , nr;
        char operand;
        int max = -1;
        try{
            line = getLine(readerDimension);
            while (line != null){
                text = line.split(" " )[0];
                if(text.length()!=1) {
                    operand = text.charAt(text.length() - 1);
                    result = Integer.parseInt(text.substring(0, text.length() - 1));
                }else{
                    result = Integer.parseInt(text);
                }
                cells = line.split(" ")[1];
                String[] splitter = cells.split(",");
                for (String s : splitter) {
                    nr = Integer.parseInt(s);
                    if (nr > max) {
                        max = nr;
                    }
                }
                line = getLine(readerDimension);

            }
            max = (int) Math.sqrt(max);
            dimension = max;

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void setInputDimension(String input){
        String text , cells;
        String[] lines = input.split("\\r?\\n");
        int result , nr , max=-1;
        char operand;
        for (int i = 0 ; i < lines.length ; i++){
            text = lines[i].split(" ")[0];
            if(text.length()!=1) {
                operand = text.charAt(text.length() - 1);
                result = Integer.parseInt(text.substring(0, text.length() - 1));
            }else{
                result = Integer.parseInt(text);
            }
            cells = lines[i].split(" ")[1];
            String[] splitter = cells.split(",");
            for (String s : splitter) {
                nr = Integer.parseInt(s);
                if (nr > max) {
                    max = nr;
                }
            }
        }
        max = (int) Math.sqrt(max);
        dimension = max;
    }


    public void createLabels(Shape shape,String text){
        Label label = new Label(text);
        label.relocate(shape.getBoundsInParent().getMinX()+4,shape.getBoundsInParent().getMinY()+1);
        label.setFont(new Font(15));
        labelArrayList.add(label);
        pane.getChildren().add(label);
    }
    private void displayLabels(){
        createLabels(recs.get(0) , "11+");
        createLabels(recs.get(1) ,"2÷");
        createLabels(recs.get(3) , "20x");
        createLabels(recs.get(4) , "6x");
        createLabels(recs.get(7) , "3-");
        createLabels(recs.get(10) , "3÷");
        createLabels(recs.get(12) , "240x");
        createLabels(recs.get(14) , "6x");
        createLabels(recs.get(20) , "6x");
        createLabels(recs.get(21) , "7+");
        createLabels(recs.get(22) , "30x");
        createLabels(recs.get(24) , "6x");
        createLabels(recs.get(29) , "9+");
        createLabels(recs.get(30) , "8+");
        createLabels(recs.get(33) , "2÷");
    }

    private void handleKeyPressEvent(){
        root.setOnKeyPressed(e -> {
            if(e.getCode().isDigitKey() && Integer.parseInt(e.getText()) != 0 && Integer.parseInt(e.getText()) <= dimension) {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    textMatrix[selectedRow][selectedColumn].setText(e.getText());
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(e.getText());
                    int newValue = Integer.parseInt(e.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(e.getText());
                    undoButton.setDisable(false);
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                } else {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setStroke(Color.BLACK);
                    text.setText(e.getText());
                    pane.getChildren().add(text);
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(e.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(e.getText());
                    int newValue = Integer.parseInt(e.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;
                }
            }else if(e.getCode() == KeyCode.BACK_SPACE){
                int oldValue = inputMatrix[selectedRow][selectedColumn];
                textMatrix[selectedRow][selectedColumn].setText("");
                cellArray[selectedRow*dimension+selectedColumn] = 0;
                inputMatrix[selectedRow][selectedColumn] = 0;
                int newValue = 0;
                StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                undoStack.push(stackObject);
                undoButton.setDisable(false);
                hintNumber--;
                hintButton.setDisable(false);
            }
        });
    }

    private void setInputButtonsAction(){
        number1.setOnAction(e -> {
            if (textMatrix[selectedRow][selectedColumn] != null) {
                int oldValue = inputMatrix[selectedRow][selectedColumn];
                textMatrix[selectedRow][selectedColumn].setText(number1.getText());
                textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number1.getText());
                cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number1.getText());
                int newValue = Integer.parseInt(number1.getText());
                StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                undoStack.push(stackObject);
                undoButton.setDisable(false);

            }else{
                Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                text.setFont(font);
                text.setText(number1.getText());
                text.setStroke(Color.BLACK);
                pane.getChildren().add(text);
                int oldValue = inputMatrix[selectedRow][selectedColumn];
                textMatrix[selectedRow][selectedColumn] = text;
                inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number1.getText());
                cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number1.getText());
                int newValue = Integer.parseInt(number1.getText());
                StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                undoStack.push(stackObject);
                undoButton.setDisable(false);
                hintNumber++;

            }
        });
        number2.setOnAction(e -> {
            if (textMatrix[selectedRow][selectedColumn] != null) {
                int oldValue = inputMatrix[selectedRow][selectedColumn];
                textMatrix[selectedRow][selectedColumn].setText(number2.getText());
                textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number2.getText());
                cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number2.getText());
                int newValue = Integer.parseInt(number2.getText());
                StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                undoStack.push(stackObject);
                undoButton.setDisable(false);

            }else{
                Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                text.setFont(font);
                text.setText(number2.getText());
                text.setStroke(Color.BLACK);
                pane.getChildren().add(text);
                int oldValue = inputMatrix[selectedRow][selectedColumn];
                textMatrix[selectedRow][selectedColumn] = text;
                inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number2.getText());
                cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number2.getText());
                int newValue = Integer.parseInt(number2.getText());
                StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                undoStack.push(stackObject);
                undoButton.setDisable(false);
                hintNumber++;

            }
        });
        if(dimension >= 3) {
            number3.setDisable(false);
            number3.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number3.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number3.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number3.getText());
                    int newValue = Integer.parseInt(number3.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number3.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number3.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number3.getText());
                    int newValue = Integer.parseInt(number3.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number3.setDisable(true);
        }
        if(dimension >= 4) {
            number4.setDisable(false);
            number4.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number4.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number4.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number4.getText());
                    int newValue = Integer.parseInt(number4.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number4.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number4.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number4.getText());
                    int newValue = Integer.parseInt(number4.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number4.setDisable(true);
        }
        if(dimension >= 5) {
            number5.setDisable(false);
            number5.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number5.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number5.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number5.getText());
                    int newValue = Integer.parseInt(number5.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number5.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number5.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number5.getText());
                    int newValue = Integer.parseInt(number5.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number5.setDisable(true);
        }
        if(dimension >= 6) {
            number6.setDisable(false);
            number6.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number6.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number6.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number6.getText());
                    int newValue = Integer.parseInt(number6.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number6.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number6.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number6.getText());
                    int newValue = Integer.parseInt(number6.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number6.setDisable(true);
        }
        if(dimension >= 7) {
            number7.setDisable(false);
            number7.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number7.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number7.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number7.getText());
                    int newValue = Integer.parseInt(number7.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number7.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number7.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number7.getText());
                    int newValue = Integer.parseInt(number7.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number7.setDisable(true);
        }
        if(dimension == 8) {
            number8.setDisable(false);
            number8.setOnAction(e -> {
                if (textMatrix[selectedRow][selectedColumn] != null) {
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn].setText(number8.getText());
                    textMatrix[selectedRow][selectedColumn].setStroke(Color.BLACK);
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number8.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number8.getText());
                    int newValue = Integer.parseInt(number8.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);

                } else {
                    Text text = new Text(selectedColumn * 75 + valueX, selectedRow * 75 + valueY, "");
                    text.setFont(font);
                    text.setText(number8.getText());
                    text.setStroke(Color.BLACK);
                    pane.getChildren().add(text);
                    int oldValue = inputMatrix[selectedRow][selectedColumn];
                    textMatrix[selectedRow][selectedColumn] = text;
                    inputMatrix[selectedRow][selectedColumn] = Integer.parseInt(number8.getText());
                    cellArray[selectedRow*dimension+selectedColumn] = Integer.parseInt(number8.getText());
                    int newValue = Integer.parseInt(number8.getText());
                    StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
                    undoStack.push(stackObject);
                    undoButton.setDisable(false);
                    hintNumber++;

                }
            });
        }else{
            number8.setDisable(true);
        }
        clear.setOnAction(e -> {
            int oldValue = inputMatrix[selectedRow][selectedColumn];
            textMatrix[selectedRow][selectedColumn].setText("");
            inputMatrix[selectedRow][selectedColumn] = 0;
            int newValue = 0;
            StackObject stackObject = new StackObject(oldValue , newValue , selectedRow , selectedColumn);
            undoStack.push(stackObject);
            undoButton.setDisable(false);
            hintNumber--;
            hintButton.setDisable(false);
        });

    }
    private void handleClearButton(){
        clearButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION , "Are you sure you want to clear the board?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                hintButton.setDisable(false);
                for(int i = 0 ; i < textMatrix.length ; i++){
                    for(int j = 0 ; j < textMatrix.length ; j++){
                        if(textMatrix[i][j] != null){
                            textMatrix[i][j].setText("");
                            textMatrix[i][j].setStroke(Color.BLACK);
                            textMatrix[i][j] = null;

                        }
                        if(rectangles[i][j].getFill() != Color.WHITE){
                            rectangles[i][j].setFill(Color.WHITE);
                        }
                        int oldValue = inputMatrix[i][j];
                        inputMatrix[i][j] = 0;
                        cellArray[i * dimension + j] = 0;
                        int newValue = 0;
                        StackObject stackObject = new StackObject(oldValue , newValue , i , j);
                        undoStack.push(stackObject);
                    }
                }
                hintNumber = 0;
                undoButton.setDisable(true);
                redoButton.setDisable(true);
            }
        });
    }


    public void Mistakes() {
        boolean worked = false;
        Cage[] cageArray = allCages.toArray(new Cage[0]);
        Solver solver = new Solver(dimension , cageArray , cageBoard);
        solver.initialize();
        solvedBoard = new int[dimension+1][dimension+1];
        if(solver.solve()){
            solvedBoard = solver.getBoard();
            worked = true;
        }else{
            System.out.println("OOPS");
        }
        if(worked){
            for (int row = 0; row < inputMatrix.length; row++) {
                for (int col = 0; col < inputMatrix.length; col++) {
                    int gameValue = inputMatrix[row][col];
                    int solvedValue = solvedBoard[row][col];

                    if (gameValue == solvedValue) {
                        rectangles[row][col].setFill(Color.GREEN);
                    } else {
                        rectangles[row][col].setFill(Color.RED);
                    }
                }
            }
        }
    }



    private String getLine(BufferedReader reader){
        String line = null;
        try{
            line = reader.readLine();
        }catch(Exception e){
            e.printStackTrace();
        }
        return line;
    }

    private boolean checkCorrectFile(BufferedReader file){
        int  i;
        boolean isValid = true;
        String cells  , line;
        int[] oneCageCell = new int[dimension*dimension];
        for(i = 0 ; i < oneCageCell.length ; i++){
            oneCageCell[i] = 0;
        }
        int[] cellsWithinCage , checker;
        try{
            line = getLine(file);
            while(line != null){
                cells = line.split(" ")[1];
                if(cells.length()!=1) {
                    String[] splitter = cells.split(",");
                    cellsWithinCage = new int[splitter.length];
                    checker = new int[splitter.length];
                    for (i = 0; i < checker.length; i++) {
                        checker[i] = 0;
                    }
                    for (i = 0; i < splitter.length; i++) {
                        oneCageCell[Integer.parseInt(splitter[i]) - 1]++;
                        cellsWithinCage[i] = Integer.parseInt(splitter[i]);
                    }
                    checker[0] = 1;
                    Arrays.sort(cellsWithinCage);
                    for (i = 0; i < cellsWithinCage.length - 1; i++) {
                        for (int j = 1; j < cellsWithinCage.length; j++) {
                            if (cellsWithinCage[j] == cellsWithinCage[i] + 1 || cellsWithinCage[j] == cellsWithinCage[i] + dimension || (j+1 < cellsWithinCage.length && cellsWithinCage[j] == cellsWithinCage[j+1] - 1)){
                                checker[j] = 1;
                            }
                        }
                    }
                    for (i = 0; i < checker.length; i++) {
                        if (checker[i] == 0) {
                            isValid = false;
                            break;
                        }
                    }
                }else{
                    oneCageCell[Integer.parseInt(cells) - 1]++;
                }
                line = getLine(file);
                if(!isValid){
                    break;
                }
            }
            for(i = 0 ; i < oneCageCell.length ; i++){
                if(oneCageCell[i] == 0 || oneCageCell[i] > 1){
                    isValid = false;
                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isValid;
    }

    private boolean checkCorrectInput(String input){
        int  i;
        String[] lines = input.split("\\r?\\n");
        boolean isValid = true;
        String cells  , line;
        int[] oneCageCell = new int[dimension*dimension];
        for(i = 0 ; i < oneCageCell.length ; i++){
            oneCageCell[i] = 0;
        }
        int[] cellsWithinCage , checker;
            for(int k = 0 ; k < lines.length ; k++){
                cells = lines[k].split(" ")[1];
                if(cells.length()!=1) {
                    String[] splitter = cells.split(",");
                    cellsWithinCage = new int[splitter.length];
                    checker = new int[splitter.length];
                    for (i = 0; i < checker.length; i++) {
                        checker[i] = 0;
                    }
                    for (i = 0; i < splitter.length; i++) {
                        oneCageCell[Integer.parseInt(splitter[i]) - 1]++;
                        cellsWithinCage[i] = Integer.parseInt(splitter[i]);
                    }
                    checker[0] = 1;
                    Arrays.sort(cellsWithinCage);
                    for (i = 0; i < cellsWithinCage.length - 1; i++) {
                        for (int j = 1; j < cellsWithinCage.length; j++) {
                            if (cellsWithinCage[j] == cellsWithinCage[i] + 1 || cellsWithinCage[j] == cellsWithinCage[i] + dimension || (j+1 < cellsWithinCage.length && cellsWithinCage[j] == cellsWithinCage[j+1] - 1)) {
                                checker[j] = 1;
                            }
                        }
                    }

                    for (i = 0; i < checker.length; i++) {
                        if (checker[i] == 0) {
                            isValid = false;
                            break;
                        }
                    }
                }else {
                    oneCageCell[Integer.parseInt(cells) - 1]++;
                }
                if(!isValid){
                    break;
                }
            }
            for(i = 0 ; i < oneCageCell.length ; i++){
                if(oneCageCell[i] == 0 || oneCageCell[i] > 1){
                    isValid = false;
                    break;
                }
            }

        return isValid;
    }
    private void createCageBoard(){
        ArrayList<Integer> cells;
        cageBoard = new int[dimension][dimension];
        for(int index = 0 ; index < allCages.size() ; index++){
            cells = allCages.get(index).getCellsNumber();
            for(int k = 0 ; k < cells.size() ; k++){
                for(int r = 0 ; r < dimension ; r++){
                    for(int c = 0 ; c < dimension ; c++){
                        if(r * dimension + c == cells.get(k)){
                            cageBoard[r][c] = index + 1;
                        }
                    }
                }
            }
        }
    }
}
