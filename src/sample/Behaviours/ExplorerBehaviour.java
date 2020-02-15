package sample.Behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sample.BaseUWBAgent;
import sample.MovementMode;
import sample.MovementState;
import sample.PublicPartOfAgent;

import java.util.Random;

public class ExplorerBehaviour extends BaseTopicBasedTickerBehaviour {
    ExpolrerStates state = ExpolrerStates.INITIAL;
    double previousOdo;
    ExplorerPositionMsg latestReceivedPosition;
    ExplorerPositionMsg previousReceivedPosition;
    static double travelBetweenMeas = 50;
    MessageTemplate positionInfoTpl = MessageTemplate.MatchOntology(UWBOntologies.positionInfo.name());
static double maxDistFromBeacon = 300;
static double minDistFromBeacon  = 50;
 double angleToBM;
   Random rnd = new Random();
    public ExplorerBehaviour(BaseUWBAgent a) {
        super(a);
        createSendingTopic(TopicNames.explorerToBeacons);
        createAndRegisterReceivingTopics(TopicNames.bmToGm);
        createSendingTopic(TopicNames.explorerToGM);
        shortName = "EB";

    }

    void measureDistanceToBeacons() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(sendingTopics[TopicNames.explorerToBeacons.ordinal()]);
        owner.send(msg);
        previousOdo = owner.publicPartOfAgent.odometryTotal;
    }

    /**
     * @return true if position update received
     */
    boolean receivePositionInfo() {
        ACLMessage msg = owner.receive(positionInfoTpl);
        //process all messages of this topic
        while (msg != null) {

            if (msg.getPerformative() == ACLMessage.INFORM) {

                try {
                    if (latestReceivedPosition != null) {
                        //send  road  info to grandmaster
                        previousReceivedPosition = latestReceivedPosition;
                    }
                    latestReceivedPosition = (ExplorerPositionMsg) msg.getContentObject();
msg.clearAllReceiver();
msg.setSender(owner.getAID());
msg.addReceiver(sendingTopics[TopicNames.explorerToGM.ordinal()]);
                    owner.send(msg);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                return true;
            }

            msg = owner.receive(positionInfoTpl);
        }
        return false;
    }

    double calculateAngleToBM() {//  travelBetweenMeas  will not always be rigtht distance
        if (previousReceivedPosition == null || latestReceivedPosition == null) return 0;
        System.out.println(" args: "+latestReceivedPosition.distanceFromBeaconMaster+" "+ travelBetweenMeas+" "+ previousReceivedPosition.distanceFromBeaconMaster);
        double gamma = Math.PI - PublicPartOfAgent.calcAngle(latestReceivedPosition.distanceFromBeaconMaster, travelBetweenMeas, previousReceivedPosition.distanceFromBeaconMaster);
        double da = latestReceivedPosition.angleFromBEaconMaster - previousReceivedPosition.angleFromBEaconMaster;
        if (Math.sin(da) < 0)// is this right?
            gamma = -gamma;
        return gamma;
    }

    boolean receiveFormationComplete() {// temporary
        ACLMessage msg = owner.receive(templates[TopicNames.bmToGm.ordinal()]);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                System.out.println(getBehaviourName() + " received Formation complete");
                return true;
            }
        }
        return false;
    }
boolean isInsideAreaCheck(double angle){
   double nextDist =PublicPartOfAgent.calcThirdSide(latestReceivedPosition.distanceFromBeaconMaster,travelBetweenMeas,angle+angleToBM);
   if(nextDist<maxDistFromBeacon&&nextDist>minDistFromBeacon)
       return true;
   else
       return false;
}
double getAngleToTurn(){// go stright if allowed
int counter  =0;
        double angle=0;
        while(!isInsideAreaCheck(angle)&&  counter++ <100) {
            angle = rnd.nextDouble() * 2 * Math.PI;
        }
        if(counter>=99){
            System.out.println(owner.getName()+" could not get good direction");
        }
        return angle;
    }

    @Override
    protected void onTick() {
        switch (state) {
            case INITIAL:
                if (receiveFormationComplete()) {
                    state = ExpolrerStates.EXPLORING;
                }
                break;
            case EXPLORING:
                if (owner.publicPartOfAgent.odometryTotal - previousOdo > travelBetweenMeas) {//update position
                    previousOdo = owner.publicPartOfAgent.odometryTotal;
                    measureDistanceToBeacons();

                    owner.publicPartOfAgent.movementState = MovementState.STILL;

                    state = ExpolrerStates.WAITING_DISTANCE_INF;

                } else {
                    owner.publicPartOfAgent.movementState = MovementState.FORWARD;
                }
                break;
            case WAITING_DISTANCE_INF:
                if (receivePositionInfo()) {
                    angleToBM=calculateAngleToBM();
                    owner.publicPartOfAgent.angleToSecond = angleToBM;//draws line to verify angle

                    //take new directon and continue exploring
                    double  angleToTurn = getAngleToTurn();
                    owner.publicPartOfAgent.turnRightBy(angleToTurn);

                    System.out.println(getBehaviourName() + " received  position info, angleToBm: "+Math.toDegrees(angleToBM));
                    state = ExpolrerStates.TURNING;
                }
                break;
            case TURNING:
                if (!owner.publicPartOfAgent.movementMode.equals(MovementMode.TO_ANGLE)) {//movement is done
                state=ExpolrerStates.EXPLORING;
                }
                break;
        }
    }
}
