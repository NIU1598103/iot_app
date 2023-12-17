
/*
Simple example of a Bluetooth communications activity

Provides a seek-bar (slider) to send values to the server, and a text widget to display the server's reply

Copyright 2018  Gunnar Bowman, Emily Boyes, Trip Calihan, Simon D. Levy, Shepherd Sims

MIT License
*/

package com.example.iotapp;


import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import static com.example.iotapp.LogHandler.Log;

import com.example.iotapp.bt.BtHandler;
import com.example.iotapp.data.CloudLogs;

public class MyCommunicationsActivity extends CommunicationsActivity {

    private String mMessageFromServer = "";

    private TextView mPlateText;

    private SeekBar mCameraBar;
    private SeekBar mFotoBar;
    private SeekBar mTempBar;
    private Spinner spinnerHoraris;

    private Handler mainHandler;
    private Runnable mainRunnable;

    private void readBtData() {
        if (!mBleManager.isConnectedAndSetup()) {
            return;
        }

        mBleManager.mockReadNumberplate();
        Log("READING BT DATA");

        String receivedData = mBleManager.getDataIfAvailable();

        if (receivedData.isEmpty()) {
            return;
        }

        mMessageFromServer += mBleManager.getDataIfAvailable();

        if (mMessageFromServer.substring(mMessageFromServer.length() - 1).equals(".")) {
            Log("Received full numberplate: " + mMessageFromServer);
            String numberPlate = mMessageFromServer.substring(0, mMessageFromServer.length() - 1);
            mPlateText.setText(numberPlate);

            CloudLogs.getInstance().mockLogEdgeToCloud(numberPlate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mCameraBar = (SeekBar)findViewById(R.id.seekBar);
        mFotoBar = (SeekBar)findViewById(R.id.seekBar4);
        mTempBar = (SeekBar)findViewById(R.id.seekBar5);
        mPlateText = (TextView)findViewById(R.id.numberPlate);
        spinnerHoraris = findViewById(R.id.spinner_horaris);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.zones_horaries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinnerHoraris.setAdapter(adapter);

        mainHandler = new Handler(Looper.getMainLooper());

        mainRunnable = new Runnable() {
            @Override
            public void run() {
                readBtData();
                mainHandler.postDelayed(this, 1000);
            }
        };

        mainHandler.post(mainRunnable);

        mCameraBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(progress);
                if (fromUser==true) {
                    sendSliderValue((byte) 0x5, String.valueOf(progress).getBytes()[0]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFotoBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(progress);
                if (fromUser == true) {
                    sendSliderValue((byte) 0x6, String.valueOf(progress).getBytes()[0]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(progress);
                if (fromUser == true) {
                    sendSliderValue((byte) 0x7, String.valueOf(progress).getBytes()[0]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void sendSliderValue(byte command, byte value) {
        if (!mBleManager.isConnectedAndSetup()) {
            return;
        }

        mBleManager.writeData(command);
        mBleManager.writeData(value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacks(mainRunnable);
    }
}
