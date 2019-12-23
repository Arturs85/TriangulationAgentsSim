package sample;

import jade.core.AID;
import jade.core.Agent;
import jade.core.BehaviourID;
import jade.core.ServiceException;
import jade.core.behaviours.Behaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.Behaviours.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class BaseUWBAgent extends Agent {
    public PublicPartOfAgent publicPartOfAgent;
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;
    public MessageTemplate requestTamplate;
    public MessageTemplate informTamplate;
    public MessageTemplate agreeTemplate;
public Set<Behaviour> behaviours=  new TreeSet<>(new BehavioursComparator());
    public static final int tickerPeriod = 50;//ms
public static String conversationAssignRole= "assignRole";//for Conversation ID

    @Override
    protected void setup() {
        super.setup();
        Object args[] = getArguments();
        publicPartOfAgent = (PublicPartOfAgent) args[0];

        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            requestTamplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            informTamplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            agreeTemplate = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
        } catch (
                ServiceException e) {
            e.printStackTrace();
        }

        //      addBehaviour(new RandomRoamingBehaviour(this, tickerPeriod));
        if (publicPartOfAgent.agentNumber == 2) {
            addBehaviour(new TwoPointFormationRequest(this));
            publicPartOfAgent.y += 20;
            publicPartOfAgent.x += 220;

        } else if (publicPartOfAgent.agentNumber == 1){
            addBehaviour(new TwoPointFormationResponderBehaviour(this));
            addBehaviour(new GrandMasterBehaviour(this));
    }else if (publicPartOfAgent.agentNumber == 3){
            addBehaviour(new BeaconsMasterBehaviour(this));
            publicPartOfAgent.y += 250;
            publicPartOfAgent.x += 100;

        }
addBehaviour(new BaseCommandListeningBehaviour(this));
    }

    public AID createTopicForBehaviour(String name) {
        if (topicHelper == null) return null;

        return topicHelper.createTopic(name);
    }
    public void registerBehaviourToTopic(AID topic) {
        if (topicHelper == null) return;

        try {
            topicHelper.register(topic);
        } catch (ServiceException e) {
            e.printStackTrace(); // what to do here?
        }
    }
@Override
    public void removeBehaviour(Behaviour behaviour) {
    super.removeBehaviour(behaviour);
    behaviours.remove(behaviour);
    }
    @Override
    public void addBehaviour(Behaviour behaviour) {//adds behaviour and  removes all conflicting ones

        if((!(behaviour.getClass().getName().equals(BaseCommandListeningBehaviour.class.getName()))) && (!(behaviour.getClass().getName().equals(GrandMasterBehaviour.class.getName())))){//remove conflicting  behaviours
            ArrayList<Behaviour> blist  = new ArrayList<>(behaviours);
    for (Behaviour b:blist ) {
        if(b.getClass().getName().equals(PathFinderBehaviour.class.getName())||
                b.getClass().getName().equals(BeaconsMasterBehaviour.class.getName())||
                b.getClass().getName().equals(TwoPointFormationResponderBehaviour.class.getName())||
                b.getClass().getName().equals(TwoPointFormationRequest.class.getName())
                ){

            behaviours.remove(b);
            System.out.println(getName()+" removed behaviour: "+b.getBehaviourName());
        }

    }

}
      boolean  success=  behaviours.add(behaviour);
if(success) {
    super.addBehaviour(behaviour);
    System.out.println(getName()+" added behaviour: "+behaviour.getBehaviourName());

}
    }
    class BehavioursComparator implements Comparator<Behaviour> {
        public int compare(Behaviour b1 , Behaviour b2)
        {

            return b1.getClass().getName().compareTo(b2.getClass().getName());
        }
    };
}
