package com.example.iotapp.bt;

import static com.example.iotapp.LogHandler.Log;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class BtHandler {
    private static BtHandler sInstance;
    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback mScanCB;
    private ArrayList<BluetoothDevice> btDevices = new ArrayList<>();

    private BtHandler() {}

    public static BtHandler getInstance() {
        if (sInstance == null) {
            sInstance = new BtHandler();
        }

        return sInstance;
    }

    public void requestPermissionsAndScan(Context ctx) {
        if (!hasPermissions(ctx)) {
            String[] permissionArr = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};

            Dexter.withContext(ctx).withPermissions(permissionArr).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        Log("All permissions granted");

                        startScanningDevices(ctx);
                    }

                    if (report.isAnyPermissionPermanentlyDenied()) {
                        List<PermissionDeniedResponse> a = report.getDeniedPermissionResponses();
                        for (PermissionDeniedResponse item : a) {
                            Log("Missing permission: " + item.getPermissionName());
                        }

                        startScanningDevices(ctx);
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
        } else {
            // has permissions
            startScanningDevices(ctx);
        }
    }

    @SuppressLint("MissingPermission")
    private void startScanningDevices(Context ctx) {
        mScanCB = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                @SuppressLint("MissingPermission") String deviceName = device.getName();

                if (deviceName == null) {
                    return;
                }

                Log("New device discovered: " + deviceName + " - address: " + device.getAddress());

                if (!deviceExists(device)) {
                    btDevices.add(device);
                }
            }
        };

        BluetoothManager bluetoothManager = (BluetoothManager)ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCB);
    }

    public ArrayList<BluetoothDevice> getBtDevices() {
        return btDevices;
    }

    @SuppressLint("MissingPermission")
    public void stopScanningDevices() {
        Log("Stop scanning devices");
        bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCB);
    }

    public BluetoothDevice getBtDeviceByAddress(String address) {
        for (BluetoothDevice item : btDevices) {
            if (item.getAddress().equals(address)) {
                return item;
            }
        }

        return null;
    }

    private boolean hasPermissions(Context ctx) {
        return (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean deviceExists(BluetoothDevice device) {
        for (BluetoothDevice item : btDevices) {
            if (item.getAddress().equals(device.getAddress())) {
                return true;
            }
        }

        return false;
    }
}
