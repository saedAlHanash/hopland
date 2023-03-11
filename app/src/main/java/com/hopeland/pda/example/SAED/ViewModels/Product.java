package com.hopeland.pda.example.SAED.ViewModels;

import android.graphics.Bitmap;

import com.hopeland.pda.example.SAED.Helpers.Images.ConverterImage;

public class Product {

    public String epc;
    //name
    public String pn;
    //price
    public String pp;
    public String wn;
    public String ln;
    public String im;
    //pure
    public String pur;
    //type
    public String mt_type;
    public String grw;
    public String mtw;
    public String ntw;
    public String sto;
    public Bitmap bitmap;

    public Bitmap getImageThump() {
        if (bitmap == null)
            return null;
        return ConverterImage.getResizedBitmap(bitmap, 500);
    }
}
