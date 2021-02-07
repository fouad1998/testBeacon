package com.example.MyAppPubNub.BeaconActions;

public abstract class BeaconDetails {
    String uuid;

    public String getUuid() {
        return uuid;
    }

    public BeaconDetails(String uuid) {
        this.uuid = uuid;
    }
}
