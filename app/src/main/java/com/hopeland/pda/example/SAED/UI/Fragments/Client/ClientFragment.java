package com.hopeland.pda.example.SAED.UI.Fragments.Client;


import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hopeland.pda.example.AppConfig.FC;
import com.hopeland.pda.example.AppConfig.FN;
import com.hopeland.pda.example.Helpers.View.FTH;
import com.hopeland.pda.example.R;
import com.hopeland.pda.example.uhf.UHFMain;
import com.port.Adapt;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClientFragment extends Fragment {


    @BindView(R.id.smart_scan)
    CardView smartScan;
    @BindView(R.id.inventory)
    CardView inventory;
    @BindView(R.id.scan)
    CardView scan;
    @BindView(R.id.settings)
    CardView settings;

    boolean isClient = false;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_client, container, false);
        ButterKnife.bind(this, view);

        listeners();


        return view;
    }

    void listeners() {
        // المسح الذكي
        smartScan.setOnClickListener(smartScanListener);

        // جرد المخزن
        inventory.setOnClickListener(inventoryListener);

        // مسح منتج
        scan.setOnClickListener(scanListener);

        //اعدادات
        settings.setOnClickListener(settingsListener);
    }

    // المسح الذكي
    private final View.OnClickListener smartScanListener = v -> {

    };
    // جرد المخزن
    private final View.OnClickListener inventoryListener = v -> {
        startInventory();
    };
    // مسح منتج
    private final View.OnClickListener scanListener = v -> {
        startUHFMainActivity();
    };
    //اعدادات
    private final View.OnClickListener settingsListener = v -> {
        startSetting();
    };


    void startUHFMainActivity() {

        Adapt.init(requireContext());
        Adapt.enablePauseInBackGround(requireContext());

        requireActivity().startActivity(new Intent(requireContext(), UHFMain.class));
    }

    void startSetting() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(), new SettingsFragment(), FN.SETTING_FN);
    }

    void startInventory() {
        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(), new InventoryFragment(), FN.SETTING_FN);
    }
}