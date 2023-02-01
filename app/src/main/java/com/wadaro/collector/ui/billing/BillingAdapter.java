package com.wadaro.collector.ui.billing;

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


public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.AdapterView> {
    private static final String TAG = "RecapAdapter";

    private ArrayList<BillingHolder> mList;
    private Context mContext;

    public BillingAdapter(Context context, ArrayList<BillingHolder> data){
        mList = data;
        mContext = context;
    }
    @NonNull
    @Override
    public AdapterView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.billing_adapter_main, parent, false);
        return new AdapterView(itemView);
    }

    public BillingHolder getData(int position){
        return mList.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterView holder, int position) {
        final BillingHolder data = mList.get(position);
        holder.txvw_no_00.setText(data.no);
        holder.txvw_consumen_00.setText(data.consumer);
        holder.txvw_installment_00.setText(data.installment);
        holder.txvw_pay_00.setText(MyCurrency.toCurrnecy(data.pay));
        holder.txvw_status_00.setText(data.status);

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
        private final TextView txvw_no_00, txvw_consumen_00, txvw_installment_00, txvw_status_00,txvw_pay_00;
        private final LinearLayout lnly_root_00;

        AdapterView(View itemView) {
            super(itemView);
            txvw_no_00 = itemView.findViewById(R.id.txvw_no_00);
            txvw_installment_00 = itemView.findViewById(R.id.txvw_installment_00);
            txvw_status_00   = itemView.findViewById(R.id.txvw_status_00);
            lnly_root_00   = itemView.findViewById(R.id.lnly_root_00);
            txvw_pay_00   = itemView.findViewById(R.id.txvw_pay_00);
            txvw_consumen_00   = itemView.findViewById(R.id.txvw_consumen_00);

        }
    }

    private OnSelectedListener onSelectedListener;
    public void setOnSelectedListener(OnSelectedListener onSelectedListener){
        this.onSelectedListener = onSelectedListener;
    }
    public interface OnSelectedListener{
        void onSelected(BillingHolder data);
    }
}
