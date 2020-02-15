package sample.Behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.PublicPartOfAgent;
import sun.reflect.generics.tree.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class BeaconsMasterBehaviour extends BaseTopicBasedTickerBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 5000;//ms
    static final double triangleNominalside = 150;//px

    static final double triangleTravelside = 50;//px

    public MessageTemplate beaconsToBeaconMasterTpl;


    public BeaconsMasterBehaviour(BaseUWBAgent owner,TreeMap<AID, Double> anglesToB,TreeMap<AID, Double> distancesToB) {
        super(owner);
        this.owner = owner;
        subscribeToMessages();
        this.anglesToB=anglesToB;
        this.distancesToB=distancesToB;
        shortName = "BMB";

    }

    TreeMap<AID, TreeMap<AID, Double>> measurements = new TreeMap();
    TreeMap<AID, Double> anglesToB;
    TreeMap<AID, Double> distancesToB;

    void subscribeToMessages() {
        createAndRegisterReceivingTopics(TopicNames.beaconsToBeaconsMaster);
    }


    @Override
    protected void onTick() {
        receiveMeasurementFromBeacons();
    }

    void receiveMeasurementFromBeacons() {
        ACLMessage msg = owner.receive(templates[TopicNames.beaconsToBeaconsMaster.ordinal()]);

        while (msg != null) {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                BeaconToBeaconMasterMsg obj = null;

                try {
                    obj = (BeaconToBeaconMasterMsg) msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                if (!measurements.containsKey(obj.explorer)) {// this is first measurement for this explorer, create new entry
                    TreeMap<AID, Double> entry = new TreeMap<>();
                    measurements.put(obj.explorer, entry);
                }
                //add dist from beacon
                measurements.get(obj.explorer).put(obj.beacon, obj.distance);

                if (measurements.get(obj.explorer).size() >= 3) {//all distances are known, calculate explorers position and send it to him

double angle = calcAngleOfExplorer(measurements.get(obj.explorer));
double distance = measurements.get(obj.explorer).get(owner.getAID());
sendPositionToExplorer(obj.explorer,distance,angle);
                    measurements.remove(obj.explorer);
                }

            }

            msg = owner.receive(templates[TopicNames.beaconsToBeaconsMaster.ordinal()]);
        }
    }

    void sendPositionToExplorer(AID explorer,double distance,double angle) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(UWBOntologies.positionInfo.name());
        ExplorerPositionMsg obj = new ExplorerPositionMsg(angle,distance);
        try {
            msg.setContentObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(explorer);
        owner.send(msg);
    }

    double calcAngleOfExplorer( TreeMap<AID, Double> distances){
       double distFromMB= distances.get(owner.getAID());// this implies that BeaconsMaster must bee one of beacons
      AID firstBeacon= anglesToB.firstKey();
        AID secondBeacon= anglesToB.lastKey();

        double beta = PublicPartOfAgent.calcAngle(distFromMB,distancesToB.get(firstBeacon),distances.get(firstBeacon));
        double gamma = PublicPartOfAgent.calcAngle(distFromMB,distancesToB.get(secondBeacon),distances.get(secondBeacon));
        ArrayList<Double> possibleAngles = new ArrayList<>(4);
        possibleAngles.add(anglesToB.get(firstBeacon)+beta);
        possibleAngles.add(anglesToB.get(firstBeacon)-beta);
        possibleAngles.add(anglesToB.get(secondBeacon)+gamma);
        possibleAngles.add(anglesToB.get(secondBeacon)-gamma);
return findTwoClosestValues(possibleAngles);
    }

    /**
     *
     * @param l
     * @return average of two values in the list @param l that are closest
     */
    double findTwoClosestValues(ArrayList<Double> l){
       double cand1=0,cand2=0;
double minErrSoFar = Double.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            for (int j = i+1; j < l.size(); j++) {
             double err = Math.abs(l.get(i)-l.get(j));
             if(err<minErrSoFar){
               cand1= l.get(i);
               cand2 = l.get(j);
             minErrSoFar=err;
             }
            }
        }
        System.out.println(" err From two triangles, degrees = "+Math.toDegrees(minErrSoFar));
        return (cand1+cand2)/2;
    }

}
