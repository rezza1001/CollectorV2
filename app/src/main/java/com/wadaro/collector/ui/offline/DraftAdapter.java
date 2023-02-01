package com.wadaro.collector.ui.offline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;

import java.util.ArrayList;


public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.AdapterView> {
    private static final String TAG = "FIndAdapter";

    private ArrayList<Bundle> mList;
    private Context mContext;

    public DraftAdapter(Context context, ArrayList<Bundle> data){
        mList = data;
        mContext = context;
    }
    @NonNull
    @Override
    public AdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.draft_adapter_main, parent, false);
        return new AdapterView(itemView);
    }

    public Bundle getData(int position){
        return mList.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterView holder, int position) {
        final Bundle data = mList.get(position);
        holder.txvw_so_00.setText(data.getString("billing_id"));
        holder.txvw_demo_00.setText("Konsumen : "+ data.getString("consumen"));
        holder.txvw_note_00.setText("Produk : "+ data.getString("product") +" ("+data.getString("payment_status")+")");

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class AdapterView extends RecyclerView.ViewHolder{
        private TextView txvw_so_00, txvw_note_00, txvw_demo_00;

        AdapterView(View itemView) {
            super(itemView);
            txvw_so_00      = itemView.findViewById(R.id.txvw_so_00);
            txvw_note_00    = itemView.findViewById(R.id.txvw_note_00);
            txvw_demo_00    = itemView.findViewById(R.id.txvw_demo_00);

        }
    }

    private OnSelectedListener onSelectedListener;
    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }
    public interface OnSelectedListener{
        void onSelected(Bundle data);
    }
}
