package com.example.MyAppPubNub;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    private static final long SCAN_PERIOD = 2000;
    boolean isScanning = false;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    LocationManager locationManager;
    Handler handler;
    ListView listView;
    ArrayList<String[]> beaconList;
    FloatingActionButton floatingActionButton;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    boolean switchState;
    SparseArray<byte[]> manufacturerData;
    ScanFilter mScanFilter;
    ScanSettings mScanSettings;
    int counter = 0;
    Handler scanHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle("Beacon Detection");

        beaconList = new ArrayList<String[]>();
        handler = new Handler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listView = (ListView) findViewById(R.id.listView);
        switchState = true;
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4287F5")));
        setScanFilter();
        setScanSettings();
        scanHandler = new Handler();

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mBluetoothAdapter.isEnabled()) {
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            if (!isScanning) {
                                mBluetoothLeScanner.startScan(Arrays.asList(mScanFilter), mScanSettings, mScanCallback);
                                isScanning = true;
                                floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.avd_play_to_pause));
                                Drawable drawable = floatingActionButton.getDrawable();
                                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DA2D21")));
                                if (drawable instanceof AnimatedVectorDrawableCompat) {
                                    avd = (AnimatedVectorDrawableCompat) drawable;
                                    avd.start();
                                } else if (drawable instanceof AnimatedVectorDrawable) {
                                    avd2 = (AnimatedVectorDrawable) drawable;
                                    avd2.start();
                                }
                                // Stops scanning after a pre-defined scan period.
                                /** handler.postDelayed(new Runnable() {
                                @Override public void run() {
                                isScanning = false;
                                mBluetoothLeScanner.stopScan(mScanCallback);
                                }
                                }, SCAN_PERIOD);**/

                            } else {
                                mBluetoothLeScanner.stopScan(mScanCallback);
                                isScanning = false;

                                floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.avd_pause_to_play));
                                Drawable drawable = floatingActionButton.getDrawable();
                                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4287F5")));

                                if (drawable instanceof AnimatedVectorDrawableCompat) {
                                    avd = (AnimatedVectorDrawableCompat) drawable;
                                    avd.start();
                                } else if (drawable instanceof AnimatedVectorDrawable) {
                                    avd2 = (AnimatedVectorDrawable) drawable;
                                    avd2.start();
                                }
                            }
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            alertDialog.setTitle("GPS setting!");
                            alertDialog.setMessage("You have to enable your GPS!");
                            alertDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    MainActivity.this.startActivity(intent);
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();
                        }
                    } else {
                        Intent i = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(i);
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
    }

    private void setScanFilter() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        mScanFilter = mBuilder.build();
    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(SCAN_PERIOD);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        mScanSettings = mBuilder.build();
    }

    protected ScanCallback mScanCallback = new ScanCallback() {

        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            beaconList.clear();

            for (int j = 0; j < results.size(); j++) {
                try {

                    counter = counter + 1;
                    ScanRecord mScanRecord = results.get(j).getScanRecord();
                    manufacturerData = mScanRecord.getManufacturerSpecificData();
                    int mRsi = results.get(j).getRssi();

                    if (manufacturerData.size() > 0) {
                        for (int i = 0; i < manufacturerData.size(); i++) {
                            int key = manufacturerData.keyAt(i);
                            byte[] obj = manufacturerData.get(key);
                            byte[] uuid = new byte[16];
                            byte[] major = new byte[2];
                            byte[] minor = new byte[2];
                            byte[] powerX = new byte[1];

                            if (obj[0] == 2 && obj[1] == 21) {
                                System.arraycopy(obj, 2, uuid, 0, uuid.length);
                                System.arraycopy(obj, 18, major, 0, major.length);
                                System.arraycopy(obj, 20, minor, 0, minor.length);
                                System.arraycopy(obj, 22, powerX, 0, powerX.length);
                                int powerXValue = Utils.getPower(powerX);
                                int minorValue = Utils.getMinorOrMajor(minor);
                                int majorValue = Utils.getMinorOrMajor(major);
                                double distance = Utils.calculateDistance((int) powerXValue, (double) mRsi);
                                beaconList.add(new String[]{Utils.bytesToHex(uuid), String.valueOf(majorValue), String.valueOf(minorValue), String.format("%.2f", distance)});
                            }
                        }
                        //programAdapter = new ProgramAdapter(MainActivity.this, beaconList);
                        //recyclerView.setAdapter(programAdapter);

                        ProgramAdapter whatever = new ProgramAdapter(MainActivity.this, beaconList);
                        listView.setAdapter(whatever);

                    }

                } catch (Exception e) {
                    Log.i("yacine", "yacine");
                }
            }
        }
    };
}




