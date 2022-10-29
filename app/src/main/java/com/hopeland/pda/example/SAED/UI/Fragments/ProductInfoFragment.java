package com.hopeland.pda.example.SAED.UI.Fragments;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.ViewModels.Product;


public class ProductInfoFragment extends Fragment {
    Product product;

    ImageView product_image;
    TextView name;
    TextView stons;
    TextView material_type;
    TextView purity;
    TextView total_weight;
    TextView material_weight;
    TextView purity_weight;
    TextView price;

    View view;

    public ProductInfoFragment(Product product) {
        this.product = product;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_product_info, container, false);
        initItem();

        return view;
    }

    void initItem() {
        product_image = view.findViewById(R.id.product_image);
        name = view.findViewById(R.id.name);
        stons = view.findViewById(R.id.stons);
        material_type = view.findViewById(R.id.material_type);
        purity = view.findViewById(R.id.purity);
        total_weight = view.findViewById(R.id.total_weight);
        material_weight = view.findViewById(R.id.material_weight);
        purity_weight = view.findViewById(R.id.purity_weight);
        price = view.findViewById(R.id.price);

        product_image.setImageBitmap(product.bitmap);

        name.setText(product.pn);
//        stons.setText(product.pn);
        material_type.setText(product.wn);
        purity.setText(product.pp);
        total_weight.setText(product.ln);
        material_weight.setText(product.im);
//        purity_weight.setText(product.);
//        price.setText(product.);
    }
}