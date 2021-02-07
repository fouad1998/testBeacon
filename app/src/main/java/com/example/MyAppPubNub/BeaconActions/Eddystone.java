package com.example.MyAppPubNub.BeaconActions;

public class Eddystone extends BeaconDetails {
    String instanceID;

    public String getInstanceID() {
        return instanceID;
    }

    public Eddystone(String uuid, String instanceID) {
        super(uuid);
        this.instanceID = instanceID;
    }
}
