package sample;

import jade.core.AID;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import sample.Behaviours.BaseTopicBasedTickerBehaviour;
import sample.Behaviours.ExplorerPositionMsg;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class PublicPartOfAgent {
    public double x = 92;
    public double y = 120;
  public   int battLevel = 97;//
    float targetX;
    float targetY;
    float targetDirection;
  public   double odometryTotal = 0;
Particle ownRelativePosition;
    public static final int radius = 10;
    public Canvas canvas;
  public   double direction;
    double moveDistanceRemaining = 0; // for seperate moves e.g. moveForward
    double angleRemaining = 0; // for seperate moves e.g. turnRight

    public static double scale = 1;
    Space2D space2D;
    public Simulation simulation;
    public MovementState movementState = MovementState.STILL;
    public volatile MovementMode movementMode = MovementMode.MANUAL;//default

    public String agentName;
    public int agentNumber;
    static int agentNumberCounter = 0;
    static double nominalSpeed = 1; //px per iteration
    static double nominalAngularSpeed = 1 * Math.PI / 180; //rad per iteration

    public double angleToOtherRobot = 0;
    public Double angleToSecond =null;
    double otherRobotLineDistance = 100;//for drawing
BaseUWBAgent owner;

    PublicPartOfAgent(Space2D space2D, Simulation simulation) {
        this.space2D = space2D;
        this.simulation = simulation;
        canvas = Main.canvas;
        agentNumber = generateAgentNumber();
        agentName = "UWBAgent " + agentNumber;
    ownRelativePosition=new Particle(0,0,0);
    }

    //kārtas skaits ievietošanai jaunā aģenta vārdā
    int generateAgentNumber() {
        return ++agentNumberCounter;
    }

    void turn(double rad) {
        direction += rad;
    }

    void moveForward(double dist) {
        double xNew = x + dist * Math.cos(direction);
        double yNew = y + dist * Math.sin(direction);

        if ((xNew - radius <= 0 || xNew + radius >= space2D.width || yNew - radius <= 0 || yNew + radius >= space2D.height) || hasHitObstacle(xNew, yNew)) {//hitted boundry of space, bounce back
            direction = direction + Math.PI;
            return;
        }
        x = xNew;
        y = yNew;
        odometryTotal += dist;
    }

    //    void moveForwardOneTick(double speed){
//        x+=dist*Math.cos(direction);
//        y+=dist*Math.sin(direction);
//
//    }
    int getAgentNumberByName(String agentName) {
        String intValue = agentName.replaceAll("[^0-9]", "");
        return Integer.parseInt(intValue);
    }

    /**
     * changes movement mode to to_target so that robot keeps moving at its speed every iteration until
     *
     * @param distance is reached
     */
    public void moveForwardBy(double distance) {
        movementMode = MovementMode.TO_TARGET;
        moveDistanceRemaining = distance;
    }

    public void turnRightBy(double angleRadians) {
        movementMode = MovementMode.TO_ANGLE;
        angleRemaining = Math.abs( angleRadians);

    }

    void manualMovementStep() {

        if (movementState.equals(MovementState.FORWARD)) {
            moveForward( nominalSpeed);
        } else if (movementState.equals(MovementState.TURNING_LEFT)) {
            turn(-nominalAngularSpeed);
        } else if (movementState.equals(MovementState.TURNING_RIGHT)) {
            turn(nominalAngularSpeed);
        }

    }

    void movementStep() {

        if (movementMode.equals(MovementMode.TO_TARGET)) {
            moveForward(nominalSpeed);
            moveDistanceRemaining -= nominalSpeed;
            if (moveDistanceRemaining <= 0) {
                movementMode = MovementMode.MANUAL;// target distance reached -> switch off to target mode
            }
        } else if (movementMode.equals(MovementMode.TO_ANGLE)) {
            turn(+nominalAngularSpeed);
            angleRemaining-=nominalAngularSpeed;
            if (angleRemaining < nominalAngularSpeed*2)
                movementMode = MovementMode.MANUAL;// target distance reached -> switch off to target mode

        } else if (movementMode.equals(MovementMode.MANUAL)) {//
            manualMovementStep();

        }

    }

    public double measureDistance(String otherAgentName){// throws Exception {
        for (PublicPartOfAgent ppa : simulation.publicPartsOfAgents) {
            if (otherAgentName.contains(ppa.agentName)) {
                return measureDistance(ppa);
            }
        }
        //throw new Exception("No agent with given name found");
    return 0;
    }

    public double measureDistance(PublicPartOfAgent otherAgent) {
        double dx = x - otherAgent.x;
        double dy = y - otherAgent.y;

        return Math.sqrt(dx * dx + dy * dy);//optionally add measurement error
    }

    /**
     * @param mes1    first distance measurement between robots
     * @param mes2    second distance measurement between robots after this robot moved in stright direction
     * @param odoDist estimated distance of this robot movement between interrobot distance measurements
     * @return aprox angle in radians of the other robot
     */
    public double calculateRelativeAngle(double mes1, double mes2, double odoDist) {
        if ((mes2 + odoDist) < mes1) return 0;
        if ((mes1 + odoDist) < mes2) return Math.PI;

        double angle = Math.acos((odoDist * odoDist + mes2 * mes2 - mes1 * mes1) / (2 * odoDist * mes2));
        return Math.PI - angle;
    }
    public static double  calcThirdSide(double a,double b,double angleRad){
return Math.sqrt(a*a+b*b-2*a*b*Math.cos(angleRad));
    }
//returns angle oposite to c
    public static double  calcAngle(double a,double b,double c){//cos theorem
        return Math.acos((a*a+b*b-c*c)/(2*a*b));
    }

    void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setStroke(Color.BLACK);

        g.strokeOval(Space2D.cellsOffset + (x - radius) * scale, Space2D.cellsOffset + (y - radius) * scale, radius * 2 * scale, radius * 2 * scale);
        g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + radius * Math.cos(direction)), Space2D.cellsOffset + scale * (y + radius * Math.sin(direction)));
        g.strokeText(String.valueOf(odometryTotal), Space2D.cellsOffset + x * scale, Space2D.cellsOffset + (y - radius) * scale);

        if (angleToOtherRobot != 0) {//needs beter test- 0 can be valid angle
            g.setStroke(Color.GREEN);
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(angleToOtherRobot+direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(angleToOtherRobot+direction)));
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(-angleToOtherRobot+direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(-angleToOtherRobot+direction)));

        }
        if(angleToSecond!=null){
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(angleToSecond+direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(angleToSecond+direction)));

        }
int rowHeight = 20;
        ArrayList<BaseTopicBasedTickerBehaviour> b= new ArrayList<>(owner.behaviours);
        StringBuilder sb=new StringBuilder();

        for (int i = 0; i < b.size(); i++) {

 sb.append(b.get(i).shortName);
 sb .append(" ");

        }
        g.strokeText(sb.toString(), Space2D.cellsOffset + owner.publicPartOfAgent.x * scale+rowHeight, Space2D.cellsOffset + (owner.publicPartOfAgent.y - owner.publicPartOfAgent.radius + rowHeight) * scale);

    }

    boolean hasHitObstacle(double x, double y) {
        for (Rectangle r : space2D.obstacles) {
            if (x + radius > r.getX() && x - radius < (r.getX() + r.getWidth()) && y + radius > r.getY() && y - radius < (r.getY() + r.getHeight())) {//hitted boundry of space, bounce back
                return true;
            }
        }
        return false;
    }

}
