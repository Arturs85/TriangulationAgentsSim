package sample.Behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.MovementState;
import sample.PublicPartOfAgent;

public class TwoPointFormationRequest extends BaseTopicBasedTickerBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 8000;//ms
    static final double triangleNominalside = 150;//px

    static final double triangleTravelside = 50;//px
    public AID b2TobMasterTopic;
    FormationBehaviourStates state=FormationBehaviourStates.STARTUP;
    static final int waitReplayTimeout = 9000;//ms
    AID responderName;
    int waitingCyclesCounter = 0;
    double dist[] = new double[3];
    double relativeAngleH1;
    double relativeAngleH2;
    double dirBeforeTurn;

    public TwoPointFormationRequest(BaseUWBAgent owner) {
        super(owner);
        this.owner = owner;
        createSendingTopic(TopicNames.beaconFormationToBeaconMasterTopic);
        createSendingTopic(TopicNames.twoPointFormation);
    createAndRegisterReceivingTopics(TopicNames.twoPointFormation);
        shortName = "TPFRB";

    }

    @Override
    protected void onTick() {
        switch (state) {
            case STARTUP:
                sendTwoPointFormationRequest();
                enterState(FormationBehaviourStates.WAITING_REPLAY);
                break;
            case IDLE:

                break;
            case FIRST_MOVE:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done
                    dist[1] = owner.publicPartOfAgent.measureDistance(responderName.getName());//todo: is this name form right?
                    System.out.println("Dist[1] "+dist[1]);

                    relativeAngleH1 = owner.publicPartOfAgent.calculateRelativeAngle(dist[0], dist[1], triangleTravelside);
                    relativeAngleH2 = -owner.publicPartOfAgent.calculateRelativeAngle(dist[0], dist[1], triangleTravelside);

                    owner.publicPartOfAgent.angleToOtherRobot = relativeAngleH1;
                    dirBeforeTurn = owner.publicPartOfAgent.direction;//for  calc delta angle
                    // turn  at one of possible directions
                    System.out.println("angle to another " + Math.toDegrees(relativeAngleH1));
                    owner.publicPartOfAgent.turnRightBy(relativeAngleH1);

                    enterState(FormationBehaviourStates.TURN_DEGREES);

                }
                break;
            case TURN_DEGREES:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {//movment   is done, update angle to other robot
                    relativeAngleH1 = relativeAngleH1 - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc
                    relativeAngleH2 = relativeAngleH2 - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc
                    System.out.println("relative angle: "+Math.toDegrees(relativeAngleH1));
                    System.out.println("relative angle: "+Math.toDegrees(relativeAngleH2));

                    owner.publicPartOfAgent.moveForwardBy(triangleTravelside / 2);
                    enterState(FormationBehaviourStates.SECOND_MOVE);
                }
                break;
            case SECOND_MOVE:

                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done

                    dist[2] = owner.publicPartOfAgent.measureDistance(responderName.getName());//todo: is this name form right?
                    System.out.println("Dist[2] "+dist[2]);
                    double predictedDistH1 = PublicPartOfAgent.calcThirdSide(dist[1], triangleTravelside / 2, relativeAngleH1);
                    double predictedDistH2 = PublicPartOfAgent.calcThirdSide(dist[1], triangleTravelside / 2, relativeAngleH2);

                    System.out.println(" Actual measure :" + dist[2]);
                    System.out.println(" Predicted measureH1 :" + predictedDistH1);
                    System.out.println(" Predicted measureH2 :" + predictedDistH2);

                    double errH1 = Math.abs(predictedDistH1 - dist[2]);
                    double errH2 = Math.abs(predictedDistH2 - dist[2]);

                    double da, daTwo;
                    if (errH1 < errH2) {
                        da = relativeAngleH1;
                    } else
                        da = relativeAngleH2;
                    System.out.println("da : " + Math.toDegrees(da));
//double gamma  = PublicPartOfAgent.calcAngle(dist[2],triangleTravelside/2,dist[1]);
                    double angleAfterMovement = Math.PI - PublicPartOfAgent.calcAngle(dist[2],triangleTravelside/2,dist[1]);

                    if (Math.sin(da) < 0)//why this is needed?
                        angleAfterMovement = -angleAfterMovement;

                    System.out.println("angle to another after move: " + Math.toDegrees(angleAfterMovement));
                    System.out.println("absolute angle2 : " + Math.toDegrees(owner.publicPartOfAgent.direction));

                    owner.publicPartOfAgent.angleToOtherRobot = angleAfterMovement;

                    double turnBy = 0;
                    if (dist[2] < triangleNominalside) {
                        //move away from other robot
                        turnBy = angleAfterMovement + Math.PI;
                    } else {
                        turnBy = angleAfterMovement;
                    }
                    System.out.println("turn by: "+Math.toDegrees(turnBy));
                    owner.publicPartOfAgent.turnRightBy(turnBy);


                    enterState(FormationBehaviourStates.FINAL_POSITION_TURN);
                }

                break;
            case FINAL_POSITION_TURN:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {//turn is done, move to position
                    double distToTravel = Math.abs(dist[2] - triangleNominalside);
                    owner.publicPartOfAgent.moveForwardBy(distToTravel);
                    enterState(FormationBehaviourStates.FINAL_POSITION_MOVE);
                }
                    break;
            case FINAL_POSITION_MOVE:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done
                    double dist = owner.publicPartOfAgent.measureDistance(responderName.getName());//todo: is this name form right?
                    System.out.println("final check measurement: "+dist);
                    sendReadyToMaster();
enterState(FormationBehaviourStates.WAITING_BEAC_MASTER_REQUEST);
                }
                break;
            case WAITING_REPLAY:

                waitingCyclesCounter--;
// receive replay
                if (receivePositiveReply()) {
                    dist[0] = owner.publicPartOfAgent.measureDistance(responderName.getName());//todo: is this name form right?
                    enterState(FormationBehaviourStates.FIRST_MOVE);
                }

                if (waitingCyclesCounter <= 0) {
                    System.out.println(getBehaviourName() + " no reply within timeout  --- ");
                    enterState(FormationBehaviourStates.STARTUP);// start again  with request message
                }


                break;
            case WAITING_BEAC_MASTER_REQUEST:
                if(receiveRequest()){
                    owner.removeBehaviour(this);
                    owner.addBehaviour(new BeaconBehaviour(owner));
                    enterState(FormationBehaviourStates.IDLE);
                }
                break;
            default:
                break;
        }
    }

    void enterState(FormationBehaviourStates newState) {
        switch (newState) {
            case STARTUP:
                state = FormationBehaviourStates.STARTUP;

                break;
            case IDLE:
                state = FormationBehaviourStates.IDLE;
                break;
            case FIRST_MOVE:
                owner.publicPartOfAgent.moveForwardBy(triangleTravelside);
                state = FormationBehaviourStates.FIRST_MOVE;
                System.out.println("entering first move");
                break;
            case TURN_DEGREES:
                System.out.println("entering turn degrees");
                state = FormationBehaviourStates.TURN_DEGREES;
                break;
            case SECOND_MOVE:
                System.out.println("entering second move");
                state = FormationBehaviourStates.SECOND_MOVE;
                break;
            case FINAL_POSITION_TURN:
                System.out.println("entering final turn");
                state=FormationBehaviourStates.FINAL_POSITION_TURN;
                break;
                case FINAL_POSITION_MOVE:
                    System.out.println("entering final move");

                    state = FormationBehaviourStates.FINAL_POSITION_MOVE;
                break;
            case WAITING_REPLAY:
                state = FormationBehaviourStates.WAITING_REPLAY;
                waitingCyclesCounter = waitReplayTimeout / BaseUWBAgent.tickerPeriod;

                break;
            case WAITING_BEAC_MASTER_REQUEST:
                state=FormationBehaviourStates.WAITING_BEAC_MASTER_REQUEST;
            default:
                break;
        }

    }
//    @Override
//    public void action() {
//        PublicPartOfAgent otherAgent = getAnotherAgent();
//        if(otherAgent==null)return;
//
//        try {
//            Thread.sleep(3000); // wait while other agent is ready
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            sendInvite(otherAgent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * sends message to another agent and waits for reply
     *
     * @param ppa
     */
    void sendInvite(PublicPartOfAgent ppa) throws Exception {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(testConvId);
        msg.addReceiver(new AID(ppa.agentName, false));
        System.out.println("Sending msg request to: " + ppa.agentName);
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
                System.out.println(owner.getName() + " moved to position for second measurement");
                double distance2 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa

                double relativeAngle = owner.publicPartOfAgent.calculateRelativeAngle(distance1, distance2, triangleTravelside);
                owner.publicPartOfAgent.angleToOtherRobot = relativeAngle;
                double dirBeforeTurn = owner.publicPartOfAgent.direction;//for  calc delta angle
                // turn  at one of possible directions
                System.out.println("angle to another " + Math.toDegrees(relativeAngle));
                owner.publicPartOfAgent.turnRightBy(relativeAngle);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                System.out.println("absolute angle1 : " + Math.toDegrees(owner.publicPartOfAgent.direction));

                double deltaAngleH1 = relativeAngle - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc
                double deltaAngleH2 = -relativeAngle - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc

                System.out.println("h1 da: " + Math.toDegrees(deltaAngleH1));
                System.out.println("h2 da: " + Math.toDegrees(deltaAngleH2));

                owner.publicPartOfAgent.moveForwardBy(triangleTravelside / 2);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance
                double distance3 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa
                System.out.println("error of first hipot: " + (distance2 - (triangleTravelside / 2) - distance3));

                double predictedDistH1 = PublicPartOfAgent.calcThirdSide(distance2, triangleTravelside / 2, deltaAngleH1);
                double predictedDistH2 = PublicPartOfAgent.calcThirdSide(distance2, triangleTravelside / 2, deltaAngleH2);

                System.out.println(" Actual measure :" + distance3);
                System.out.println(" Predicted measureH1 :" + predictedDistH1);
                System.out.println(" Predicted measureH2 :" + predictedDistH2);

                double angleAfterMovement = Math.PI - Math.asin(distance2 * Math.sin(deltaAngleH2) / distance3);

                if (angleAfterMovement > Math.PI / 2)//why this is needed?
                    angleAfterMovement = Math.PI - angleAfterMovement;

                System.out.println("angle to another after move: " + Math.toDegrees(angleAfterMovement));
                System.out.println("absolute angle2 : " + Math.toDegrees(owner.publicPartOfAgent.direction));

                owner.publicPartOfAgent.angleToOtherRobot = angleAfterMovement;

                double turnBy = 0;
                if (distance3 < triangleNominalside) {
                    //move away from other robot
                    turnBy = angleAfterMovement + Math.PI;
                } else {
                    turnBy = angleAfterMovement;
                }
                owner.publicPartOfAgent.turnRightBy(turnBy);

                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {
                }//wait while robot turns set distance
                System.out.println("absolute angle3 : " + Math.toDegrees(owner.publicPartOfAgent.direction));
                double distToTravel = Math.abs(distance3 - triangleNominalside);
                owner.publicPartOfAgent.moveForwardBy(distToTravel);
                while (owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {
                }//wait while robot moves set distance

                double distance4 = owner.publicPartOfAgent.measureDistance(ppa.agentName);//no use to call measureDistance by name- could be called directly by ppa
                System.out.println(" Actual measure :" + distance4);
                sendReadyToMaster();

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(owner.getName() + " Waiting reply");
            if ((System.currentTimeMillis() - startTime) > replyTimeout) {
                waitingReply = false;
                System.out.println("No reply received within timeout");
            }
        }

    }

    PublicPartOfAgent getAnotherAgent() {
        for (PublicPartOfAgent other : owner.publicPartOfAgent.simulation.publicPartsOfAgents) {
            if (other.agentName.compareTo(owner.publicPartOfAgent.agentName) != 0)
                return other;
        }
        return null;
    }

    void sendReadyToMaster() {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(sendingTopics[TopicNames.beaconFormationToBeaconMasterTopic.ordinal()]);
        owner.send(msg);

    }

    void sendTwoPointFormationRequest() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(sendingTopics[TopicNames.twoPointFormation.ordinal()]);
        //System.out.println("Sending msg request to: "+ppa.agentName);
        owner.send(msg);
        System.out.println(owner.getName() + " twoPointFormation request msg sent");
    }
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

    boolean receivePositiveReply() {

        ACLMessage msg = owner.receive(MessageTemplate.MatchOntology(UWBOntologies.twoPointFormation.name()));
        if (msg != null) {
            responderName = msg.getSender();

            if (msg.getPerformative() == ACLMessage.AGREE) {
                System.out.println(getBehaviourName() + " received positive reply");
                return true;

            }
        }
        return false;
    }
}
