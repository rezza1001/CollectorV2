package com.wadaro.collector.ui.recap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.util.MyCurrency;

import java.util.ArrayList;


public class RecapAdapter extends RecyclerView.Adapter<RecapAdapter.AdapterView> {
    private static final String TAG = "RecapAdapter";

    private ArrayList<RecapHolder> mList;
    private Context mContext;

    public RecapAdapter(Context context, ArrayList<RecapHolder> data){
        mList = data;
        mContext = context;
    }
    @NonNull
    @Override
    public AdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recap_adapter_main, parent, false);
        return new AdapterView(itemView);
    }

    public RecapHolder getData(int position){
        return mList.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterView holder, int position) {
        final RecapHolder data = mList.get(position);
        holder.txvw_note_00.setText(data.note);
        holder.txvw_qty_00.setText(data.qty+"");
        holder.txvw_total_00.setText(MyCurrency.toCurrnecy(data.total));

        holder.lnly_root_00.setOnClickListener(v -> {
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
        private final TextView txvw_note_00, txvw_qty_00, txvw_total_00;
        private final LinearLayout lnly_root_00;

        AdapterView(View itemView) {
            super(itemView);
            txvw_note_00 = itemView.findViewById(R.id.txvw_note_00);
            txvw_total_00 = itemView.findViewById(R.id.txvw_total_00);
            lnly_root_00   = itemView.findViewById(R.id.lnly_root_00);
            txvw_qty_00 = itemView.findViewById(R.id.txvw_qty_00);

        }
    }

    private OnSelectedListener onSelectedListener;
    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }
    public interface OnSelectedListener{
        void onSelected(RecapHolder data);
    }
}
