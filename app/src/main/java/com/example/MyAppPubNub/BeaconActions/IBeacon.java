package com.example.MyAppPubNub.BeaconActions;

public class IBeacon extends BeaconDetails {
    int major;
    int minor;

    public IBeacon(String uuid, int major, int minor) {
        super(uuid);
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }
}
