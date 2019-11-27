package sample;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    public Controller controller;
    static int agentNumber = 0;
    int simStepDefDuration = 50;
    Space2D board;
    int simTimeFactor = 1;
    Timeline timeline;
    int simTime = 0;
    boolean isRunning = false;
    ContainerController cc;
    AID[] topics = new AID[5];
    private final int SIM_TIME_LIMIT=2001;

    //kārtas skaits ievietošanai jaunā aģenta vārdā
    int getAgentNumber() {
     return ++agentNumber;
    }

public List<BaseAgent> agents=new ArrayList<>(10);

    Simulation(Space2D board){
        this.board=board;
        timeline = new Timeline(new KeyFrame(Duration.millis(simStepDefDuration), ae -> simulationStep()));
        timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    }

    synchronized void simulationStep() {
        simTime++;
        board.draw();
        for (BaseAgent a : agents) {
            a.movementStep();
        a.draw();
        }
    }



    void createGUIAgent() {
        if (cc != null) {
            // Create a new agent, a DummyAgent
// and pass it a reference to an Object
            Object reference = new Object();
            Object args[] = new Object[]{reference, controller};

            try {
                AgentController dummy = cc.createNewAgent("guiAgent",
                        "sample.GUIAgent", args);
// Fire up the agent
                dummy.start();

                //agents.add(new AgentInfo(dummy.getName(), simTime, dummy,new int[]{10,20,3}));
            } catch (Exception e) {

            }
        }
    }







    void restart(){
        simTime=0;
    }




}
