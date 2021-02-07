package com.example.MyAppPubNub.BeaconManager;

public class Eddystone extends Beacon {
    String instanceID;

    public Eddystone(String namespace, String instanceID, float distance) throws Exception {
        super(namespace, distance);

        if (namespace.length() !=20 ) {
            throw new Exception("The namespace must have 20 caracters length");
        }

        if (instanceID.length() != 12) {
            throw new Exception("The instanceID must have 16 caracters length");
        }

        this.instanceID = instanceID;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String toString(){
        return "Eddystone Beacon: namespace: " +getUuid()+", instanceid: " + instanceID + ", distance: " + getDistance();

    }
}
