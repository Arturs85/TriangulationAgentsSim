package sample;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
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
    int simStepDefDuration = 50;
    Space2D board;
    int simTimeFactor = 1;
    Timeline timeline;
    int simTime = 0;
    boolean isRunning = false;
    ContainerController cc;
    AID[] topics = new AID[5];
    private final int SIM_TIME_LIMIT=2001;
public List<PublicPartOfAgent> publicPartsOfAgents = new ArrayList<>();


//public List<PublicPartOfAgent> agents=new ArrayList<>(10);

    Simulation(Space2D board){
        this.board=board;
    cc= startJade();
        timeline = new Timeline(new KeyFrame(Duration.millis(simStepDefDuration), ae -> simulationStep()));
        timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    }

    synchronized void simulationStep() {
        simTime++;
        board.draw();
        for (PublicPartOfAgent a : publicPartsOfAgents) {
            a.movementStep();
        a.draw();
        }
    }

    ContainerController startJade() {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        rt.invokeOnTermination(new Runnable() {
            @Override
            public void run() {
                System.out.println(getClass().getName() + " runtimeTermination");
            }
        });
        // Create a default profile
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        p.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        //p.setParameter(Profile.SERVICES,"TopicManagement");
        // Create a new non-main container, connecting to the default
// main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createMainContainer(p);
        return cc;
    }

    /**
     * use to create new agents and get their public parts to acces their data
     * @return  public part of agent
     */
    PublicPartOfAgent createAgent(){
        PublicPartOfAgent ppa = new PublicPartOfAgent(board,this);

        if(createAgent(ppa))
            return ppa;
        else
            return null;
    }
   private boolean createAgent(PublicPartOfAgent publicPartOfAgent) {
        if (cc != null) {
            Object reference = new Object();
            Object args[] = {publicPartOfAgent};

            try {
                AgentController dummy = cc.createNewAgent(publicPartOfAgent.agentName,
                        "sample.BaseUWBAgent", args);
                dummy.start();
            } catch (Exception e) {
                e.printStackTrace();
            return false;
            }
publicPartsOfAgents.add(publicPartOfAgent);
        return true;
        }
    return false;
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
