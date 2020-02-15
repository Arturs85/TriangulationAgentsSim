package sample.Behaviours;

import jade.lang.acl.ACLMessage;
import sample.BaseUWBAgent;
import sample.MovementState;

public class TwoPointFormationResponderBehaviour extends BaseTopicBasedTickerBehaviour {
    BaseUWBAgent owner;
    FormationBehaviourStates state=FormationBehaviourStates.WAITING_REQUEST;
public TwoPointFormationResponderBehaviour(BaseUWBAgent owner){
    super(owner);

    this.owner=owner;
    createAndRegisterReceivingTopics(TopicNames.twoPointFormation);
    shortName = "FRespB";

}

//    @Override
//    public void action() {
//        while (!receiveRequest()){
//            System.out.println(owner.getName()+" waiting request");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    boolean receiveRequest(){//returns true if request received and accepted
        ACLMessage msg = myAgent.receive(templates[TopicNames.twoPointFormation.ordinal()]);
        if(msg!= null){
    if(msg.getSender().getName().compareTo(owner.getName())==0)return false;
            System.out.println(owner.getName()+" received formation request");
            ACLMessage reply = new ACLMessage(ACLMessage.AGREE);//make response msg
    reply.setOntology(UWBOntologies.twoPointFormation.name());
    reply.addReceiver(msg.getSender());
    owner.send(reply);// send response
     //hold still
     owner.publicPartOfAgent.movementState=MovementState.STILL;
        return true;
        }
        return false;
    }

    @Override
    protected void onTick() {
       switch (state){
           case WAITING_REQUEST:
             if(  receiveRequest()){
                 state=FormationBehaviourStates.WAITING_BEAC_MASTER_REQUEST;
             }
           case WAITING_BEAC_MASTER_REQUEST:
               if(  receiveRequest()){
                   state=FormationBehaviourStates.IDLE;
               owner.addBehaviour(new BeaconBehaviour(owner));
               owner.removeBehaviour(this);
               }
               break;
           case IDLE:
               break;
       }
    }
}
