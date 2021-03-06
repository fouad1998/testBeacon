package com.example.MyAppPubNub;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MainActivity";

    // required variable for program
    String buttonStatus = "Start Scanner";
    boolean enabledScanner = false;
    boolean isScanning = false;
    boolean isHandlerStillRunning = false;
    final int intervalToScan = 5000; // 5s
    ScanSettings mScanSettings;
    ScanFilter mScanFilter;
    Handler mBluetoothScannerHandler = new Handler();

    // Reference to UI elements
    Button start;
    TextView textView;

    // Bluetooth Adapter and scanner
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setScanSettings();

        // UI
        start = (Button) findViewById(R.id.start);
        textView = (TextView) findViewById(R.id.textView);
        start.setText(buttonStatus);

        if (!mBluetoothAdapter.isEnabled()) {
            start.setClickable(false);
            textView.setText("Enable your bluetooth first");
        }


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Click on the " + buttonStatus + " button");
                if (enabledScanner) {
                    enabledScanner = false;
                    buttonStatus = "Start Scanner";
                    textView.setText("Scanner stopped :)");
                } else {
                    ensureBluetoothInstance();
                    if (mBluetoothAdapter.isEnabled()) {
                        buttonStatus = "Disable the scanner";
                        enabledScanner = true;
                        try {
                            if (!isHandlerStillRunning) {
                                // If the handler is stopped we can start it again
                                isHandlerStillRunning = true;
                                mBluetoothScannerHandler.post(mBluetoothRunnable);
                            }
                        } catch (IllegalArgumentException error) {
                            enabledScanner = false;
                            buttonStatus = "Start Scanner";
                            textView.setText("Sorry we have some trouble we couldn't start, please try again.");
                            Log.e(TAG, "Threw exception because the arguments are null, so please fix this issue");
                        }
                    }
                }
                start.setText(buttonStatus);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            // Ensure the permission are satisfied
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect peripherals.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }

        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }

         */
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord mScanRecord = result.getScanRecord();
            Log.d(TAG, result.toString());
            SparseArray<byte[]> manufacturerData = mScanRecord.getManufacturerSpecificData();
            int mRssi = result.getRssi();
            Log.i(TAG, "Received data");
            Log.i(TAG, manufacturerData.toString());

            if (manufacturerData.size() > 0) {
                String template = "\nBEACON UUID: {{uuid}}\nMajor: {{major}}\nMinor: {{minor}}\ndistance: {{distance}}";
                String textContent = "";

                for (int i = 0; i < manufacturerData.size(); i++) {
                    Log.i(TAG, "data is");
                    int key = manufacturerData.keyAt(i);
                    byte[] obj = manufacturerData.get(key);
                    byte[] uuid = new byte[16];
                    byte[] major = new byte[2];
                    byte[] minor = new byte[2];
                    byte[] powerX = new byte[1];

                    Log.e(TAG, String.valueOf(obj.length));
                    try {
                        System.arraycopy(obj, 2, uuid, 0, uuid.length);
                        System.arraycopy(obj, 18, major, 0, major.length);
                        System.arraycopy(obj, 20, minor, 0, minor.length);
                        System.arraycopy(obj, 22, powerX, 0, powerX.length);

                        int powerXValue = Utils.getPower(powerX);
                        int minorValue = Utils.getMinorOrMajor(minor);
                        int majorValue = Utils.getMinorOrMajor(major);
                        double distance = Utils.calculateDistance((int) powerXValue, (double) mRssi);

                        textContent += template.replace("{{uuid}}", Utils.bytesToHex(uuid))
                                .replace("{{major}}", new Integer(majorValue).toString())
                                .replace("{{minor}}", new Integer(minorValue).toString())
                                .replace("{{distance}}", new Double(distance).toString() + "m");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, e.toString());
                    }
                }

                textView.setText(textContent);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "Started the on BatchScan");
        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan faild");
            enabledScanner = false;
            buttonStatus = "Start Scanner";
            start.setText(buttonStatus);
            textView.setText("The scan faild, and it returns the code error: " + errorCode);
        }
    };


    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    }


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
    }

    private void ensureBluetoothInstance() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (mBluetoothLeScanner == null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    private final Runnable mBluetoothRunnable = new Runnable() {
        @Override
        public void run() {
            boolean continueAfter = true;

            if (isScanning) {
                Log.d(TAG, "Stop scanner");
                isScanning = false;
                ensureBluetoothInstance();
                // The process still scanning
                mBluetoothLeScanner.stopScan(mScanCallback);
                // Check if the scanner is still enabled
                if (!enabledScanner) {
                    continueAfter = false;
                }
            } else {
                Log.d(TAG, "Starting scanner again");
                isScanning = true;
                // Is the scanner ON
                if (enabledScanner) {
                    // Verify if the bluetooth still on
                    if (mBluetoothAdapter.isEnabled()) {
                        ensureBluetoothInstance();
                        mBluetoothLeScanner.startScan(mScanCallback);
                    } else {
                        // Bluetooth adapter is off
                        continueAfter = false;
                        Log.e(TAG, "The bluetooth is off, so we can't continue scanning");
                    }
                }
            }

            // Make another call to this function after interval of time
            if (enabledScanner && continueAfter) {
                mBluetoothScannerHandler.postDelayed(this, intervalToScan);
            } else {
                isHandlerStillRunning = false;
            }
        }
    };
}