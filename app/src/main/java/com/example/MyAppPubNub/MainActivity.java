package com.example.MyAppPubNub;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
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
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    private static final long SCAN_PERIOD = 1500;
    boolean isScanning = false;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    Scanner myScanner;
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
    ParcelUuid serviceUid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb");
    String[] urlSchemePrefix;
    String[] topLevelDomain;
    ImageView imageView;
    TextView instanceIdView;
    TextView instanceId;
    TextView distance;
    Context myContext;
    List<Object> myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle("Beacon Detection");

        handler = new Handler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        switchState = true;
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4287F5")));
        setScanFilter();
        setScanSettings();
        scanHandler = new Handler();

        imageView = (ImageView) findViewById(R.id.imageView);
        instanceIdView = (TextView) findViewById(R.id.instanceIdView);
        instanceId = (TextView) findViewById(R.id.instanceId);
        distance = (TextView) findViewById(R.id.distance);

        imageView = (ImageView) findViewById(R.id.imageView);
        instanceIdView = (TextView) findViewById(R.id.instanceIdView);
        distance = (TextView) findViewById(R.id.distance);

        try{
            this.myScanner = new Scanner(this, instanceIdView, imageView);
        }catch (Exception e) {
            Log.e(TAG, e.toString());
            System.exit(-1);
        }


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
                                mBluetoothLeScanner.startScan(Arrays.asList(mScanFilter), mScanSettings, myScanner);
                                isScanning = true;
                                floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.avd_play_to_pause, null));
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
                                mBluetoothLeScanner.stopScan(myScanner);
                                isScanning = false;

                                floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.avd_pause_to_play, null));
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
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    }

    public void settContext(Context c){
        this.myContext = c;
    }
}




