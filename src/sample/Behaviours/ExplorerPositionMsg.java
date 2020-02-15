package sample.Behaviours;

import java.io.Serializable;

public class ExplorerPositionMsg implements Serializable {
    double angleFromBEaconMaster;
    double distanceFromBeaconMaster;

    public ExplorerPositionMsg(double angleFromBEaconMaster, double distanceFromBeaconMaster) {
        this.angleFromBEaconMaster = angleFromBEaconMaster;
        this.distanceFromBeaconMaster = distanceFromBeaconMaster;
    }
}
