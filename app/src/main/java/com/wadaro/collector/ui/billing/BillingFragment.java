package com.wadaro.collector.ui.billing;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyFragment;
import com.wadaro.collector.component.SelectHolder;
import com.wadaro.collector.component.SelectView;
import com.wadaro.collector.util.MyCurrency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BillingFragment extends MyFragment {

    private TextView txvw_total_00;
    private SelectView slvw_date_00;
    private BillingAdapter mAdapter;
    private final ArrayList<BillingHolder> holders = new ArrayList<>();

    DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", new Locale("id"));

    public static BillingFragment newInstance() {
        
        Bundle args = new Bundle();
        
        BillingFragment fragment = new BillingFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    protected int setlayout() {
        return R.layout.billing_fragment_main;
    }

    @Override
    protected void initLayout(View view) {
        slvw_date_00 = view.findViewById(R.id.slvw_date_00);
        slvw_date_00.setHint("Tanggal");
        txvw_total_00 = view.findViewById(R.id.txvw_total_00);

        RecyclerView rcvw_data_00 = view.findViewById(R.id.rcvw_data_00);
        rcvw_data_00.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new BillingAdapter(mActivity, holders);
        rcvw_data_00.setAdapter(mAdapter);
        txvw_total_00.setText("0");
    }

    @Override
    protected void initListener() {
        slvw_date_00.setSelectedListener(data -> openDate());
    }

    private void openDate(){

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, (view, year1, monthOfYear, dayOfMonth) -> {
            try {
                Date date = format2.parse(year1 +"-"+ (monthOfYear + 1)+"-"+ dayOfMonth);
                assert date != null;
                slvw_date_00.setValue(new SelectHolder(format2.format(date),format2.format(date)));
                request();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void initData() {
        slvw_date_00.setValue(new SelectHolder(format2.format(new Date()),format2.format(new Date())));
        request();
    }

    private void request(){
        holders.clear();
        PostManager post = new PostManager(mActivity,"report/billing?billing_date="+slvw_date_00.getKey());
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    long total = 0;
                    JSONArray data = obj.getJSONArray("data");
                    for (int i=0; i<data.length(); i++){
                        JSONObject jo = data.getJSONObject(i);
                        BillingHolder holder = new BillingHolder();
                        holder.consumer = jo.getString("consumen_name");
                        holder.installment = jo.getString("installment_period");
                        holder.pay = jo.getString("payment_total").split("\\.")[0];
                        holder.status = jo.getString("status");
                        holder.no  = jo.getString("billing_payment_id");
                        holders.add(holder);
                        total = total + Long.parseLong(holder.pay);
                    }
                    txvw_total_00.setText(MyCurrency.toCurrnecy(total));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }
}
