package com.wadaro.collector.ui.overcredit;

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


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.AdapterView> {
    private static final String TAG = "SurveyAdapter";

    private ArrayList<DataHolder> mList;
    private Context mContext;

    public DataAdapter(Context context, ArrayList<DataHolder> data){
        mList = data;
        mContext = context;
    }
    @NonNull
    @Override
    public AdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_assignment_adapter, parent, false);
        return new AdapterView(itemView);
    }

    public DataHolder getData(int position){
        return mList.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterView holder, int position) {
        final DataHolder data = mList.get(position);
        holder.txvw_nottb_00.setText(data.salesRcvId);
        holder.txvw_coordinator_00.setText(data.coordinatorName);
        holder.txvw_installment_00.setText(data.tenor+"");
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
        private TextView txvw_nottb_00, txvw_coordinator_00, txvw_installment_00,txvw_total_00;
        private LinearLayout lnly_root_00;

        AdapterView(View itemView) {
            super(itemView);
            txvw_nottb_00       = itemView.findViewById(R.id.txvw_nottb_00);
            txvw_coordinator_00  = itemView.findViewById(R.id.txvw_coordinator_00);
            txvw_installment_00 = itemView.findViewById(R.id.txvw_installment_00);
            txvw_total_00 = itemView.findViewById(R.id.txvw_total_00);
            lnly_root_00   = itemView.findViewById(R.id.lnly_root_00);

        }
    }

    private OnSelectedListener onSelectedListener;
    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }
    public interface OnSelectedListener{
        void onSelected(DataHolder data);
    }
}
