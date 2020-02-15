package sample.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sample.BaseUWBAgent;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

import static sample.Behaviours.MasterStates.*;

public class GrandMasterBehaviour extends BaseTopicBasedTickerBehaviour {
    MasterStates state = INITIAL_STATE;
    static final int waitReplysTimeout = 5000;//ms
    static final int minReplysToReceive = 3;// wait  at least this number   of replays to go on with beacon formation
    int cyclesCounter = 0;
    Map<AID, Integer> receivedInfo = new TreeMap<>();
    Map<AID, ArrayList<ExplorerPositionMsg>> receivedMaps = new TreeMap<>();
    Canvas canvas;

    public GrandMasterBehaviour(BaseUWBAgent a) {
        super(a);
        createSendingTopic(TopicNames.grandMasterToAll);
        createAndRegisterReceivingTopics(TopicNames.potentialBeaconsToGMaster);
        createAndRegisterReceivingTopics(TopicNames.bmToGm);
        createAndRegisterReceivingTopics(TopicNames.explorerToGM);
        shortName = "GMB";
        showPlotWindow();
    }

    @Override
    protected void onTick() {
        switch (state) {
            case INITIAL_STATE:
                //send battery level request to all robots
                sendBatLevelRequest();
                enterState(GATHERING_AGENTS_INFO);
                break;

            case GATHERING_AGENTS_INFO:
                cyclesCounter--;
                receiveValuesFromPotentialBeacons();
                if (cyclesCounter <= 0)
                    if (receivedInfo.size() < minReplysToReceive) {
                        enterState(INITIAL_STATE);// try again sending out bat level request
                        System.out.println("not enough replays: " + receivedInfo.size());
                    } else {
                        informNewBeacons();
                        enterState(WAITING_BEACON_FORMATION);
                    }
                break;
            case WAITING_BEACON_FORMATION:
                if (receiveFormationComplete())
                    enterState(RECEIVING_MAP_INFO);
                break;
            case RECEIVING_MAP_INFO:
                receivePositionInfo();
                drawMapOnMainThread();
                break;

            default:
                break;
        }
    }

    void sendBatLevelRequest() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(sendingTopics[TopicNames.grandMasterToAll.ordinal()]);
        owner.send(msg);
    }

    void sendBecomeBeaconRequest(AID agent, int hierarchyLevel) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology(UWBOntologies.carryOutBeaconRole.name());
        try {
            msg.setContentObject(hierarchyLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(agent);
        owner.send(msg);
    }

    void receivePositionInfo() {
        ACLMessage msg = owner.receive(templates[TopicNames.explorerToGM.ordinal()]);
        //process all messages of this topic
        while (msg != null) {

            if (msg.getPerformative() == ACLMessage.INFORM) {

                try {

                    ExplorerPositionMsg pos = (ExplorerPositionMsg) msg.getContentObject();
                    addPosition(msg.getSender(), pos);
                    //   System.out.println("====added new point========");
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }

            msg = owner.receive(templates[TopicNames.explorerToGM.ordinal()]);
        }
    }

    void addPosition(AID sender, ExplorerPositionMsg pos) {
        if (!receivedMaps.containsKey(sender)) {
            ArrayList l = new ArrayList<ExplorerPositionMsg>(200);
            l.add(pos);
            receivedMaps.put(sender, l);
        } else {
            receivedMaps.get(sender).add(pos);
        }
    }

    boolean receiveFormationComplete() {
        ACLMessage msg = owner.receive(templates[TopicNames.bmToGm.ordinal()]);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                System.out.println(getBehaviourName() + " received Formation complete");
                return true;
            }
        }
        return false;
    }

    void receiveValuesFromPotentialBeacons() {
        ACLMessage msg = owner.receive(templates[TopicNames.potentialBeaconsToGMaster.ordinal()]);
        //process all messages of this topic
        while (msg != null) {

            Integer value = null;

            try {
                value = (Integer) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            receivedInfo.put(msg.getSender(), value);
            msg = owner.receive(templates[TopicNames.potentialBeaconsToGMaster.ordinal()]);
        }
    }

    ArrayList<Map.Entry<AID, Integer>> listOfSortedBeaconCandidates() {
        ArrayList<Map.Entry<AID, Integer>> entryList = new ArrayList<>(receivedInfo.entrySet());
        Collections.sort(entryList, new ScoreComparator<>());
        return entryList;
    }

    void informNewBeacons() {
        ArrayList<Map.Entry<AID, Integer>> sortedEntryList = listOfSortedBeaconCandidates();
        for (int level = 0; level < 3; level++) {
            AID beaconName = sortedEntryList.get(level).getKey();
            sendBecomeBeaconRequest(beaconName, level);

        }
    }

    class ScoreComparator<V extends Comparable<V>>
            implements Comparator<Map.Entry<?, V>> {
        public int compare(Map.Entry<?, V> o1, Map.Entry<?, V> o2) {

            // Call compareTo() on V, which is known to be a Comparable<V>
            return o1.getValue().compareTo(o2.getValue());
        }
    }

    ;


    /**
     * use this to switch  betwwen states. it initializes related variables
     *
     * @param nextState
     */
    void enterState(MasterStates nextState) {
        switch (nextState) {
            case GATHERING_AGENTS_INFO:
                state = GATHERING_AGENTS_INFO;
                cyclesCounter = waitReplysTimeout / BaseUWBAgent.tickerPeriod;
                receivedInfo.clear();
                break;
            case RECEIVING_MAP_INFO:
                state = RECEIVING_MAP_INFO;
                System.out.println("Entering  Receiving Map info state");
                break;
            case WAITING_BEACON_FORMATION:
                System.out.println("Entering  Waiting formation to complete state");

                state = WAITING_BEACON_FORMATION;
                break;
            case INITIAL_STATE:
                state = INITIAL_STATE;
                break;
        }
    }

    void showPlotWindow() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage stage = new Stage();
                stage.setX(100);
                stage.setY(100);


                Group root = new Group();
                canvas = new Canvas(1000, 800);

                root.getChildren().add(canvas);
                stage.setTitle("Map");

                stage.setScene(new Scene(root));
                stage.show();
            }
        });
    }

    void drawMapOnMainThread() {
        if (canvas == null) return;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                drawMap(gc);
            }
        });

    }

    int counter = 0;
int axisOffset=400;
    void drawMap(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GREEN);
        gc.strokeLine(axisOffset,0,axisOffset,canvas.getHeight());
        gc.strokeLine(0,axisOffset,canvas.getWidth(),axisOffset);

        Set<Map.Entry<AID, ArrayList<ExplorerPositionMsg>>> poss = receivedMaps.entrySet();
        gc.strokeText(poss.size() + " entries size", 22, 42);
int points=0;
        for (Map.Entry<AID, ArrayList<ExplorerPositionMsg>> pos : poss) {
            for (ExplorerPositionMsg p : pos.getValue()) {
points++;
                gc.fillOval(axisOffset+Math.cos(p.angleFromBEaconMaster) * p.distanceFromBeaconMaster,
                        axisOffset+Math.sin(p.angleFromBEaconMaster) * p.distanceFromBeaconMaster,
                        5, 5
                );
            }
        }

        gc.strokeText("points: "+points+"  ,"+counter++ + " redraws, ", 22, 22);
    }
}
