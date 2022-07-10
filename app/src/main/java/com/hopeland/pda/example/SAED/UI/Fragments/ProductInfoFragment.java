package com.hopeland.pda.example.SAED.UI.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hopeland.pda.example.R;

import butterknife.ButterKnife;


public class ProductInfoFragment extends Fragment {
    View view;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_product_info, container, false);
            ButterKnife.bind(this, view);


            return view;
        }
}