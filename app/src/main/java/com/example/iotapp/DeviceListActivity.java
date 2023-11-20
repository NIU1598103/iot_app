package com.example.iotapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 2; // Use a different request code for Bluetooth enable

    ListView mDeviceList;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> mPairedDevices;

    public static String EXTRA_ADDRESS = "device_address";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mDeviceList = findViewById(R.id.listView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                listPairedDevices();
            } else {
                // Ask the user to turn Bluetooth on
                requestBluetoothPermissionAndEnable();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void listPairedDevices() {
        // Check for BLUETOOTH permission
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed to list paired devices
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            ArrayList<String> list = new ArrayList<>();

            if (mPairedDevices.size() > 0) {
                for (BluetoothDevice bt : mPairedDevices) {
                    list.add(bt.getName() + "\n" + bt.getAddress());
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            mDeviceList.setAdapter(adapter);
            mDeviceList.setOnItemClickListener(myListClickListener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestBluetoothPermissionAndEnable() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with Bluetooth enabling
            enableBluetooth();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableBluetooth() {
        // Check if the app has the BLUETOOTH_ADMIN permission
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed with Bluetooth enabling
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, BLUETOOTH_ENABLE_REQUEST_CODE);
        } else {
            // Permission is not granted, request it
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Make an intent to start the next activity.
            Intent i = new Intent(DeviceListActivity.this, MyCommunicationsActivity.class);
            // Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); // This will be received at CommunicationsActivity
            startActivity(i);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with Bluetooth enabling
                enableBluetooth();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled, proceed with listing paired devices
                listPairedDevices();
            } else {
                // Bluetooth enabling was canceled or failed, handle accordingly
                Toast.makeText(this, "Bluetooth enabling canceled or failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
