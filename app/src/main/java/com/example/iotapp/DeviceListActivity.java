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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 2; // Use a different request code for Bluetooth enable

    ListView mDeviceList;
    Button mButton;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> mPairedDevices;

    public static String EXTRA_ADDRESS = "device_address";

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            System.out.println("Permission granted");
        } else {
            System.out.println("Permission denied");
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mDeviceList = findViewById(R.id.listView);
        mButton = findViewById(R.id.button_first2);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DeviceListActivity.this, MyCommunicationsActivity.class);
                startActivity(i);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                System.out.println("List pair devices");

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
                }
                listPairedDevices();
            } else {
                // Ask the user to turn Bluetooth on
                System.out.println("wtf");
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void listPairedDevices() {
        // Check for BLUETOOTH permission
        //requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);

        /*if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {*/

        // Permission already granted, proceed to list paired devices

        // TODO: CODI PER CONNECTAR NOUS DISPOSITIUS, bluetooth scan

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
}
