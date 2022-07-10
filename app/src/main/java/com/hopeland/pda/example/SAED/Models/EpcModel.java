package com.hopeland.pda.example.SAED.Models;

import android.util.Pair;

import java.util.ArrayList;

public class EpcModel {
    public Pair<String, Long> data;
    public boolean isSent;

    public void setData(Pair<String, Long> data) {
        this.data = data;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}
