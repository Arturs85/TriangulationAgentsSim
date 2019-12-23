package sample.Behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.PublicPartOfAgent;

public class TwoPointFormationRequest extends OneShotBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 5000;//ms
    static final double triangleNominalside = 150;//px

    static final double triangleTravelside = 50;//px
    public AID b2TobMasterTopic;


    public TwoPointFormationRequest(BaseUWBAgent owner) {
        this.owner = owner;
        b2TobMasterTopic = owner.createTopicForBehaviour(TopicNames.beaconToBeaconMasterTopic.name());

    }

    @Override
    public void action() {
        PublicPartOfAgent otherAgent = getAnotherAgent();
        if(otherAgent==null)return;

        try {
            Thread.sleep(3000); // wait while other agent is ready
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            sendInvite(otherAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sends message to another agent and waits for reply
     *
     * @param ppa
     */
    void sendInvite(PublicPartOfAgent ppa) throws Exception {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(testConvId);
        msg.addReceiver(new AID(ppa.agentName, false));
        System.out.println("Sending msg request to: "+ppa.agentName);
        owner.send(msg);
        System.out.println(owner.getName() + " twoPointFormation request msg sent");
        boolean waitingReply = true;
        long startTime = System.currentTimeMillis();
        while (waitingReply) {
            if (receivePositiveReply()) {
                // carry on with protocol
                System.out.println("Confirmation received");
                double distance1 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa
                // now make stright movement in some direction
                owner.publicPartOfAgent.moveForwardBy(triangleTravelside);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance
                System.out.println(owner.getName()+" moved to position for second measurement");
                double distance2 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa

                double relativeAngle = owner.publicPartOfAgent.calculateRelativeAngle(distance1, distance2, triangleTravelside);
                owner.publicPartOfAgent.angleToOtherRobot = relativeAngle;
            double dirBeforeTurn = owner.publicPartOfAgent.direction;//for  calc delta angle
                // turn  at one of possible directions
                System.out.println("angle to another "+Math.toDegrees(relativeAngle));
                owner.publicPartOfAgent.turnRightBy(relativeAngle);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                System.out.println("absolute angle1 : "+Math.toDegrees(owner.publicPartOfAgent.direction));

                double deltaAngleH1 = relativeAngle-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc
                double deltaAngleH2 = -relativeAngle-(owner.publicPartOfAgent.direction-dirBeforeTurn);// angle for triangle calc

                System.out.println("h1 da: "+Math.toDegrees(deltaAngleH1));
                System.out.println("h2 da: "+Math.toDegrees(deltaAngleH2));

                owner.publicPartOfAgent.moveForwardBy(triangleTravelside/2);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance
                double distance3 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa
                System.out.println("error of first hipot: "+(distance2-(triangleTravelside/2)-distance3));

                double predictedDistH1 =PublicPartOfAgent.calcThirdSide(distance2,triangleTravelside/2,deltaAngleH1);
                double predictedDistH2 =PublicPartOfAgent.calcThirdSide(distance2,triangleTravelside/2,deltaAngleH2);

                System.out.println(" Actual measure :" +distance3);
                System.out.println(" Predicted measureH1 :" +predictedDistH1);
                System.out.println(" Predicted measureH2 :" +predictedDistH2);

                double angleAfterMovement = Math.PI-Math.asin(distance2*Math.sin(deltaAngleH2)/distance3);

                if(angleAfterMovement>Math.PI/2)//why this is needed?
                    angleAfterMovement=Math.PI-angleAfterMovement;

                System.out.println("angle to another after move: "+Math.toDegrees(angleAfterMovement));
                System.out.println("absolute angle2 : "+Math.toDegrees(owner.publicPartOfAgent.direction));

                owner.publicPartOfAgent.angleToOtherRobot=angleAfterMovement;

                double turnBy=0;
                if(distance3<triangleNominalside){
                    //move away from other robot
                    turnBy=angleAfterMovement+Math.PI;
                }else {
                    turnBy = angleAfterMovement;
                }
                owner.publicPartOfAgent.turnRightBy(turnBy);

                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                System.out.println("absolute angle3 : "+Math.toDegrees(owner.publicPartOfAgent.direction));
double distToTravel = Math.abs(distance3-triangleNominalside);
                owner.publicPartOfAgent.moveForwardBy(distToTravel);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance

                double distance4 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa
                System.out.println(" Actual measure :" +distance4);
sendReadyToMaster();

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
    PublicPartOfAgent getAnotherAgent(){
        for (PublicPartOfAgent other :owner.publicPartOfAgent.simulation.publicPartsOfAgents) {
        if(other.agentName.compareTo(owner.publicPartOfAgent.agentName)!=0)
            return other;
        }
        return null;
    }

    void sendReadyToMaster(){
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(b2TobMasterTopic);
        owner.send(msg);

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
