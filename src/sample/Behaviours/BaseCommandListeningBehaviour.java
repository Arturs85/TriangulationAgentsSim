package sample.Behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sample.BaseUWBAgent;

import java.io.IOException;

public class BaseCommandListeningBehaviour extends BaseTopicBasedTickerBehaviour{
MessageTemplate assignRoleTpl = MessageTemplate.MatchOntology(UWBOntologies.carryOutBeaconRole.name());

    public BaseCommandListeningBehaviour(BaseUWBAgent a) {
        super(a);
        createAndRegisterReceivingTopics(TopicNames.grandMasterToAll);
createSendingTopic(TopicNames.potentialBeaconsToGMaster);
    }


    @Override
    protected void onTick() {
        processBaseMessages();
        receiveBehaviourChangeRequest();
    }

    void processBaseMessages() {// messages from  master- orders to change role
        receiveBatLevelRequest();
        //owner.postMessage();// does this puts msg back in queue??

    }

    void receiveBatLevelRequest() {
        ACLMessage msg = owner.receive(templates[TopicNames.grandMasterToAll.ordinal()]);
        //process all messages of this topic
        while (msg != null) {

            if (msg.getPerformative() == ACLMessage.REQUEST) {//this is bat level request
                sendBatLevel();
            }

            msg = owner.receive(templates[TopicNames.potentialBeaconsToGMaster.ordinal()]);
        }
    }
    void receiveBehaviourChangeRequest() {
        ACLMessage msg = owner.receive(assignRoleTpl);
        //process all messages of this topic
        while (msg != null) {
            Integer hierarchyLevel=null;
            try {
                 hierarchyLevel = (Integer) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            if (hierarchyLevel == 0 ) {//0 means master

                owner.addBehaviour(new BeaconsMasterBehaviour(owner));
            }else if(hierarchyLevel==1){
                owner.addBehaviour(new TwoPointFormationRequest(owner));
            }else if(hierarchyLevel==2){
                owner.addBehaviour(new TwoPointFormationResponderBehaviour(owner));
            }

            msg = owner.receive(assignRoleTpl);
        }
    }

    void sendBatLevel() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(owner.publicPartOfAgent.battLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(sendingTopics[TopicNames.potentialBeaconsToGMaster.ordinal()]);
        System.out.println("--sent bat level to topic pbtgm--");
        owner.send(msg);
    }
}
