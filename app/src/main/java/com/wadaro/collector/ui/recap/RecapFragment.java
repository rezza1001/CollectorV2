package com.wadaro.collector.ui.recap;

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

public class RecapFragment extends MyFragment {

    private SelectView slvw_date_00;
    private RecapAdapter adapterBill, adapterPay;
    private TextView txvw_qty_00,txvw_total_00,txvw_qty_11,txvw_total_11;
    private final ArrayList<RecapHolder> holderBill = new ArrayList<>();
    private final ArrayList<RecapHolder> holderPay = new ArrayList<>();

    DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", new Locale("id"));

    public static RecapFragment newInstance() {
        
        Bundle args = new Bundle();
        
        RecapFragment fragment = new RecapFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    protected int setlayout() {
        return R.layout.recap_fragment_main;
    }

    @Override
    protected void initLayout(View view) {
        slvw_date_00 = view.findViewById(R.id.slvw_date_00);
        slvw_date_00.setHint("Tanggal");

        RecyclerView rcvw_bill_00 = view.findViewById(R.id.rcvw_data_00);
        rcvw_bill_00.setLayoutManager(new LinearLayoutManager(mActivity));
        adapterBill = new RecapAdapter(mActivity, holderBill);
        rcvw_bill_00.setAdapter(adapterBill);

        RecyclerView rcvw_pay_11 = view.findViewById(R.id.rcvw_data_11);
        rcvw_pay_11.setLayoutManager(new LinearLayoutManager(mActivity));
        adapterPay = new RecapAdapter(mActivity, holderPay);
        rcvw_pay_11.setAdapter(adapterPay);

        txvw_total_00 = view.findViewById(R.id.txvw_total_00);
        txvw_qty_00 = view.findViewById(R.id.txvw_qty_00);

        txvw_total_11 = view.findViewById(R.id.txvw_total_11);
        txvw_qty_11 = view.findViewById(R.id.txvw_qty_11);
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
        holderBill.clear();
        holderPay.clear();
        PostManager post = new PostManager(mActivity,"report/rekap-billing?billing_date="+slvw_date_00.getKey());
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data = obj.getJSONObject("data");
                    JSONObject billing = data.getJSONObject("billings");
                    JSONArray billData = billing.getJSONArray("data");
                    for (int i=0; i<billData.length(); i++){
                        JSONObject jo = billData.getJSONObject(i);
                        RecapHolder holder = new RecapHolder();
                        holder.qty = jo.getInt("jumlah");
                        holder.total = Long.parseLong(jo.getString("nominal").split("\\.")[0]);
                        holder.note = jo.getString("keterangan");
                        holderBill.add(holder);
                    }
                    txvw_total_00.setText(billing.getString("total"));
                    txvw_qty_00.setText(MyCurrency.toCurrnecy(billing.getString("jumlah")));


                    JSONObject payment = data.getJSONObject("payments");
                    JSONArray payData = payment.getJSONArray("data");
                    for (int i=0; i<payData.length(); i++){
                        JSONObject jo = payData.getJSONObject(i);
                        RecapHolder holder = new RecapHolder();
                        holder.qty = jo.getInt("jumlah");
                        holder.total = Long.parseLong(jo.getString("nominal").split("\\.")[0]);
                        holder.note = jo.getString("keterangan");
                        holderPay.add(holder);
                    }
                    txvw_total_11.setText(payment.getString("total"));
                    txvw_qty_11.setText(MyCurrency.toCurrnecy(payment.getString("jumlah")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapterBill.notifyDataSetChanged();
            adapterPay.notifyDataSetChanged();
        });
    }
}
