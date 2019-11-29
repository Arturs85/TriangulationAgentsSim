package sample;

import jade.core.Agent;

public class BasaeUWBAgent extends Agent {
PublicPartOfAgent publicPartOfAgent;

    @Override
    protected void setup() {
        super.setup();
    Object args[] =getArguments();
    publicPartOfAgent = (PublicPartOfAgent)args[0];

    }

    }
