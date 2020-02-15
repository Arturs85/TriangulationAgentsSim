package sample.Behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import sample.BaseUWBAgent;
import sample.PublicPartOfAgent;

import java.io.IOException;

//receives distmeasRequests,  calculated dist, and sends it to master beacon
public class BeaconBehaviour extends BaseTopicBasedTickerBehaviour{


    public BeaconBehaviour(BaseUWBAgent a) {
        super(a);
    createAndRegisterReceivingTopics(TopicNames.explorerToBeacons);
    createSendingTopic(TopicNames.beaconsToBeaconsMaster);
        shortName = "BB";

    }


    @Override
    protected void onTick() {
receiveMeasureRequest();
    }
    void receiveMeasureRequest() {
        ACLMessage msg = owner.receive(templates[TopicNames.explorerToBeacons.ordinal()]);
        if(msg!=null&& msg.getPerformative()==ACLMessage.REQUEST){// this is dist measure request
            System.out.println(getBehaviourName()+ " received dist measurement request");
            double dist = owner.publicPartOfAgent.measureDistance(msg.getSender().getName());
            //send this dist to masterBeacon
sendMeasurementToBeaconMaster(msg.getSender(),dist);
        }

    }
    void sendMeasurementToBeaconMaster(AID explorer,double distance) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        BeaconToBeaconMasterMsg obj = new BeaconToBeaconMasterMsg(explorer,distance,owner.getAID());
        try {
            msg.setContentObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.beaconsToBeaconsMaster.ordinal()]);
        owner.send(msg);
    }

}
