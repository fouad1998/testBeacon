package com.example.MyAppPubNub;
import android.widget.ImageView;
import android.widget.TextView;

public class Scanner(ImageView imageView, ) {

    Beacon beacon = BeaconManager.getNearest();

    if(beacon instanceof Eddystone){



    }else if(beacon instanceof iBeacon){

    }

}
