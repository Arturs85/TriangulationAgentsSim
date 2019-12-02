package sample.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sample.BaseUWBAgent;
import sample.MovementState;

import java.util.Random;

public class RandomRoamingBehaviour extends TickerBehaviour {
   Random rnd = new Random();
   BaseUWBAgent owner;
   public RandomRoamingBehaviour(BaseUWBAgent a, long period) {
        super(a, period);
        owner = a;
    }

    @Override
    protected void onTick() {
     float rand =rnd.nextFloat();
     if(rand>0.95 && rand<0.97 )owner.publicPartOfAgent.movementState = MovementState.TURNING_LEFT;
 else
     if(rand>0.97 && rand<0.99 )owner.publicPartOfAgent.movementState = MovementState.TURNING_RIGHT;
     else
     if(rand>0.9 && rand<0.95 )owner.publicPartOfAgent.movementState = MovementState.STILL;
     else
     if(rand>0.85 && rand<0.9 )owner.publicPartOfAgent.movementState = MovementState.FORWARD;

    }
}
