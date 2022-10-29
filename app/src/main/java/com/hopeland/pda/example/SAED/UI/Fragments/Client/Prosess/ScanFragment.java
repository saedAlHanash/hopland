package com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess;

import android.annotation.SuppressLint;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Adadpter.AdapterItemEpc;
import com.hopeland.pda.example.SAED.AppConfig.FC;
import com.hopeland.pda.example.SAED.AppConfig.FN;
import com.hopeland.pda.example.SAED.Helpers.View.FTH;
import com.hopeland.pda.example.SAED.UI.Fragments.ProductInfoFragment;
import com.hopeland.pda.example.uhf.ClientActivity;
import com.hopeland.pda.example.SAED.ViewModels.MyViewModel;
import com.hopeland.pda.example.SAED.ViewModels.Product;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


@SuppressLint("NonConstantResourceId")
public class ScanFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, ClientActivity.OnReadTag, AdapterItemEpc.OnItemClicked {


    ClientActivity myActivity;
    Button btnStart;
    Button btnStop;
    Button btnClear;
    Spinner spinnerTypeScan;
    TextView typeScanTv;

    //region base

    RecyclerView recyclerView;

    AdapterItemEpc adapter;

    ArrayList<String> sentEpc = new ArrayList<>();

    MyViewModel myViewModel;
    boolean blockSend = false;

    String currentEpc;

    View view;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (ClientActivity) requireActivity();
        myViewModel = myActivity.myViewModel;

        myViewModel.productLiveData = new MutableLiveData<>();

        view = inflater.inflate(R.layout.fragment_scan, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        initAdapter();

        initView();

        return view;
    }


    final androidx.lifecycle.Observer<Pair<Product, Boolean>> Observer = pair -> {
        blockSend = false;
        if (!isAdded())
            return;



        if (pair == null )
            return;

        Log.d("SAED____", ": "+pair.second);

        if (!pair.second) {
            sentEpc.add(currentEpc);
            return;
        }

        if (sentEpc.contains(pair.first.epc))
            return;


        Log.e("SAED_", "product  " + pair.first.epc);

        sentEpc.add(pair.first.epc);
        adapter.insertItem(pair.first);
    };

    void initView() {

        btnStart = view.findViewById(R.id.read);
        btnStop = view.findViewById(R.id.stop);
        btnClear = view.findViewById(R.id.clean);
        spinnerTypeScan = view.findViewById(R.id.spinner_tybe_scan);
        typeScanTv = view.findViewById(R.id.scan_type_tv);


        btnStart.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        typeScanTv.setOnClickListener(this);

        spinnerTypeScan.setOnItemSelectedListener(this);
    }

    void initAdapter() {

        if (adapter == null)
            adapter = new AdapterItemEpc(myActivity, new ArrayList<>());

        adapter.setOnItemClicked(this);

        recyclerView.setAdapter(adapter);
    }

    void observeProduct() {
        myViewModel.productLiveData.observe(myActivity, Observer);
    }

    void removeObserveProduct() {
        myViewModel.productLiveData.removeObserver(Observer);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.read: {
                myActivity.read();
                break;
            }

            case R.id.stop: {
                myActivity.stop();
                break;
            }

            case R.id.clean: {
                myActivity.stop();

                if (myViewModel.productLiveData != null)
                    myViewModel.productLiveData.setValue(null);

                sentEpc.clear();

                adapter.setAndRefresh(new ArrayList<>());

                break;
            }

            case R.id.scan_type_tv: {
                spinnerTypeScan.performClick();
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        myActivity.onReadTag = null;
        removeObserveProduct();
    }

    @Override
    public void onResume() {
        super.onResume();
        myActivity.onReadTag = this;
        observeProduct();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        myActivity.stop();
    }

    @Override
    public void onStop() {
        super.onStop();

        myActivity.stop();
    }

    @Override
    public void onRead(@NotNull String epc, byte rssi) {

        if (sentEpc.contains(epc))
            return;

        blockSend = true;

        this.currentEpc = epc;
        myViewModel.getProduct(myActivity.socket, epc);

    }

    @Override
    public void onItemClicked(int position, ArrayList<Product> list) {
        FTH.addFragmentUpFragment(FC.CLIENT_C,requireActivity(),
                new ProductInfoFragment(list.get(position)), FN.PRODUCT_INFO_FN);
    }

    int typeScan;

    //region spinner

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        typeScan = position;

        if (position == 0)
            typeScanTv.setText(getResources().getString(R.string.single));
        else
            typeScanTv.setText(getResources().getString(R.string.inventory_epc));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    //endregion

}