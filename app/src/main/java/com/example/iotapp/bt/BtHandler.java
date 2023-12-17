package com.example.iotapp.bt;

import static com.example.iotapp.LogHandler.Log;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BtHandler {
    private static BtHandler sInstance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic btCharacteristic;
    private ScanCallback mScanCB;
    private ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    private boolean dataAvailable = false;
    private byte[] availableData;

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
                            Log("Missing permission: " + item.getPermissionName())
                        }

                        startScanningDevices(ctx);
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
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

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device, Context ctx) {
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log("onConnectionStateChange status: " + status + " newState:" + newState);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        // will trigger onServicesDiscovered
                        bluetoothGatt.discoverServices();
                    } else {
                        gatt.close();
                        Log("GATT DISCONNECTED");
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // DEVICE SERVICES
                    Log("Looking for services");
                    List<BluetoothGattService> gattServices = bluetoothGatt.getServices();
                    BluetoothGattService foundService = null;
                    for (BluetoothGattService gattServiceItem : gattServices) {
                        UUID uuid = gattServiceItem.getUuid();
                        Log("UUID (service): " + uuid.toString());
                        foundService = gattServiceItem;
                    }
                    if (foundService == null) {
                        Log("No service found");
                    } else {
                        Log("Amount of services found: " + gattServices.size());
                        btCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("19b10001e8f2537e4f6cd104768a1215"));
                    }
                } else {
                    Log("Could not discover services");
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value, int status) {
                super.onCharacteristicRead(gatt, characteristic, value, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log("Read data successfully");
                    Log("Data => " + value);
                    availableData = value;
                    dataAvailable = true;
                } else {
                    Log("Failed to read data");
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS: {
                        break;
                    }
                    case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED: {
                        Log("GATT_REQUEST_NOT_SUPPORTED");
                        break;
                    }
                    case BluetoothGatt.GATT_READ_NOT_PERMITTED: {
                        Log("GATT_READ_NOT_PERMITTED");
                        break;
                    }
                    case BluetoothGatt.GATT_WRITE_NOT_PERMITTED: {
                        Log("GATT_WRITE_NOT_PERMITTED");
                        break;
                    }
                    default: {
                        Log("BT Characteristic fail");
                        break;
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log("onDescriptorWrite");
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    return;
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                //byte[] data = characteristic.getValue();
                Log("onCharacteristicChanged");
            }
        };

        bluetoothGatt = device.connectGatt(ctx, false, gattCallback);
    }

    public boolean isConnected() {
        return btCharacteristic != null;
    }

    public String getDataIfAvailable() {
        if (dataAvailable) {
            dataAvailable = false;
            return new String(availableData, StandardCharsets.UTF_8);
        }

        return "";
    }

    @SuppressLint("MissingPermission")
    public void mockReadNumberplate() {
        bluetoothGatt.readCharacteristic(btCharacteristic);
    }

    @SuppressLint("MissingPermission")
    public void writeData(byte data) {
        byte[] value = new byte[]{data};
        bluetoothGatt.writeCharacteristic(btCharacteristic, value, WRITE_TYPE_NO_RESPONSE);
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
