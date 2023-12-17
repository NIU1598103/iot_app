/*
Bluetooth communications activity

Works with BluetoothConnection to provide simple interaction with a sever over a Bluetooth socket:
seek bar (slider) sends serialized values to server; activity checks for available responses from
server.

Copyright 2018  Gunnar Bowman, Emily Boyes, Trip Calihan, Simon D. Levy, Shepherd Sims

MIT License
*/

package com.example.iotapp;

import android.content.Intent;
import android.os.Bundle;
import android.bluetooth.BluetoothDevice;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iotapp.bt.BtHandler;
import com.example.iotapp.bt.MyBleManager;

public abstract class CommunicationsActivity extends AppCompatActivity {


    private String mDeviceAddress;
    private BtHandler mBtHandler;
    protected MyBleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_communications);

        // Retrieve the address of the bluetooth device from the BluetoothListDeviceActivity
        Intent newint = getIntent();
        mDeviceAddress = newint.getStringExtra(DeviceListActivity.EXTRA_ADDRESS);

        // Create a connection to this device
        mBtHandler = BtHandler.getInstance();
        BluetoothDevice device = mBtHandler.getBtDeviceByAddress(mDeviceAddress);

        mBleManager = new MyBleManager(this);
        mBleManager.connectToDevice(device);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}