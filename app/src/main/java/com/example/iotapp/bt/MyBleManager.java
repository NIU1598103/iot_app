package com.example.iotapp.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.iotapp.LogHandler;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class MyBleManager extends BleManager {
    private static final String TAG = "MyBleManager";
    private static final UUID SERVICE_UUID = UUID.fromString("19b10001-e8f2-537e-4f6c-d104768a1215");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("19b10001-e8f2-537e-4f6c-d104768a1215");

    private boolean mConnected;
    private boolean dataAvailable = false;
    private String availableData;

    public MyBleManager(@NonNull final Context context) {
        super(context);
    }

    // ==== Logging =====

    @Override
    public int getMinLogPriority() {
        // Use to return minimal desired logging priority.
        return Log.VERBOSE;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        // Log from here.
        Log.println(priority, TAG, message);
    }

    // ==== Required implementation ====

    // This is a reference to a characteristic that the manager will use internally.
    private BluetoothGattCharacteristic mCharacteristic;

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        // Here obtain instances of your characteristics.
        // Return false if a required service has not been discovered.
        BluetoothGattService mService = gatt.getService(SERVICE_UUID);
        if (mService != null) {
            mCharacteristic = mService.getCharacteristic(CHARACTERISTIC_UUID);
        }
        return mCharacteristic != null;
    }

    @Override
    protected void initialize() {
        // Initialize your device.
        // This means e.g. enabling notifications, setting notification callbacks, or writing
        // something to a Control Point characteristic.
        // Kotlin projects should not use suspend methods here, as this method does not suspend.
    }

    @Override
    protected void onServicesInvalidated() {
        // This method is called when the services get invalidated, i.e. when the device
        // disconnects.
        // References to characteristics should be nullified here.
        mCharacteristic = null;
    }

    // Here you may add some high level methods for your device:
    public void connectToDevice(BluetoothDevice device) {
        connect(device)
                .done(d -> {
                    log(Log.INFO, "Connected to BLE device");
                    mConnected = true;
                })
                .fail((d, status) -> log(Log.WARN, "Could not connect to device: " + status))
                .enqueue();
    }

    public boolean isConnectedAndSetup() {
        return mConnected;
    }

    public void mockReadNumberplate() {
        readCharacteristic(mCharacteristic).with((device, data) -> {
            LogHandler.Log("Read data successfully");
            LogHandler.Log("Data => " + data);

            availableData = data.toString();
            dataAvailable = true;
        }).enqueue();
    }

    public String getDataIfAvailable() {
        if (dataAvailable) {
            dataAvailable = false;
            return availableData;
        }

        return "";
    }

    public void writeData(byte data) {
        byte[] value = new byte[]{data};

        writeCharacteristic(mCharacteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .with((device, d) -> log(Log.INFO, "Wrote data successfully"))
                .fail((device, status) -> log(Log.WARN, "Failed to write data: " + status))
                .enqueue();
    }
}