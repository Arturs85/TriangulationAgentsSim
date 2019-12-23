package sample.Behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import sample.BaseUWBAgent;
import sample.MovementState;

public class TwoPointFormationResponderBehaviour extends OneShotBehaviour {
    BaseUWBAgent owner;
public TwoPointFormationResponderBehaviour(BaseUWBAgent owner){
    this.owner=owner;
}

    @Override
    public void action() {
        while (!receiveRequest()){
            System.out.println(owner.getName()+" waiting request");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    boolean receiveRequest(){//returns true if request received and accepted
        ACLMessage msg = myAgent.receive(owner.requestTamplate);
        if(msg!= null){
    if(msg.getSender().getName().compareTo(owner.getName())==0)return false;
            System.out.println(owner.getName()+" received formation request");
            ACLMessage reply = new ACLMessage(ACLMessage.AGREE);//make response msg
    reply.setConversationId(msg.getConversationId());
    reply.addReceiver(msg.getSender());
    owner.send(reply);// send response
     //hold still
     owner.publicPartOfAgent.movementState=MovementState.STILL;
        return true;
        }
        return false;
    }

}
