package com.example.MyAppPubNub.BeaconManager;

import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;

public class BeaconManager {
    final static String TAG = "BeaconManager";
    final int timeout = 50000;

    private ArrayList<Beacon> beacons;

    public BeaconManager() throws Exception{
        beacons = new ArrayList<Beacon>();
    }

    public void addEddyBeacon(String namespace, String instanceID,float distance) throws Exception{
        boolean isAllowedToAdd = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long now = timestamp.getTime();

        for (int index  = 0; index < beacons.size();) {
           try {
            Beacon b = this.beacons.get(index);

           // CHeck if the beacon is instance of eddystone or not
            if (b instanceof  Eddystone) {
                Eddystone e = (Eddystone) b;
                if (e.getUuid() == namespace && e.getInstanceID() == instanceID){
                    e.setDistance(distance);
                    isAllowedToAdd = false;
                }
            }

            // Check if the beacon dead or not
            if (now - b.getTimestamp() > timeout){
                // The beacon in this case is dead
                beacons.remove(index);
                continue;
            }
           } catch (Exception e) {
                Log.e(TAG, "Error on the beacon manager, error is: " + e.toString());
           }

            ++index;
        }

        if (isAllowedToAdd) {
            beacons.add(new Eddystone(namespace, instanceID, distance));
        }
    }


    public void addIBeacon(String uuid, int major, int minor, float distance) throws  Exception{
        boolean isAllowedToAdd = true;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long now = timestamp.getTime();

        for (int index  = 0; index < beacons.size();) {
            try {
                Beacon b = this.beacons.get(index);

                // CHeck if the beacon is instance of eddystone or not
                if (b instanceof IBeacon) {
                    IBeacon i = (IBeacon) b;
                    if (i.getUuid() == uuid && i.getMajor() == major && i.getMinor() == minor){
                        i.setDistance(distance);
                        isAllowedToAdd = false;
                    }
                }

                // Check if the beacon dead or not
                if (now - b.getTimestamp() > timeout){
                    // The beacon in this case is dead
                    beacons.remove(index);
                    continue;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error on the beacon manager, error is: " + e.toString());
            }

            ++index;
        }

        if (isAllowedToAdd) {
            beacons.add(new IBeacon(uuid,major, minor, distance));
        }
    }

    public Beacon getNearest(){
        if (beacons.size() > 0) {
            float lowestDistance = beacons.get(0).getDistance();
            Beacon nearestBeacon = beacons.get(0);

            for (Beacon b:beacons){
                final float distance = b.getDistance();
                if (distance < lowestDistance) {
                    lowestDistance = distance;
                    nearestBeacon = b;
                }
            }

            return nearestBeacon;
        }

        return null;
    }
}
