package com.hopeland.pda.example.SAED.UI.Fragments.Client.Prosess;

import android.annotation.SuppressLint;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
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
import java.util.PriorityQueue;
import java.util.Queue;


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

    String currentEpc;

    ArrayList<String> sentEpc = new ArrayList<>();
    ArrayList<String> scannedEpc = new ArrayList<>();

    MyViewModel myViewModel;


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
        if (!isAdded())
            return;

        if (pair == null)
            return;

        if (!pair.second) {
            Product product = new Product();
            product.epc = currentEpc;
            product.wn = getString(R.string.unknown_product);

            adapter.insertItem(product);
        } else
            adapter.insertItem(pair.first);

        new Handler(Looper.getMainLooper()).postDelayed(this::send, 400);
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
        if (v.getId() == R.id.read) {
            myActivity.read();
        } else if (v.getId() == R.id.stop) {
            myActivity.stop();
        } else if (v.getId() == R.id.clean) {
            myActivity.stop();

            if (myViewModel.productLiveData != null)
                myViewModel.productLiveData.setValue(null);

            scannedEpc.clear();

            adapter.setAndRefresh(new ArrayList<>());
        } else if (v.getId() == R.id.scan_type_tv) {
            spinnerTypeScan.performClick();
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

    boolean blockSend = false;

    @Override
    public void onRead(@NotNull String epc, byte rssi) {

        if (scannedEpc.contains(epc))
            return;

        scannedEpc.add(epc);

        queue.offer(epc);

        if (blockSend)
            return;

        send();
    }

    Queue<String> queue = new PriorityQueue<>();

    public void send() {

        blockSend = true;

        if (queue.isEmpty()) {
            blockSend = false;
            return;
        }

        currentEpc = queue.poll();
        myViewModel.getProduct(myActivity.socket, currentEpc);
    }

    @Override
    public void onItemClicked(int position, ArrayList<Product> list) {
        Log.d(TAG, "onItemClicked: product clicked ");
        if (list.get(position).wn.equals(getString(R.string.unknown_product)))
            return;

        FTH.addFragmentUpFragment(FC.CLIENT_C, requireActivity(),
                new ProductInfoFragment(list.get(position)), FN.PRODUCT_INFO_FN);
    }

    private static final String TAG = "ScanFragment";

    //region spinner

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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