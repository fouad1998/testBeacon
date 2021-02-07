package com.example.MyAppPubNub.BeaconManager;

import java.time.Instant;
import java.sql.Timestamp;


public abstract class Beacon {
    private String uuid;
    private float distance;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public Beacon(String uuid, float distance) {
        this.uuid = uuid;
        this.distance = distance;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestamp.getTime();
    }

    public void setDistance(float distance) {
        this.distance = distance;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestamp.getTime();
    }

    public String getUuid() {
        return uuid;
    }

    public float getDistance() {
        return distance;
    }

    public String toString() {
        return "Beacon: uuid:" + uuid + ", distance: " +distance;
    }
}
