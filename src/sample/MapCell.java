package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;

public class MapCell {
    static int size = 80;
    static int scale = 1;
    static double MAX_ERR_PERCENT = 1;
    static double MAX_ERR_PERCENT_FIRST_STEP = 230;

    double reward = -0.04;
    double terminalValue = 1;
    boolean isObstacle = false;
    boolean isTerminal = false;
    boolean isStartPosition = false;
    boolean isSelected = false;
    double curUtilityValue = 0;
    double nextUtilityValue = 0;

    int x;//numbering
    int y;
    Point position;//drawing
    int cellNr;
    Space2D board;
    String availableDirectionsString;

    MapCell(int x, int y, Space2D board) {
        this.board = board;
        this.x = x;//redundant - also present in board
        this.y = y;
        this.cellNr = x + y * board.width + 1;
        position = new Point(x * MapCell.size + Space2D.cellsOffset, y * MapCell.size + Space2D.cellsOffset);
    }

    void setAsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
        if (isTerminal)
            curUtilityValue = terminalValue;
        else curUtilityValue = 0;
    }

    //use only after next u value calculation
    //returns true if values match
    boolean updateValue() {
        boolean ret;

        if (Math.abs(curUtilityValue - nextUtilityValue) < Math.abs(curUtilityValue) * MAX_ERR_PERCENT / 100)
            ret = true;
        else {
            ret = false;
            // System.out.println(" no match cell: x: "+x+" y: "+y);
        }
        curUtilityValue = nextUtilityValue;
        return ret;

    }

    boolean updateValueFirstStep() {
        boolean ret;

        if (Math.abs(curUtilityValue - nextUtilityValue) < Math.abs(curUtilityValue) * MAX_ERR_PERCENT_FIRST_STEP / 100)
            ret = true;
        else {
            ret = false;
            // System.out.println(" no match cell: x: "+x+" y: "+y);
        }
        curUtilityValue = nextUtilityValue;
        return ret;

    }






    void draw(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();

//draw border;
        if (!isSelected)
            g.setStroke(Color.DARKGRAY);
        else
            g.setStroke(Color.RED);

        g.setLineWidth(2);
        g.strokeRect(scale * position.x, scale * position.y, scale * size, scale * size);

        g.setStroke(Color.DARKGRAY);
        g.setFill(Color.DARKGRAY);

        //avail dir
        g.setLineWidth(1);

        if (!isObstacle && !isTerminal) {
            //g.strokeText(composeAvailableDirectionsString(), position.x + 5, position.y + 20);
            String uVal = String.format("%.4f", curUtilityValue);
            g.strokeText("U: " + uVal, position.x + 5, position.y + 35);

        }

        //obstacle

        if (isObstacle) {
            g.fillRect(position.x, position.y, size, size);
        }
        if (isStartPosition) {
            g.strokeText("S", position.x + 5, position.y + 50);
        }
        if (isTerminal) {
            g.strokeText("T " + terminalValue, position.x + 25, position.y + 50);
        }


    }


}
