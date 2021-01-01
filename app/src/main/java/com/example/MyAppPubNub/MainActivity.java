package com.example.MyAppPubNub;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final static String TAG ="MainActivity";
    String buttonStatus = "Start Scanner";
    boolean enabledScanner = false;
    Button start;
    TextView textView;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    ScanSettings mScanSettings;
    ScanFilter mScanFilter;

    String string = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        setScanSettings();

        // UI
        start = (Button) findViewById(R.id.start);
        textView = (TextView) findViewById(R.id.textView);

        start.setText(buttonStatus);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }
    }

    public void onClick(View view){
        Log.i(TAG, "Click on the " +buttonStatus +" button");
        if (enabledScanner) {
            enabledScanner = false;
            mBluetoothLeScanner.stopScan(stoppedScanner);
            buttonStatus = "Start Scanner";
            start.setText(buttonStatus);
            textView.setText("Scanner stopped :)");
        }else{
            buttonStatus = "Disable the scanner";
            enabledScanner = true;
            start.setText(buttonStatus);
            try {
                mBluetoothLeScanner.startScan(mScanCallback);
            } catch (IllegalArgumentException error) {
                enabledScanner = false;
                buttonStatus = "Start Scanner";
                start.setText(buttonStatus);
                mBluetoothLeScanner.startScan(mScanCallback);
                textView.setText("Sorry we have some trouble we couldn't start, please try again.");
                Log.e(TAG, "Threw exception because the arguments are null, so please fix this issue");
            }
        }
    }

    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "Starting the scanner");
            ScanRecord mScanRecord = result.getScanRecord();
            Iterator<Map.Entry<ParcelUuid, byte[]>> iterator = mScanRecord.getServiceData().entrySet().iterator();
            Log.d(TAG, "//////////////////////////////////////////////");
            Log.d(TAG, "Received data [" + mScanRecord.getServiceData().size() + "]");
            while (iterator.hasNext()) {
                Map.Entry<ParcelUuid, byte[]> entry = iterator.next();
                Log.d(TAG, entry.getKey().toString() + ":" + entry.getValue());
            }
            Log.d(TAG, "End Received data");
            Log.d(TAG, "//////////////////////////////////////////////");
            SparseArray<byte[]> manufacturerData = mScanRecord.getManufacturerSpecificData();
            int mRssi = result.getRssi();
            Log.i(TAG, "Received data");
            Log.i(TAG, manufacturerData.toString());

            if(manufacturerData.size()>0) {
                for(int i = 0; i < manufacturerData.size(); i++) {
                    Log.i(TAG, "data is");

                    int key = manufacturerData.keyAt(i);
                    byte[] obj = manufacturerData.get(key);

                    string = "";

                    Log.i(TAG, "Payload length: " + obj.length);

                    for(byte part : obj){
                        string = string + String.valueOf(part) + "  ";
                    }

                    textView.setText(string);
                    Log.i(TAG, string);

                }
            }
        }

        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan faild");
            enabledScanner = false;
            buttonStatus = "Start Scanner";
            start.setText(buttonStatus);
            textView.setText("The scan faild, and it returns the code error: " + errorCode);
        }


    };

    private ScanCallback stoppedScanner = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(TAG, "Scanner ended");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "faild to stop the scanner");
        }
    };

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    };

    private void setScanFilter() {
        /*
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
        byte[] uuid = []byte(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020"));
        mManufacturerData.put(0, (byte)0xBE);
        mManufacturerData.put(1, (byte)0xAC);
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]);
        }
        for (int i=0; i<=17; i++) {
            mManufacturerDataMask.put((byte)0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        mScanFilter = mBuilder.build();
        /
         */
    };

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }


    private String getDistance(double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1) {
            return "Immediate";
        } else if (accuracy < 3) {
            return "Near";
        } else {
            return "Far";
        }
    }
}