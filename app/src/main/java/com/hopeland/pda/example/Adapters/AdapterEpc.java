package com.hopeland.pda.example.Adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hopeland.pda.example.R;
import com.hopeland.pda.example.SAED.Models.EpcModel;

import java.util.ArrayList;

import butterknife.ButterKnife;

/*
created on 01/07/2022 - 01:54 م
to project PDAExample
*/
public class AdapterEpc extends RecyclerView.Adapter<AdapterEpc.Holder> {

    Activity activity;
    ArrayList<EpcModel> list;

    OnItemClicked listener;

    public AdapterEpc(Activity activity, ArrayList<EpcModel> list) {
        this.activity = activity;
        this.list = list;
    }

    /**
     * set new Item List to the adapter and refreshing all item
     *
     * @param list list of new Item {@link #list}
     */
    public void setAndRefresh(ArrayList<EpcModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_epc, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.epc.setText(list.get(position).data.first);
        holder.count.setText(String.valueOf((list.get(position).data.second)));

        if (list.get(position).isSent)
            holder.imageView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView epc;
        TextView count;
        ImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            epc = itemView.findViewById(R.id.epc);
            count = itemView.findViewById(R.id.count);
            imageView = itemView.findViewById(R.id.imageView6);
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
         * @param list     items list of Pair<String,String>
         */
        void onClicked(int position, ArrayList<EpcModel> list);
    }

    /**
     * get an item form {@link #list}
     *
     * @param i position adapter == (index in list)
     * @return object Pair<String,String> from list {@link #list}
     */
    private EpcModel getItem(int i) {
        // return item in index i
        return this.list.get(i);
    }

    /**
     * delete an item form {@link #list}
     *
     * @param i position adapter == (index in list)
     */
    private void deleteItem(int i) {
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
    private void editItem(int i, EpcModel item) {
        // replace item in index i with new item
        this.list.set(i, item);
        this.notifyItemChanged(i);
    }

    /**
     * adding an item to begin {@link #list}
     *
     * @param item new Item to add it to the list
     */
    private void insertItem(EpcModel item) {
        // add item in begin of list
        this.list.add(0, item);
        this.notifyItemInserted(0);
    }


}
