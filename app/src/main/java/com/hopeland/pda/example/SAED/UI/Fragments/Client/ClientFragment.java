package com.hopeland.pda.example.SAED.UI.Fragments.Client;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.AppConfig.FC;
import com.hopeland.pda.example.SAED.AppConfig.FN;
import com.hopeland.pda.example.SAED.Helpers.View.FTH;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess.InventoryFragment;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess.SmartScanFragment;
import com.hopeland.pda.example.uhf.ClientActivity;
import com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess.ScanFragment;


@SuppressLint("NonConstantResourceId")
public class ClientFragment extends Fragment implements View.OnClickListener {


    CardView smartScan;
    CardView inventory;
    CardView scan;
    CardView settings;

    View view;
    ClientActivity myActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myActivity = (ClientActivity) requireActivity();
        view = inflater.inflate(R.layout.fragment_client, container, false);
        initView();

        listeners();

        return view;
    }

    private void initView() {
        smartScan = view.findViewById(R.id.smart_scan);
        inventory = view.findViewById(R.id.inventory);
        scan = view.findViewById(R.id.scan);
        settings = view.findViewById(R.id.settings);
    }

    void listeners() {
        // المسح الذكي
        smartScan.setOnClickListener(this);
        // جرد المخزن
        inventory.setOnClickListener(this);
        // مسح منتج
        scan.setOnClickListener(this);
        //اعدادات
        settings.setOnClickListener(this);
    }

    void startSetting() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(),
                new SettingsFragment(), FN.SETTING_FN);
    }

    void startInventory() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(),
                new InventoryFragment(), FN.SETTING_FN);
    }

    void startScanFragment() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, myActivity,
                new ScanFragment(), FN.SCAN_FN);
    }

    void startSmartScanFragment() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(),
                new SmartScanFragment(), FN.SMART_SCAN_FN);
    }

    private static final String TAG = "ClientFragment";

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.smart_scan) {
            // المسح الذكي
            startSmartScanFragment();
        } else if (v.getId() == R.id.inventory) {
            // جرد المخزن
            startInventory();
        } else if (v.getId() == R.id.scan) {
            // مسح منتج
            startScanFragment();
        } else if (v.getId() == R.id.settings) {
            // إعدادات
            startSetting();
        }

    }


}