package sample.Behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sample.BaseUWBAgent;
import sample.Space2D;

import java.io.IOException;
import java.util.ArrayList;

import static sample.PublicPartOfAgent.scale;

public class BaseCommandListeningBehaviour extends BaseTopicBasedTickerBehaviour{
MessageTemplate assignRoleTpl = MessageTemplate.MatchOntology(UWBOntologies.carryOutBeaconRole.name());

    public BaseCommandListeningBehaviour(BaseUWBAgent a) {
        super(a);
        createAndRegisterReceivingTopics(TopicNames.grandMasterToAll);
createSendingTopic(TopicNames.potentialBeaconsToGMaster);
   shortName = "BCLB";
    }


    @Override
    protected void onTick() {
        processBaseMessages();
        receiveBehaviourChangeRequest();
   // draw();
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

                owner.addBehaviour(new TwoPointParalelFormationRequest(owner));
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
     //   System.out.println("--sent bat level to topic pbtgm--");
        owner.send(msg);
    }

    void draw() {
        Platform.runLater(new Runnable() {
                              @Override
                              public void run() {
                                  GraphicsContext g = owner.publicPartOfAgent.canvas.getGraphicsContext2D();
                                  g.setStroke(Color.BLACK);
                                  int rowHeight=20;
                                  ArrayList<BaseTopicBasedTickerBehaviour>  b= new ArrayList<>(owner.behaviours);
                                  for (int i = 0; i < b.size(); i++) {


                                      g.strokeText(String.valueOf(b.get(i).shortName+" "), Space2D.cellsOffset + owner.publicPartOfAgent.x * scale, Space2D.cellsOffset + (owner.publicPartOfAgent.y - owner.publicPartOfAgent.radius + rowHeight) * scale);

                                  }

                              }
                          }
        );

    }
}
