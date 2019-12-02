package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public class PublicPartOfAgent {
    double x = 92;
    double y = 50;
    float targetX;
    float targetY;
    float targetDirection;
    double odometryTotal = 0;

    static final int radius = 10;
    Canvas canvas;
    double direction;
    double moveDistanceRemaining = 0; // for seperate moves e.g. moveForward
    static double scale = 1;
    Space2D space2D;
    Simulation simulation;
    public MovementState movementState = MovementState.STILL;
    public MovementMode movementMode = MovementMode.MANUAL;//default

    public String agentName;
    public int agentNumber;
    static int agentNumberCounter = 0;
    static double nominalSpeed = 3; //px per iteration
    static double nominalAngularSpeed = 6 * Math.PI / 180; //rad per iteration

    PublicPartOfAgent(Space2D space2D, Simulation simulation) {
        this.space2D = space2D;
        this.simulation = simulation;
        canvas = Main.canvas;
        agentNumber = generateAgentNumber();
        agentName = "UWBAgent " + agentNumber;
    }

    //kārtas skaits ievietošanai jaunā aģenta vārdā
    int generateAgentNumber() {
        return ++agentNumberCounter;
    }

    void turn(double rad) {
        direction += rad;
    }

    void moveForward(int dist) {
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
     * @param distance is reached
     */
  public   void moveForwardBy(double distance) {
        movementMode = MovementMode.TO_TARGET;
        moveDistanceRemaining = distance;
    }

    void manualMovementStep() {

        if (movementState.equals(MovementState.FORWARD)) {
            moveForward((int) nominalSpeed);
        } else if (movementState.equals(MovementState.TURNING_LEFT)) {
            turn(-nominalAngularSpeed);
        } else if (movementState.equals(MovementState.TURNING_RIGHT)) {
            turn(+nominalAngularSpeed);
        }

    }

    void movementStep() {

        if (movementMode.equals(MovementMode.TO_TARGET)) {
            moveForward((int) nominalSpeed);
            moveDistanceRemaining -= nominalSpeed;
            if (moveDistanceRemaining <= 0) {
                movementMode = MovementMode.MANUAL;// target distance reached -> switch off to target mode
            }
        } else if (movementMode.equals(MovementMode.MANUAL)) {//
            manualMovementStep();

        }

    }

    public double measureDistance(String otherAgentName) throws Exception {
        for (PublicPartOfAgent ppa : simulation.publicPartsOfAgents) {
            if (otherAgentName.contains(ppa.agentName)) {
                return measureDistance(ppa);
            }
        }
        throw new Exception("No agent with given name found");
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

    void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.strokeOval(Space2D.cellsOffset + (x - radius) * scale, Space2D.cellsOffset + (y - radius) * scale, radius * 2 * scale, radius * 2 * scale);
        g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + radius * Math.cos(direction)), Space2D.cellsOffset + scale * (y + radius * Math.sin(direction)));
        g.strokeText(String.valueOf(odometryTotal), Space2D.cellsOffset + x * scale, Space2D.cellsOffset + (y - radius) * scale);
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
