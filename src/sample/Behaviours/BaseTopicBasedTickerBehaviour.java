package sample.Behaviours;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sample.BaseUWBAgent;

import java.io.IOException;


public abstract class BaseTopicBasedTickerBehaviour extends TickerBehaviour {
    BaseUWBAgent owner;
    public AID[] sendingTopics;
    public AID[] receivingTopics;
    public MessageTemplate[] templates;// for receiving  messages  of particular topic
public String shortName;
    void createSendingTopic(TopicNames topicName) {
        sendingTopics[topicName.ordinal()] = owner.createTopicForBehaviour(topicName.name());
    }


    void createAndRegisterReceivingTopics(TopicNames topicName) {
        receivingTopics[topicName.ordinal()] = owner.createTopicForBehaviour(topicName.name());
        owner.registerBehaviourToTopic(receivingTopics[topicName.ordinal()]);
        templates[topicName.ordinal()] = MessageTemplate.MatchTopic(receivingTopics[topicName.ordinal()]);
    }


    public BaseTopicBasedTickerBehaviour(BaseUWBAgent a) {

        super(a, BaseUWBAgent.tickerPeriod);
        owner = a;
    sendingTopics=new AID[TopicNames.values().length];
    receivingTopics=new AID[TopicNames.values().length];
    templates = new MessageTemplate[TopicNames.values().length];
    }


}
