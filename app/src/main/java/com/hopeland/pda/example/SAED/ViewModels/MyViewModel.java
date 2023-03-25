package com.hopeland.pda.example.SAED.ViewModels;


import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.hopeland.pda.example.SAED.Network.SaedSocket;


import java.util.ArrayList;

public class MyViewModel extends ViewModel {

    Gson gson = new Gson();
    public MutableLiveData<ArrayList<All>> allLiveData = new MutableLiveData<>();
    public MutableLiveData<Pair<Product,Boolean>> productLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> connectLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> sendReportLiveData = new MutableLiveData<>();

    public void getAll(SaedSocket socket) {
        if (socket == null)
            return;
        if (allLiveData.getValue() != null) {
            allLiveData.postValue(allLiveData.getValue());
            return;
        }

        socket.send("1,");
    }

    public void getProduct(SaedSocket socket, String epc) {
        if (socket == null)
            return;
        socket.send("0," + epc);
    }

    private static final String TAG = "MyViewModel";

    public void sendReport(SaedSocket socket, Report report) {

        if (socket == null)
            return;

        sendReportLiveData = new MutableLiveData<>();

        String json = gson.toJson(report, Report.class);

        Log.d(TAG, "sendReport: " + json);

        socket.send("2," + json);
    }

}