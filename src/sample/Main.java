package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {
    Space2D board = new Space2D(400, 300);
    PublicPartOfAgent testAgent;
    Simulation simulation = new Simulation(board);
boolean obstDrawingStarted = false;
double firstX;
double firstY;
static Canvas canvas = new Canvas(2500, 2500);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        ScrollPane scrollPane = new ScrollPane(canvas);
        BorderPane root = new BorderPane(scrollPane);
        board.draw(canvas);
        testAgent = simulation.createAgent();
         simulation.createAgent();
        //simulation.agents.add(testAgent);
        // PASimulator simulator = new PASimulator(board);
        // PolicyIterator policyIterator = new PolicyIterator(board);
        Button obstButton = new Button("Set Obstacle");
        Button terminalPositionButton = new Button("Set Terminal");
        Button startPositionButton = new Button("Set Start Position");
        Button iterationButton = new Button("Run PI Algorithm");
        Button makeMoveButton = new Button("Move");
        Button stopMoveButton = new Button("Stop");

        obstButton.setPrefWidth(130);
        terminalPositionButton.setPrefWidth(130);
        startPositionButton.setPrefWidth(130);
        iterationButton.setPrefWidth(130);
        makeMoveButton.setPrefWidth(130);

        Button xPlus = new Button("X+");
        Button xMinus = new Button("X-");
        Button yPlus = new Button("Y+");
        Button yMinus = new Button("Y-");

        Button turnRightButton = new Button(">>");
        Button turnLeftButton = new Button("<<");

        HBox adjustmentPanel = new HBox(5, xPlus, xMinus, yPlus, yMinus);
        HBox robotControlPanel = new HBox(turnLeftButton, makeMoveButton,stopMoveButton, turnRightButton);
        xPlus.setOnAction(event -> {
            board.setWidth(board.width + 1);
            redrawBoard();
        });
        xMinus.setOnAction(event -> {
            board.setWidth(board.width - 1);
            redrawBoard();
        });
        yPlus.setOnAction(event -> {
            board.setHeight(board.height + 1);
            redrawBoard();
        });
        yMinus.setOnAction(event -> {
            board.setHeight(board.height - 1);
            redrawBoard();
        });

        turnLeftButton.setOnAction(event -> {
            testAgent.movementState=MovementState.TURNING_LEFT;
        });
        turnRightButton.setOnAction(event -> {
            testAgent.movementState=MovementState.TURNING_RIGHT;
        });
stopMoveButton.setOnAction(event -> {
    testAgent.movementState=MovementState.STILL;
});
        canvas.setOnMouseClicked(event -> {
          if(!obstDrawingStarted) {
          firstX = event.getX()-Space2D.cellsOffset;
          firstY = event.getY()-Space2D.cellsOffset;
              obstDrawingStarted=true;
                    }
                    else{
              board.obstacles.add(new Rectangle(firstX,firstY,event.getX()-Space2D.cellsOffset-firstX,event.getY()-Space2D.cellsOffset-firstY));
              obstDrawingStarted=false;
          }
            board.draw();
        });


        iterationButton.setOnAction(event -> {
            long start = System.nanoTime();
            //   int itCount = policyIterator.runPolicyIterations();
            long end = System.nanoTime();
            end = end - start;
            // System.out.println("No of iterations: " + itCount + " time: " + end + " ns");
            //policyIterator.iteration();
            board.draw(canvas);

        });
        makeMoveButton.setOnAction(event -> {
testAgent.movementState = MovementState.FORWARD;

        });
        obstButton.setOnAction(event -> {
            if (board.selectedCell != null) {
                if (!board.selectedCell.isObstacle) {
                    board.selectedCell.isObstacle = true;
                } else {
                    board.selectedCell.isObstacle = false;
                }
                redrawObstButton(obstButton, board.selectedCell.isObstacle);
                //  System.out.println(MotionModel.getRelativeDirection());
            }


            //     if (simulator != null)
            //         simulator.draw();
            //     else
            board.draw();
        });
        terminalPositionButton.setOnAction(event -> {
            if (board.selectedCell != null && !board.selectedCell.isObstacle) {
                if (!board.selectedCell.isTerminal) {
                    if (board.terminalCount % 2 == 0)
                        board.selectedCell.terminalValue = 1;
                    else
                        board.selectedCell.terminalValue = -1;
                    board.selectedCell.setAsTerminal(true);
                    board.terminalCount++;
                } else {
                    board.selectedCell.setAsTerminal(false);
                    board.terminalCount--;
                }

                redrawObstButton(obstButton, board.selectedCell.isObstacle);

            }
            //       if (simulator != null)
            //          simulator.draw();
            ///    else
            board.draw();
        });

        startPositionButton.setOnAction(event -> {

        });
        //  Button resetButton = new Button("Default");
        Slider sliderSimSpeed = new Slider();
        sliderSimSpeed.setTooltip(new Tooltip("Sim Speed adjustment"));
        sliderSimSpeed.valueProperty().addListener((observable, oldValue, newValue) -> {
            //   System.out.println("Speed slider value changed "+ newValue);
            //      simulator.timeline.setRate(newValue.intValue());//setDelay(Duration.millis(simulation.simStepDefDuration/newValue.intValue()));

        });


        VBox buttonBar = new VBox(20, adjustmentPanel, obstButton, terminalPositionButton, startPositionButton, iterationButton, robotControlPanel, sliderSimSpeed);
        buttonBar.setPadding(new Insets(5, 5, 5, 5));
        buttonBar.setAlignment(Pos.CENTER);

        root.setRight(buttonBar);


        primaryStage.setTitle("Particle localisation");
        primaryStage.setScene(new Scene(root, 650, 575));
        primaryStage.show();
    }

    void redrawObstButton(Button b, boolean isObstacle) {
        if (isObstacle)
            b.setText("Clear obstacle");
        else
            b.setText("Set obstacle");

    }

    void redrawBoard() {

        board.draw();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
