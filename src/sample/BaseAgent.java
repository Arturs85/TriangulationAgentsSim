package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class BaseAgent {
double x=92;
double y=50;
float targetX;
float targetY;
float targetDirection;

static final int radius = 10;
Canvas canvas;
double direction;
double scale =1;
Space2D space2D;
MovementState movementState = MovementState.STILL;

static double nominalSpeed = 3; //px per iteration
    static double nominalAngularSpeed = 6*Math.PI/180; //rad per iteration

BaseAgent(Space2D space2D){
    this.space2D = space2D;
    canvas = Main.canvas;
}
void turn(double rad){
    direction+=rad;
}
void moveForward(int dist){
  double xNew = x + dist*Math.cos(direction);
  double yNew = y + dist*Math.sin(direction);

  if(xNew-radius<0||xNew+radius>space2D.width||yNew-radius<0||yNew+radius>space2D.height){//hitted boundry of space, bounce back
      direction = direction+Math.PI;
  }
x=xNew;
y=yNew;
}
//    void moveForwardOneTick(double speed){
//        x+=dist*Math.cos(direction);
//        y+=dist*Math.sin(direction);
//
//    }

void movementStep(){
  if(movementState.equals(MovementState.FORWARD)){
      moveForward((int)nominalSpeed);
  }else if(movementState.equals(MovementState.TURNING_LEFT)){
      turn(-nominalAngularSpeed);
  }else if(movementState.equals(MovementState.TURNING_RIGHT)){
      turn(+nominalAngularSpeed);
  }

}

void draw(){
   GraphicsContext g = canvas.getGraphicsContext2D();
g.strokeOval(Space2D.cellsOffset+(x-radius)*scale,Space2D.cellsOffset+(y-radius)*scale,radius*2*scale,radius*2*scale);
g.strokeLine(Space2D.cellsOffset+x*scale,Space2D.cellsOffset+y*scale,Space2D.cellsOffset+scale* (x+radius*Math.cos(direction)),Space2D.cellsOffset+scale* (y+radius*Math.sin(direction)));
}


}
