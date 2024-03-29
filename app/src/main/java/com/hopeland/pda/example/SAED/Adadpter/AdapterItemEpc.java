package com.hopeland.pda.example.SAED.Adadpter;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Helpers.system.Copy;
import com.hopeland.pda.example.SAED.ViewModels.Product;

import java.util.ArrayList;


/*
created on 19/08/2022 - 11:29 ص
to project PdaAdapter_hks
*/
public class AdapterItemEpc extends RecyclerView.Adapter<AdapterItemEpc.Holder> {

    private final Activity activity;
    private ArrayList<Product> list;

    private OnItemClicked listener;


    public AdapterItemEpc(Activity activity, ArrayList<Product> list) {
        this.activity = activity;
        this.list = list;
    }

    /**
     * set new Item List to the adapter and refreshing all item
     *
     * @param list list of new Item {@link #list}
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setAndRefresh(ArrayList<Product> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        Product item = getItem(position);

        holder.epc.setText(item.epc);
        holder.name.setText(item.pn);
        holder.price.setText(item.wn);

        if (item.bitmap != null)
            holder.imageView9.setImageBitmap(item.getImageThump());
        else
            holder.imageView9.setImageResource(R.drawable.gray_logo);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    @SuppressLint("NonConstantResourceId")
    public class Holder extends RecyclerView.ViewHolder {
        ImageView imageView9;
        TextView name;
        TextView epc;
        TextView price;

        public Holder(@NonNull View itemView) {
            super(itemView);

            imageView9 = itemView.findViewById(R.id.imageView9);
            name = itemView.findViewById(R.id.name);
            epc = itemView.findViewById(R.id.epc);
            price = itemView.findViewById(R.id.price);
            itemView.setOnClickListener(view -> {
                if (listener == null)
                    return;
                listener.onItemClicked(getAdapterPosition(), list);
            });
            itemView.setOnLongClickListener(v -> {
                Copy.copyText(activity, getItem(getAdapterPosition()).epc);
                return true;
            });
        }
    }

    /**
     * set and init listener to take an action when an item is pressed
     *
     * @param listener Object form interface @{@link OnItemClicked}
     */
    public void setOnItemClicked(OnItemClicked listener) {
        this.listener = listener;
    }

    public interface OnItemClicked {
        /**
         * to call it when item clicked
         *
         * @param position index item in adapter and list
         * @param list     items list of Product
         */
        void onItemClicked(int position, ArrayList<Product> list);
    }

    /**
     * get an item form {@link #list}
     *
     * @param i position adapter == (index in list)
     * @return object Product from list {@link #list}
     */
    private Product getItem(int i) {
        // return item in index i
        return this.list.get(i);
    }

    /**
     * delete an item form {@link #list}
     *
     * @param i position adapter == (index in list)
     */
    public void deleteItem(int i) {
        // delete item with index i in the list
        this.list.remove(i);
        this.notifyItemRemoved(i);
    }

    /**
     * editing an item form {@link #list}
     *
     * @param i    @param i position adapter == (index in list)
     * @param item new Item data
     */
    public void editItem(int i, Product item) {
        // replace item in index i with new item
        this.list.set(i, item);
        this.notifyItemChanged(i);
    }

    /**
     * adding an item to begin {@link #list}
     *
     * @param item new Item to add it to the list
     */
    public void insertItem(Product item) {

        // add item in begin of list
        this.list.add(list.size(), item);
        notifyItemChanged(list.size() - 1);
    }


}
