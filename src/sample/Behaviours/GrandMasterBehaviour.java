package sample.Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
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

    public GrandMasterBehaviour(BaseUWBAgent a) {
        super(a);
        createSendingTopic(TopicNames.grandMasterToAll);
        createAndRegisterReceivingTopics(TopicNames.potentialBeaconsToGMaster);
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
                        System.out.println("not enough replays: "+receivedInfo.size());
                }
                        else {
                        informNewBeacons();
                        enterState(RECEIVING_MAP_INFO);
                    }
                break;
            case RECEIVING_MAP_INFO:

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
                state  = GATHERING_AGENTS_INFO;
                cyclesCounter = waitReplysTimeout / BaseUWBAgent.tickerPeriod;
                receivedInfo.clear();
                break;
            case RECEIVING_MAP_INFO:
                state = RECEIVING_MAP_INFO;
                System.out.println("Entering  Receiving Map info state");
                break;
            case INITIAL_STATE:
                state=INITIAL_STATE;
                break;
        }
    }
}
