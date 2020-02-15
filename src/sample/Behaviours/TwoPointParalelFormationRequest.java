package sample.Behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.PublicPartOfAgent;

import java.util.TreeMap;
import java.util.TreeSet;

public class TwoPointParalelFormationRequest extends BaseTopicBasedTickerBehaviour {
    BaseUWBAgent owner;
    public static String testConvId = "testId";
    static final int replyTimeout = 8000;//ms
    static final double triangleNominalside = 150;//px

    static final double triangleTravelside = 50;//px
    public AID b2TobMasterTopic;
    FormationBehaviourStates state = FormationBehaviourStates.WAITING_READY_MSG;
    static final int waitReplayTimeout = 9000;//ms
    TreeSet<AID> responderNames = new TreeSet<>();
    int waitingCyclesCounter = 0;
    double dist[] = new double[3];
    double distTwo[] = new double[3];
    double finalDistance;
    double finalAngle;
    double relativeAngleH1;
    double relativeAngleH2;
    double relativeAngleH1Two;
    double relativeAngleH2Two;
    double angleToFirst;
    double angleToSecond;

    double dirBeforeTurn;
double odoBeforeTravel;
    public TwoPointParalelFormationRequest(BaseUWBAgent owner) {
        super(owner);
        this.owner = owner;
        //b2TobMasterTopic = owner.createTopicForBehaviour(TopicNames.beaconFormationToBeaconMasterTopic.name());
        createSendingTopic(TopicNames.twoPointFormation);
    createSendingTopic(TopicNames.bmToGm);
        createAndRegisterReceivingTopics(TopicNames.beaconFormationToBeaconMasterTopic);
        shortName = "TPPFRB";

    }

    @Override
    protected void onTick() {
        switch (state) {
            case WAITING_READY_MSG:
                if (receiveTwoReadymsg()) {
                    System.out.println(getBehaviourName() + " received two points ready");
                    enterState(FormationBehaviourStates.STARTUP);
                }
                break;
            case STARTUP:
                sendTwoPointFormationRequest();
                enterState(FormationBehaviourStates.WAITING_REPLAY);
                break;
            case IDLE:

                break;
            case FIRST_MOVE:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done
                    dist[1] = owner.publicPartOfAgent.measureDistance(responderNames.first().getName());//todo: is this name form right?
                    distTwo[1] = owner.publicPartOfAgent.measureDistance(responderNames.last().getName());//todo: is this name form right?

                    System.out.println("Dist[1] " + dist[1]);

                    relativeAngleH1 = owner.publicPartOfAgent.calculateRelativeAngle(dist[0], dist[1], triangleTravelside);
                    relativeAngleH2 = -owner.publicPartOfAgent.calculateRelativeAngle(dist[0], dist[1], triangleTravelside);
                    relativeAngleH1Two = owner.publicPartOfAgent.calculateRelativeAngle(distTwo[0], distTwo[1], triangleTravelside);
                    relativeAngleH2Two = -owner.publicPartOfAgent.calculateRelativeAngle(distTwo[0], distTwo[1], triangleTravelside);

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
                    relativeAngleH1Two = relativeAngleH1Two - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc
                    relativeAngleH2Two = relativeAngleH2Two - (owner.publicPartOfAgent.direction - dirBeforeTurn);// angle for triangle calc


                    System.out.println("relative angle: " + Math.toDegrees(relativeAngleH1));
                    System.out.println("relative angle: " + Math.toDegrees(relativeAngleH2));

                    owner.publicPartOfAgent.moveForwardBy(triangleTravelside / 2);
                    enterState(FormationBehaviourStates.SECOND_MOVE);
                }
                break;
            case SECOND_MOVE:

                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done

                    dist[2] = owner.publicPartOfAgent.measureDistance(responderNames.first().getName());//todo: is this name form right?
                    distTwo[2] = owner.publicPartOfAgent.measureDistance(responderNames.last().getName());//todo: is this name form right?

                    System.out.println("Dist[2] " + dist[2]);
                    double predictedDistH1 = PublicPartOfAgent.calcThirdSide(dist[1], triangleTravelside / 2, relativeAngleH1);
                    double predictedDistH2 = PublicPartOfAgent.calcThirdSide(dist[1], triangleTravelside / 2, relativeAngleH2);
                    double predictedDistH1Two = PublicPartOfAgent.calcThirdSide(distTwo[1], triangleTravelside / 2, relativeAngleH1Two);
                    double predictedDistH2Two = PublicPartOfAgent.calcThirdSide(distTwo[1], triangleTravelside / 2, relativeAngleH2Two);

                    System.out.println(" Actual measure :" + dist[2]);
                    System.out.println(" Predicted measureH1 :" + predictedDistH1);
                    System.out.println(" Predicted measureH2 :" + predictedDistH2);

                    double errH1 = Math.abs(predictedDistH1 - dist[2]);
                    double errH2 = Math.abs(predictedDistH2 - dist[2]);
                    double errH1Two = Math.abs(predictedDistH1Two - distTwo[2]);
                    double errH2Two = Math.abs(predictedDistH2Two - distTwo[2]);

                    double da, daTwo;
                    if (errH1 < errH2) {
                        da = relativeAngleH1;
                    } else
                        da = relativeAngleH2;
                    System.out.println("da : " + Math.toDegrees(da));
                    if (errH1Two < errH2Two) {
                        daTwo = relativeAngleH1Two;
                    } else
                        daTwo = relativeAngleH2Two;

//double gamma  = PublicPartOfAgent.calcAngle(dist[2],triangleTravelside/2,dist[1]);
                    double angleAfterMovement = Math.PI - PublicPartOfAgent.calcAngle(dist[2], triangleTravelside / 2, dist[1]);
                    double angleAfterMovementTwo = Math.PI - PublicPartOfAgent.calcAngle(distTwo[2], triangleTravelside / 2, distTwo[1]);

                    if (Math.sin(da) < 0)
                        angleAfterMovement = -angleAfterMovement;
                    if (Math.sin(daTwo) < 0)
                        angleAfterMovementTwo = -angleAfterMovementTwo;

                    angleToFirst=angleAfterMovement;
                    angleToSecond=angleAfterMovementTwo;
                    System.out.println("angle to another after move: " + Math.toDegrees(angleAfterMovement));
                    System.out.println("angle to anotherTwo after move: " + Math.toDegrees(angleAfterMovementTwo));

                    System.out.println("absolute angle2 : " + Math.toDegrees(owner.publicPartOfAgent.direction));

                    owner.publicPartOfAgent.angleToOtherRobot = angleAfterMovement;
                    owner.publicPartOfAgent.angleToSecond=angleAfterMovementTwo;

                    double avga = (angleAfterMovement + angleAfterMovementTwo) / 2;

                    System.out.println("turn by: " + Math.toDegrees(avga));
//calc  alfa+Beta  angle
                    double gamma = angleAfterMovement - angleAfterMovementTwo;
                    double closestVertexDist = min(dist[2], distTwo[2]);
                    double apb = PublicPartOfAgent.calcAngle(closestVertexDist, triangleNominalside, max(dist[2], distTwo[2]));
                    System.out.println("apb : " + Math.toDegrees(apb));
                    double alfa = apb - Math.PI / 3;
                    finalDistance = PublicPartOfAgent.calcThirdSide(closestVertexDist, triangleNominalside, alfa);
                    finalAngle = PublicPartOfAgent.calcAngle(finalDistance, closestVertexDist, triangleNominalside);
                    System.out.println("final angle : " + Math.toDegrees(finalAngle));
dirBeforeTurn = owner.publicPartOfAgent.direction;
                    owner.publicPartOfAgent.turnRightBy(finalAngle);

                    enterState(FormationBehaviourStates.FINAL_POSITION_TURN);
                }

                break;
            case FINAL_POSITION_TURN:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {//turn is done, move to position
                    double dista = (dist[2] + distTwo[2]) / 2;
odoBeforeTravel = owner.publicPartOfAgent.odometryTotal;
                    owner.publicPartOfAgent.moveForwardBy(finalDistance);
                    enterState(FormationBehaviourStates.FINAL_POSITION_MOVE);
                }
                break;
            case FINAL_POSITION_MOVE:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_TARGET)) {//movment   is done
                    double dista = owner.publicPartOfAgent.measureDistance(responderNames.first().getName());//todo: is this name form right?
                    double distaTwo = owner.publicPartOfAgent.measureDistance(responderNames.last().getName());//todo: is this name form right?

                    System.out.println("final check measurement: " + dista);
                    sendReadyToMaster();
                    // calculate positions of other beacons after final move
                    double deltaAngle = owner.publicPartOfAgent.direction-dirBeforeTurn;
                   double travel = owner.publicPartOfAgent.odometryTotal-odoBeforeTravel;
                    angleToFirst = calcAngleAfterTurnAndMove(angleToFirst,deltaAngle,travel,dista,dist[2]);
                    owner.publicPartOfAgent.angleToOtherRobot=angleToFirst;// replace with behaviours own draw method
                    angleToSecond = calcAngleAfterTurnAndMove(angleToSecond,deltaAngle,travel,distaTwo,distTwo[2]);
                    owner.publicPartOfAgent.angleToSecond=angleToSecond;
                    // add bm behaviour
                    TreeMap<AID, Double> anglesToB=new TreeMap<>();
                    TreeMap<AID, Double> distancesToB=new TreeMap<>();
                    anglesToB.put(responderNames.first(),angleToFirst);
                    anglesToB.put(responderNames.last(),angleToSecond);
distancesToB.put(responderNames.first(),dista);
                    distancesToB.put(responderNames.last(),distaTwo);
                    owner.addBehaviour(new BeaconsMasterBehaviour(owner,anglesToB,distancesToB));
                    owner.addBehaviour(new BeaconBehaviour(owner));
                    owner.removeBehaviour(this);

                    enterState(FormationBehaviourStates.IDLE);
                }
                break;
            case WAITING_REPLAY:

                waitingCyclesCounter--;
// receive replay
                if (receivePositiveReplys()) {
                    dist[0] = owner.publicPartOfAgent.measureDistance(responderNames.first().getName());//todo: is this name form right?
                    distTwo[0] = owner.publicPartOfAgent.measureDistance(responderNames.last().getName());//todo: is this name form right?

                    enterState(FormationBehaviourStates.FIRST_MOVE);
                }

                if (waitingCyclesCounter <= 0) {
                    System.out.println(getBehaviourName() + " no reply within timeout  --- ");
                    enterState(FormationBehaviourStates.STARTUP);// start again  with request message
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
                state = FormationBehaviourStates.FINAL_POSITION_TURN;
                break;
            case FINAL_POSITION_MOVE:
                System.out.println("entering final move");

                state = FormationBehaviourStates.FINAL_POSITION_MOVE;
                break;
            case WAITING_REPLAY:
                state = FormationBehaviourStates.WAITING_REPLAY;
                waitingCyclesCounter = waitReplayTimeout / BaseUWBAgent.tickerPeriod;

                break;
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
            if (receivePositiveReplys()) {
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

    double min(double first, double second) {
        if (first < second)
            return first;
        else
            return second;
    }

    double max(double first, double second) {
        if (first > second)
            return first;
        else
            return second;
    }
    double calcAngleAfterTurnAndMove(double prevRelatAngle, double turnAngle, double travel, double measAfter,double measBefore){
        double angleAfterMovement = Math.PI - PublicPartOfAgent.calcAngle(travel,measAfter,measBefore);

        if (Math.sin(prevRelatAngle-turnAngle) < 0)// todo is this right for all cases
            angleAfterMovement = -angleAfterMovement;
        return angleAfterMovement;
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
        msg.addReceiver(sendingTopics[TopicNames.bmToGm.ordinal()]);
        owner.send(msg);

    }

    void sendTwoPointFormationRequest() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(sendingTopics[TopicNames.twoPointFormation.ordinal()]);
        //System.out.println("Sending msg request to: "+ppa.agentName);
        owner.send(msg);
        System.out.println(owner.getName() + " twoPointFormation request msg sent");
    }

    boolean receiveTwoReadymsg() {
        ACLMessage msg = owner.receive(templates[TopicNames.beaconFormationToBeaconMasterTopic.ordinal()]);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                return true;
        }

        return false;
    }

    boolean receivePositiveReplys() {

        ACLMessage msg = owner.receive(MessageTemplate.MatchOntology(UWBOntologies.twoPointFormation.name()));
        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.AGREE) {
                System.out.println(getBehaviourName() + " received positive reply");
                responderNames.add(msg.getSender());

            }
            if (responderNames.size() >= 2) return true;

        }
        return false;
    }
}
