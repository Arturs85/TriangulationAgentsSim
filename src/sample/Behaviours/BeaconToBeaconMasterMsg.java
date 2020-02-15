package sample.Behaviours;

import jade.core.AID;

import java.io.Serializable;

public class BeaconToBeaconMasterMsg implements Serializable {
AID explorer;
double distance;

    public BeaconToBeaconMasterMsg(AID explorer, double distance, AID beacon) {
        this.explorer = explorer;
        this.distance = distance;
        this.beacon = beacon;
    }

    AID beacon;
}
