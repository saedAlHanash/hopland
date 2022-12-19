package com.hopeland.pda.example.SAED.ViewModels;


import java.util.ArrayList;
import java.util.Date;

public class Report {

    public int wid;
    public int lid;
    public Date dt;
    public String usr;

    public ArrayList<String> epcs1 = new ArrayList<>();//all
    public ArrayList<String> epcs2 = new ArrayList<>();//scanned
    public ArrayList<String> epcs3 = new ArrayList<>();//undefined
    public ArrayList<String> epcs4 = new ArrayList<>();//unknown

    public void setWid(int wid) {
        this.wid = wid;
    }

    public void setLid(int lid) {
        this.lid = lid;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public void setEpcs1(ArrayList<String> epcs1) {
        this.epcs1.clear();
        this.epcs1.addAll(epcs1);
    }

    public void setEpcs2(ArrayList<String> epcs2) {
        this.epcs2.clear();
        this.epcs2.addAll(epcs2);
    }

    public void setEpcs3(ArrayList<String> epcs3) {
        this.epcs3.clear();
        this.epcs3.addAll(epcs3);
    }

    public void setEpcs4(ArrayList<String> epcs4) {
        this.epcs4.clear();
        this.epcs4.addAll(epcs4);
    }

    public Report() {
        this.wid = 0;
        this.lid = 0;
        this.dt = new Date();
        this.usr = "";
    }
}
