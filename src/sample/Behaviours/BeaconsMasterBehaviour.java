package sample.Behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.PublicPartOfAgent;

public class BeaconsMasterBehaviour extends OneShotBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 5000;//ms
    static final double triangleNominalside = 150;//px

    static final double triangleTravelside = 50;//px
int confirmationsReceived=0;

   public AID b2TobMasterTopic;
    public MessageTemplate beaconsToBeaconMasterTpl;

public BeaconsMasterBehaviour(BaseUWBAgent owner) {

    this.owner = owner;
subscribeToMessages();
}

    void subscribeToMessages() {
        b2TobMasterTopic = owner.createTopicForBehaviour(TopicNames.beaconToBeaconMasterTopic.name());
        beaconsToBeaconMasterTpl = MessageTemplate.MatchTopic(b2TobMasterTopic);
        owner.registerBehaviourToTopic(b2TobMasterTopic);

    }


    @Override
    public void action() {
       while (!receiveTwoReadymsg()){
           try {
               Thread.sleep(50); // wait while other agents are ready
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }

        System.out.println("Received  Two ready message !!!");

        PublicPartOfAgent[] otherAgents = getAnotherTwoAgents();
        if(otherAgents==null)return;

        try {
            Thread.sleep(3000); // wait while other agent is ready
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            sendInvite(otherAgents[0],otherAgents[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sends message to another agent and waits for reply
     *
     * @param ppaOne
     */
    void sendInvite(PublicPartOfAgent ppaOne,PublicPartOfAgent ppaTwo  ) throws Exception {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(testConvId);
        msg.addReceiver(new AID(ppaOne.agentName, false));
        msg.addReceiver(new AID(ppaTwo.agentName, false));

        System.out.println("Sending msg request to: "+ppaOne.agentName);
        owner.send(msg);
        System.out.println(owner.getName() + " twoPointFormation request msg sent");
        boolean waitingReply = true;
        long startTime = System.currentTimeMillis();
        while (waitingReply) {
            if(true){// (receiveTwoAgreements()) {
                // carry on with protocol
                System.out.println("Confirmation received");
                double distance1 = owner.publicPartOfAgent.measureDistance(ppaOne.agentName);//no use to call measureDistance by name- could be called directly by ppa
                double distance1Two = owner.publicPartOfAgent.measureDistance(ppaTwo.agentName);//no use to call measureDistance by name- could be called directly by ppa

                // now make stright movement in some direction
                owner.publicPartOfAgent.moveForwardBy(triangleTravelside);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance
                System.out.println(owner.getName()+" moved to position for second measurement");
                double distance2 = owner.publicPartOfAgent.measureDistance(ppaOne.agentName);//no use to call measureDistance by name- could be called directly by ppa
                double distance2Two = owner.publicPartOfAgent.measureDistance(ppaTwo.agentName);//no use to call measureDistance by name- could be called directly by ppa

                double relativeAngle = owner.publicPartOfAgent.calculateRelativeAngle(distance1, distance2, triangleTravelside);
                double relativeAngleTwo = owner.publicPartOfAgent.calculateRelativeAngle(distance1Two, distance2Two, triangleTravelside);

                owner.publicPartOfAgent.angleToOtherRobot = relativeAngle;
            double dirBeforeTurn = owner.publicPartOfAgent.direction;//for  calc delta angle
                // turn  at one of possible directions
                System.out.println("angle to another "+Math.toDegrees(relativeAngle));
                System.out.println("angle to anotherTwo "+Math.toDegrees(relativeAngleTwo));

                owner.publicPartOfAgent.turnRightBy(relativeAngle);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                System.out.println("absolute angle1 : "+Math.toDegrees(owner.publicPartOfAgent.direction));

                double deltaAngleH1 = relativeAngle-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc
                double deltaAngleH2 = -relativeAngle-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc
                double deltaAngleH1Two = relativeAngleTwo-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc
                double deltaAngleH2Two = -relativeAngleTwo-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc

                System.out.println("h1 da: "+Math.toDegrees(deltaAngleH1));
                System.out.println("h2 da: "+Math.toDegrees(deltaAngleH2));
                System.out.println("h1Two da: "+Math.toDegrees(deltaAngleH1Two));
                System.out.println("h2Two da: "+Math.toDegrees(deltaAngleH2Two));

                owner.publicPartOfAgent.moveForwardBy(triangleTravelside/2);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance
                double distance3 = owner.publicPartOfAgent.measureDistance(ppaOne.agentName);//no use to call measureDistance by name- could be called directly by ppa
                double distance3Two = owner.publicPartOfAgent.measureDistance(ppaTwo.agentName);//no use to call measureDistance by name- could be called directly by ppa

                System.out.println("error of first hipot: "+(distance2-(triangleTravelside/2)-distance3));


                double predictedDistH1 =PublicPartOfAgent.calcThirdSide(distance2,triangleTravelside/2,deltaAngleH1);
                double predictedDistH2 =PublicPartOfAgent.calcThirdSide(distance2,triangleTravelside/2,deltaAngleH2);
                double predictedDistH1Two =PublicPartOfAgent.calcThirdSide(distance2Two,triangleTravelside/2,deltaAngleH1Two);
                double predictedDistH2Two =PublicPartOfAgent.calcThirdSide(distance2Two,triangleTravelside/2,deltaAngleH2Two);


                System.out.println(" Actual measure :" +distance3);
                System.out.println(" Predicted measureH1 :" +predictedDistH1);
                System.out.println(" Predicted measureH2 :" +predictedDistH2);
//now selsct right hip

                double errH1= Math.abs(predictedDistH1-distance3);
                double errH2= Math.abs(predictedDistH2-distance3);
                double errH1Two= Math.abs(predictedDistH1Two-distance3Two);
                double errH2Two= Math.abs(predictedDistH2Two-distance3Two);

                double da,daTwo;
                if(errH1>errH2){
                    da = deltaAngleH2;
                }else
                    da=deltaAngleH1;

                if(errH1Two>errH2Two){
                    daTwo = deltaAngleH2Two;
                }else
                    daTwo=deltaAngleH1Two;

                double angleAfterMovement = Math.PI-Math.asin(distance2*Math.sin(da)/distance3);
                double angleAfterMovementTwo = Math.PI-Math.asin(distance2Two*Math.sin(daTwo)/distance3Two);

             //   if(angleAfterMovement>Math.PI/2)//why this is needed?
             //       angleAfterMovement=Math.PI-angleAfterMovement;
             //   if(angleAfterMovementTwo>Math.PI/2)//why this is needed?
             //       angleAfterMovementTwo=Math.PI-angleAfterMovementTwo;

                System.out.println("angle to another after move: "+Math.toDegrees(angleAfterMovement));
                System.out.println("angle to anotherTwo after move: "+Math.toDegrees(angleAfterMovementTwo));

                System.out.println("absolute angle2 : "+Math.toDegrees(owner.publicPartOfAgent.direction));

                owner.publicPartOfAgent.angleToOtherRobot=angleAfterMovement;

                double avga= (angleAfterMovement+angleAfterMovementTwo)/2;
                double dista= (distance3+distance3Two)/2;

                owner.publicPartOfAgent.turnRightBy(avga);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                owner.publicPartOfAgent.moveForwardBy(dista);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance

waitingReply=false;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(owner.getName()+" Waiting reply");
            if ((System.currentTimeMillis() - startTime) > replyTimeout){
                waitingReply = false;
            System.out.println("No reply received within timeout");
            }
        }

    }
    PublicPartOfAgent[] getAnotherTwoAgents(){
        PublicPartOfAgent[] others=new PublicPartOfAgent[2];
        int count =0;
        for (PublicPartOfAgent other :owner.publicPartOfAgent.simulation.publicPartsOfAgents) {

            if(other.agentName.compareTo(owner.publicPartOfAgent.agentName)!=0)
others[count++]=other;
        }
        if(count>=2)
        return others;
        else
            return null;
    }

    boolean receiveTwoAgreements(){
        receivePositiveReply();
        if(confirmationsReceived>=2)
            return true;
        else
            return false;
    }
    boolean receiveTwoReadymsg(){
        ACLMessage msg = owner.receive(MessageTemplate.MatchTopic(b2TobMasterTopic));
if(msg!=null){
    if(msg.getPerformative()==ACLMessage.ACCEPT_PROPOSAL)
    return true;
}

        return false;
    }

    boolean receivePositiveReply() {

        ACLMessage msg = owner.receive(MessageTemplate.MatchConversationId(testConvId));
        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.AGREE) {
             confirmationsReceived++;
                return true;
            }
        }
        return false;
    }
}
