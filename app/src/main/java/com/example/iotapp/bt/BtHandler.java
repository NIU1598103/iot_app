package com.example.iotapp.bt;

import static com.example.iotapp.LogHandler.Log;

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

    public void requestPermissionsAndScan() {
        if (!hasPermissions()) {
            String[] permissionArr = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
            
            Dexter.withContext(this).withPermissions(permissionArr).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        Log("All permissions granted");

                        startScanningDevices();
                    }

                    if (report.isAnyPermissionPermanentlyDenied()) {
                        List<PermissionDeniedResponse> a = report.getDeniedPermissionResponses();
                        for (PermissionDeniedResponse item : a) {
                            Log("Missing permission: " + item.getPermissionName())
                        }

                        startScanningDevices();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
        }
    }

    private void startScanningDevices() {
        mScanCB = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                String deviceName = device.getName();
                
                if (deviceName == null) {
                    return;
                }

                LogI("New device discovered: " + deviceName + " - address: " + device.getAddress());
                
                if (!deviceExists(device)) {
                    btDevices.add(device);
                }
            }
        };

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCB);
    }

    public ArrayList<BluetoothDevice> getBtDevices() {
        return btDevices;
    }

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

    public void connectToDevice(BluetoothDevice device) {
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
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
                        return;
                    } else {
                        Log("Amount of services found: " + gattServices.size());
                        btCharacteristic = new BluetoothGattCharacteristic("19b10001e8f2537e4f6cd104768a1215");
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

        bluetoothGatt = device.connectGatt(this, false, gattCallback);
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

    public void mockReadNumberplate() {
        bluetoothGatt.readCharacteristic(btCharacteristic);
    }

    public void writeData(byte data) {
        byte[] value = new byte[]{data};
        bluetoothGatt.writeCharacteristic(btCharacteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private boolean hasPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
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
