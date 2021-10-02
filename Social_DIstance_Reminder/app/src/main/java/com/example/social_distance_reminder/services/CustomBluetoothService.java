package com.example.social_distance_reminder.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.social_distance_reminder.broadcastrecievers.NetworkStateReceiver;
import com.example.social_distance_reminder.db.crudhelper.SqlLiteHelper;
import com.example.social_distance_reminder.exceptions.BeaconNotSupportedException;
import com.example.social_distance_reminder.exceptions.BluetoothNotSupportException;
import com.example.social_distance_reminder.helper.BackgroundTaskHelper;
import com.example.social_distance_reminder.helper.BluetoothHelper;
import com.example.social_distance_reminder.helper.NotificationHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;
import static com.example.social_distance_reminder.helper.RandomIDGenerator.getForegroundID;

public class CustomBluetoothService extends Service implements BeaconConsumer, NetworkStateReceiver.NetworkStateReceiverListener {
    private static String deviceId;
    private BeaconTransmitter beaconTransmitter;
    private Beacon beacon;
    private String BEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private BeaconParser beaconParser = new BeaconParser()
            .setBeaconLayout(BEACON_LAYOUT);
    private BeaconManager beaconManager;

    private final NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();
    private SqlLiteHelper sqlLiteHelper;

    public CustomBluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(getForegroundID(), NotificationHelper.createBackgroundNotificationForService(
                "Social Distance Remonder",
                "Background Running...",
                getApplicationContext()
        ));
        startTransmitAndScan();
        return START_STICKY;
    }

    private void startTransmitAndScan() {
        BluetoothAdapter mBluetoothAdapter = null;
        try {
            mBluetoothAdapter = BluetoothHelper.getBluetoothAdapter();
        } catch (BluetoothNotSupportException e) {
            //TODO: HANDLE THIS
            Log.d(TAG, "onCreate: devise bluetooth not supported");
        }
        if (mBluetoothAdapter == null) {
            //TODO: HANDLE THIS
            Log.d(TAG, "onCreate: devise bluetooth not supported");
        } else if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onCreate: bluetooth is disabled");
            //setBluetooth(true);
            try {
                BluetoothHelper.requestBluetooth(this);
            } catch (BluetoothNotSupportException e) {
                //TODO: HANDLE THIS
                Log.d(TAG, "onCreate: devise bluetooth not supported");
            }
        } else {
//            if (DatabaseHelper.getInstance(this).isAllowed()
//                    && deviceId != null) {
                setupTransmit(deviceId);
                setupScan();
//            } else {
//                if (DatabaseHelper.getInstance(this).isAllowed() && deviceId == null) {
//                    deviceId = DatabaseHelper.getInstance(this).getUserId();
//                    setupTransmit(deviceId);
//                    setupScan();
//                } else {
//                    stopSelf();
//                }
//            }
        }
    }

    private void setupScan() {
        System.out.println("\n\nHere is the setupScan method\n\n");
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(this);

            // To detect proprietary beacons, you must add a line like below corresponding to your beacon
            // type.  Do a web search for "setBeaconLayout" to get the proper expression.
            beaconManager.getBeaconParsers().add(beaconParser);
            beaconManager.setEnableScheduledScanJobs(false);
       /* beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(10000);*/
            beaconManager.bind(this);
        }
    }

    private void setupTransmit(String deviceId) {
        System.out.println("\n\nHere is the setupTransmit method\n\n");

        int result = BeaconTransmitter.checkTransmissionSupported(getApplicationContext());
        if(result != BeaconTransmitter.SUPPORTED)
            try {
                //TODO: HANDLE
                throw new BeaconNotSupportedException("Device Does not support for Beacon Transmitting");
            } catch (BeaconNotSupportedException e) {
                System.out.println("\n\n" + e.getMessage()+"\n\n");
        }

        if (beaconTransmitter == null) {
            beaconTransmitter = new BeaconTransmitter(this, beaconParser);
        }

        if (beacon == null) {
            beacon = new Beacon.Builder()
                    .setId1(deviceId) // need to generate ids device specific
                    .setId2("1")
                    .setId3("2")
                    .setManufacturer(0x0118)
                    .setTxPower(-81)
                    .setDataFields(Collections.singletonList(0L))
                    .build();
        }
   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        beaconTransmitter.setAdvertiseTxPowerLevel(ADVERTISE_TX_POWER_LOW);
    }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.i(TAG, "Advertisement of Beacons start succeeded.");
                    System.out.println("Advertisement of Beacons start succeeded.");
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.e(TAG, "Advertisement of Beacons start failed with code: " + errorCode);
                    System.out.println("Advertisement of Beacons start succeeded.");
                    System.out.println("Advertisement of Beacons start failed with code: " + errorCode);
                    System.out.println("");
                    System.out.println("");
                }
            });

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerBroadcastReceivers();
        registerReceiver(mReceiver, filter);
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        sqlLiteHelper = SqlLiteHelper.getInstance(this);
        deviceId = sqlLiteHelper.getUserId();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Bluetooth Service is Going to Destroy");
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "No Reciever got registered"); // check how this cause
        }
        beaconManager.unbind(this);
    }

//    private void registerBroadcastReceivers() {
//        // Create a new broadcast intent filter that will filter and
//        // receive ACTION_VIEW_LOCAL intents.
//        IntentFilter intentFilter =
//                new IntentFilter(CustomBluetoothService.ACTION_LOCATION_ACCESS_STATE_CHANGE);
//
//        // Call the Activity class helper method to register this
//        // local receiver instance.
//        LocalBroadcastManager.getInstance(this)
//                .registerReceiver(locationAccessStateChangeReceiver,
//                        intentFilter);
//
//        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
//
//        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(ACTION_PROVIDERS_CHANGED));
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(keepTransmitterAliveReceiver, new IntentFilter(ACTION_KEEP_TRANSMITTER_ALIVE));
//    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //
                        try {
                            BluetoothHelper.requestBluetooth(CustomBluetoothService.this);
                        } catch (BluetoothNotSupportException e) {
                            //TODO: HANDLE THIS
                            e.printStackTrace();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //when ever user try to turn off bluetooth this method swill turn it on again
                         BluetoothHelper.setBluetooth(true);
                        break;
                    case BluetoothAdapter.STATE_ON:
//                        if (database_helper.isAllowed()
//                                && deviceId != null) {
                            setupTransmit(deviceId);
                            setupScan();
//                        } else {
//                            if (deviceId == null) {
//                                deviceId = database_helper.getUserId();
//                                setupTransmit(deviceId);
//                                setupScan();
//                            } else {
                        //TODO:HANDLE
//                                stopSelf();
//                            }
//                        }

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Keep it on, Noting to do
                        break;
                }
            }
        }
    };

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(
            (beacons, region) -> {
                for (Beacon beacon : beacons) {
                    Log.d(TAG, "I see a beacon ");
                    double distance = beacon.getDistance();
                    System.out.println("Beacon Detected - " + beacon.getId1().toString());

                    //TODO:CHECK FOR DISTANCE
                    sqlLiteHelper.addDevice(beacon.getId1().toString(), 0, 0);
                }
        });
        try {
            beaconManager.setForegroundScanPeriod(10000);
            beaconManager.setForegroundBetweenScanPeriod(10000);
            beaconManager.updateScanPeriods();
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {

        }
    }

    @Override
    public void networkAvailable() {
        new BackgroundTaskHelper.UploadTask().execute(this); // called after network state changed from disable to enable
    }

    @Override
    public void networkUnavailable() {
        Log.d(TAG, "networkUnavailable: ");
    }
}