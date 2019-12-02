package sample;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.Behaviours.RandomRoamingBehaviour;

public class BaseUWBAgent extends Agent {
    public PublicPartOfAgent publicPartOfAgent;
    TopicManagementHelper topicHelper = null;
    public AID uiTopic;
    public MessageTemplate requestTamplate;
    public MessageTemplate informTamplate;
    public MessageTemplate agreeTemplate;

    public static final int tickerPeriod = 500;//ms

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

        addBehaviour(new RandomRoamingBehaviour(this, tickerPeriod));
    }

}
