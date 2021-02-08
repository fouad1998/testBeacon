package com.example.MyAppPubNub;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.MyAppPubNub.BeaconManager.Beacon;
import com.example.MyAppPubNub.BeaconManager.BeaconManager;
import com.example.MyAppPubNub.BeaconManager.Eddystone;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Scanner extends ScanCallback {
    final static String TAG = "Scanner";
    private BeaconManager beaconManager;

    //UI element
    TextView myTextView;
    ImageView myImageView;
    ImageView myImageView2;
    Context myContext;

    //constant
    final String[] urlSchemePrefix = new String[]{"http://www.", "https://www.", "http://", "https://"};
    final String[] topLevelDomain = new String[]{".com/", ".org/", ".edu/", ".net/", ".info/", ".biz/", ".gov/", ".com", ".org", ".edu", ".net", ".info", "biz", ".gov"};


    Scanner(TextView textView, ImageView imageView, ImageView imageView2) throws Exception {
        beaconManager = new BeaconManager();
        myImageView = imageView;
        myTextView = textView;
        myImageView2 = imageView2;
    }

    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        beaconManager.clean();
        Log.i(TAG, "On batch called, size of result is : " + results.size());

        for (int index = 0; index < results.size(); ++index) {
            try {
                ScanRecord mScanRecord = results.get(index).getScanRecord();
                byte[] iBeaconFrame = mScanRecord.getManufacturerSpecificData(76);
                int mRsi = results.get(index).getRssi();

                if (iBeaconFrame != null && iBeaconFrame.length != 0) {
                    byte[] uuid = new byte[16];
                    byte[] major = new byte[2];
                    byte[] minor = new byte[2];
                    byte[] powerX = new byte[1];

                    if (iBeaconFrame[0] == 2 && iBeaconFrame[1] == 21) {
                        System.arraycopy(iBeaconFrame, 2, uuid, 0, uuid.length);
                        System.arraycopy(iBeaconFrame, 18, major, 0, major.length);
                        System.arraycopy(iBeaconFrame, 20, minor, 0, minor.length);
                        System.arraycopy(iBeaconFrame, 22, powerX, 0, powerX.length);
                        int powerXValue = Utils.getPower(powerX);
                        int minorValue = Utils.getMinorOrMajor(minor);
                        int majorValue = Utils.getMinorOrMajor(major);
                        double distance = Utils.calculateDistance((int) powerXValue, (double) mRsi);
                        beaconManager.addIBeacon(Utils.bytesToHex(uuid),majorValue, minorValue, (float) distance);
                    }
                } else {
                    Map<ParcelUuid, byte[]> myMap = mScanRecord.getServiceData();
                    String url = "";
                    byte[] txPower = new byte[1];
                    byte[] nameSpaceId = new byte[10];
                    byte[] instanceId = new byte[6];
                    float distance;

                    for (Map.Entry<ParcelUuid, byte[]> eddystoneFrame : myMap.entrySet()) {
                        // for eddystone URL
                        if (eddystoneFrame.getValue()[0] == 16) {
                            url += urlSchemePrefix[eddystoneFrame.getValue()[2]];

                            for (int i = 3; i < eddystoneFrame.getValue().length - 1; i++) {
                                url += (char) eddystoneFrame.getValue()[i];
                            }

                            url += topLevelDomain[eddystoneFrame.getValue()[eddystoneFrame.getValue().length - 1]];
                            txPower[0] = eddystoneFrame.getValue()[1];
                            distance = (float) Utils.calculateDistance((int) txPower[0], (double) mRsi);

                            try {
                                beaconManager.addEddyBeacon(Utils.bytesToHex(nameSpaceId), Utils.bytesToHex(instanceId), distance);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }

                        } else if (eddystoneFrame.getValue()[0] == 0) {
                            // For Eddystone UID
                            System.arraycopy(eddystoneFrame.getValue(), 2, nameSpaceId, 0, nameSpaceId.length);
                            System.arraycopy(eddystoneFrame.getValue(), 12, instanceId, 0, instanceId.length);
                            System.arraycopy(eddystoneFrame.getValue(), 1, txPower, 0, txPower.length);

                            distance = (float) Utils.calculateDistance((int) txPower[0], (double) mRsi);

                            /**  beaconList.add(new String[]{"Name Space ID : " + Utils.bytesToHex(nameSpaceId)+ "\n" + "Instance ID :" + Utils.bytesToHex(instanceId),
                             String.format("%.2f", distance),
                             "eddystoneuid"});**/
                            try {
                                beaconManager.addEddyBeacon(Utils.bytesToHex(nameSpaceId), Utils.bytesToHex(instanceId), distance);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }

                        }
                        Log.i(TAG, "The size of the frame is: " + eddystoneFrame.getValue().length);
                    }

                }


            } catch (Exception e) {
                Log.e("Error123456789", e.toString());
                break;
            }
        }

        Beacon b = beaconManager.getNearest();
        if (b != null) {
            myTextView.setText(b.toString());
            if (b.getUuid().equals("33963772448957556609")) {
                Log.i(TAG, "Same uuid");
                myImageView.setVisibility(View.VISIBLE);
                myImageView2.setVisibility(View.INVISIBLE);
            } else {
                Log.i(TAG, "not Same uuid");
                myImageView.setVisibility(View.INVISIBLE);
                myImageView2.setVisibility(View.VISIBLE);
            }

        } else {
            myTextView.setText("No beacon detected");
            myImageView.setVisibility(View.INVISIBLE);
            myImageView2.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);

        Log.i(TAG, "On result called");
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.i(TAG, "On scan faild");
    }
}
