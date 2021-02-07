package com.example.MyAppPubNub.BeaconManager;

public class IBeacon extends Beacon {
    int major;
    int minor;

    public IBeacon(String uuid, int major, int minor, float distance) throws Exception {
        super(uuid, distance);
        if (uuid.length() != 32) {
            throw new Exception("The Ibeacon UUID must have 32 caracters");
        }
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String toString() {
        return "IBeacon: uuid: " + getUuid() + ", Major: " + major + ", Minor: " + minor + ", distance: " + getDistance();
    }
}
