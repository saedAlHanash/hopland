package com.hopeland.pda.example.SAED.UI.Fragments.Client;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hopeland.pda.example.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryFragment extends Fragment {

    @BindView(R.id.inventory_spinner)
    Spinner inventorySpinner;
    @BindView(R.id.place_spinner)
    Spinner placeSpinner;
    @BindView(R.id.expected)
    TextView expected;
    @BindView(R.id.scaned)
    TextView scanned;
    @BindView(R.id.not_found)
    TextView notFound;
    @BindView(R.id.undefined)
    TextView undefined;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inventory, container, false);
        ButterKnife.bind(this, view);

        listeners();
        return view;
    }

    void listeners() {

        inventorySpinner.setOnItemSelectedListener(inventorySpinnerSelectListener);

    }

    void initTvs(int y) {
        expected.setText(String.valueOf(y * 2));
        scanned.setText(String.valueOf(y * 3));
        notFound.setText(String.valueOf(y * 5));
        undefined.setText(String.valueOf(y * 7));
    }

    private final AdapterView.OnItemSelectedListener inventorySpinnerSelectListener
            = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            switch (i) {
                case 0: {
                    initTvs(1);
                    break;
                }
                case 1: {
                    initTvs(4);
                    break;
                }
                case 2: {
                    initTvs(12);
                    break;
                }
                case 3: {
                    initTvs(9);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


}