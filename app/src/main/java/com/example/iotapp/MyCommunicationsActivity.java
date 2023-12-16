
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

public class MyCommunicationsActivity extends CommunicationsActivity {

    private String mMessageFromServer = "";

    private TextView mPlateText;

    private SeekBar mCameraBar;
    private SeekBar mFotoBar;
    private SeekBar mTempBar;
    private Spinner spinnerHoraris;

    private void readBtData() {
        System.out.println("READING BT DATA");
        while (mBluetoothConnection.available() > 0) {
            char c = (char)mBluetoothConnection.read();

            if (c == '.') {
                if (mMessageFromServer.length() > 0) {
                    mPlateText.setText(mMessageFromServer);
                    mMessageFromServer = "";
                }
            }
            else {
                mMessageFromServer += c;
            }
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

        Handler mainHandler = new Handler(Looper.getMainLooper());

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                readBtData();
                mainHandler.postDelayed(this, 1000);
            }
        });


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
        mBluetoothConnection.write(command);
        mBluetoothConnection.write(value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
