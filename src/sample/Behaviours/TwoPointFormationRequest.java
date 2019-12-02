package sample.Behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;
import sample.PublicPartOfAgent;

public class TwoPointFormationRequest extends OneShotBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 5000;//ms

    TwoPointFormationRequest(BaseUWBAgent owner) {
        this.owner = owner;
    }

    @Override
    public void action() {

    }

    /**
     * sends message to another agent and waits for reply
     *
     * @param ppa
     */
    void sendInvite(PublicPartOfAgent ppa) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(testConvId);
        msg.addReceiver(new AID(ppa.agentName, true));
        owner.send(msg);
        System.out.println(owner.getName() + " twoPointFormation request msg sent");
        boolean waitingReply = true;
        long startTime = System.currentTimeMillis();
        while (waitingReply) {
            if (receivePositiveReply()) {
                // carry on with protocol
                System.out.println("Confirmation received");

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - startTime > replyTimeout)
                waitingReply = false;

        }
    }

    boolean receivePositiveReply() {

        ACLMessage msg = owner.receive(MessageTemplate.MatchConversationId(testConvId));
        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.AGREE)
                return true;

        }
        return false;
    }
}