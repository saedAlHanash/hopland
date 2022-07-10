package com.hopeland.pda.example.SAED.Models;

import android.util.Pair;

import java.util.ArrayList;

/**
 * model to carrying data from EPC<br>
 * <p> creating by saed </p>
 */
public class EpcModel {

    /**
     * paring to set data <br>
     * first : for epc or tid data;<br>
     * second : for item count;
     */
    public Pair<String, Long> data;
    /**
     * to checking if data sent to server or not
     */
    public boolean isSent;

    public void setData(Pair<String, Long> data) {
        this.data = data;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}
