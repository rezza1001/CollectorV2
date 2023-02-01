package com.wadaro.collector.ui.assignment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wadaro.collector.R;

import java.util.ArrayList;


public class DenahAdapter extends RecyclerView.Adapter<DenahAdapter.AdapterView> {

    private ArrayList<String> mList;
    private Context mContext;

    public DenahAdapter(Context context, ArrayList<String> data){
        mList = data;
        mContext = context;
    }
    @NonNull
    @Override
    public AdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.assignment_adapter_denah, parent, false);
        return new AdapterView(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterView holder, int position) {
        final String data = mList.get(position);
        Log.d("TAGRZ",data);
        Glide.with(mContext).load(data).into(holder.imvw_denah_00);
        holder.imvw_denah_00.setOnClickListener(v -> {
            if (onSelectedListener != null){
                onSelectedListener.onSelected(data);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class AdapterView extends RecyclerView.ViewHolder{
        private RoundedImageView imvw_denah_00;

        @SuppressLint("SetTextI18n")
        AdapterView(View itemView) {
            super(itemView);
            imvw_denah_00     = itemView.findViewById(R.id.imvw_denah_00);


        }
    }

    private OnSelectedListener onSelectedListener;
    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }
    public interface OnSelectedListener{
        void onSelected(String data);
    }
}
