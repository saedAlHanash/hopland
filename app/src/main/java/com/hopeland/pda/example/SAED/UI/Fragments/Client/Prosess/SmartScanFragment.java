package com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Helpers.NoteMessage;
import com.hopeland.pda.example.uhf.ClientActivity;


import org.jetbrains.annotations.NotNull;


@SuppressLint("NonConstantResourceId")
public class SmartScanFragment extends Fragment implements View.OnClickListener, ClientActivity.OnReadTag {
    View view;

    ImageView imageView8;
   public EditText epc;

    Button read;
    Button stop;
    TextView rssiTv;

    ClientActivity myActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (ClientActivity) requireActivity();

        view = inflater.inflate(R.layout.fragment_smart_scan, container, false);
        initViews();

        read.setOnClickListener(this);
        stop.setOnClickListener(this);

        return view;
    }

    private void initViews() {
        imageView8 = view.findViewById(R.id.imageView8);
        epc = view.findViewById(R.id.epc);
        read = view.findViewById(R.id.read);
        stop = view.findViewById(R.id.stop);
        rssiTv = view.findViewById(R.id.rssi);
    }


    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 0 && msg.what <= 12) {
                imageView8.setImageLevel(msg.what);
                rssiTv.setText("-" + rssi + "dB");

                //log.d("SAED_S", "smart scan rssi =: " + rssi);
            }
        }
    };

    byte rssi;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.read: {
                if (epc.getText().length() < 20) {
                    NoteMessage.showSnackBar(myActivity, "يجب ادخال ال Tag كاملا");
                    return;
                }

                myActivity.readSmart(epc.getText().toString());
                break;
            }

            case R.id.stop: {

                myActivity.stop();
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        myActivity.onReadTag = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        myActivity.onReadTag = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        myActivity.stop();
    }

    @Override
    public void onRead(@NotNull String epc, byte rssi) {
        if (!epc.equals(this.epc.getText().toString()))
            return;

        //log.d("SAED_S", "onRead: sss ");
        this.rssi = rssi;

        if (rssi < 60) {
            handler.sendEmptyMessage(0);
            return;
        }

        if (rssi > 60 && rssi < 65)
            handler.sendEmptyMessage(1);

        else if (rssi >= 65 && rssi < 70)
            handler.sendEmptyMessage(2);

        else if (rssi >= 70 && rssi < 75)
            handler.sendEmptyMessage(3);

        else if (rssi >= 75 && rssi < 80)
            handler.sendEmptyMessage(4);

        else if (rssi >= 80 && rssi < 85)
            handler.sendEmptyMessage(5);

        else if (rssi >= 85 && rssi < 90)
            handler.sendEmptyMessage(6);

        else if (rssi >= 90 && rssi < 95)
            handler.sendEmptyMessage(7);

        else if (rssi >= 95 && rssi < 100)
            handler.sendEmptyMessage(8);

        else if (rssi >= 100 && rssi < 105)
            handler.sendEmptyMessage(9);

        else if (rssi >= 105 && rssi < 110)
            handler.sendEmptyMessage(10);

        else if (rssi >= 110 && rssi < 115)
            handler.sendEmptyMessage(11);

        else if (rssi >= 115)
            handler.sendEmptyMessage(12);


    }
}