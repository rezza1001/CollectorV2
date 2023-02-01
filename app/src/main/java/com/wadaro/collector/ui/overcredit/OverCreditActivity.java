package com.wadaro.collector.ui.overcredit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OverCreditActivity extends MyActivity {

    private ArrayList<DataHolder> allData = new ArrayList<>();
    private DataAdapter mAdapter;

    @Override
    protected void initListener() {
        mAdapter.setOnSelectedListener(data -> {
            Intent intent = new Intent(mActivity, DetailActivity.class);
            intent.putExtra("billing_id",data.noTTB);
            intent.putExtra("consumen_id",data.consumentId);
            intent.putExtra("product_id",data.productId);
            startActivity(intent);
        });

        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected int setLayout() {
        return R.layout.overcredit_frg_main;
    }

    @Override
    protected void initLayout() {
        TextView txvw_titile_00 = findViewById(R.id.txvw_titile_00);
        txvw_titile_00.setText("Data CTB");
        RecyclerView rcvw_data_00 = findViewById(R.id.rcvw_data_00);
        rcvw_data_00.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new DataAdapter(mActivity, allData);
        rcvw_data_00.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {
        allData.clear();
        PostManager post = new PostManager(mActivity,"over-credit");
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONArray ja = obj.getJSONArray("data");
                    for (int i=0; i<ja.length(); i++){
                        JSONObject jo = ja.getJSONObject(i);
                        DataHolder holder = new DataHolder();
                        holder.salesRcvId = jo.getString("sales_receive_id");
                        holder.coordinatorName = jo.getString("coordinator_name");
                        holder.consumentId = jo.getString("consumen_id");
                        holder.productId = jo.getString("product_id");
                        holder.noTTB = jo.getString("billing_id");
                        holder.tenor = jo.getInt("installment_period");
                        holder.total = Long.parseLong(jo.getString("payment_total").split("\\.")[0]);
                        holder.data = jo;
                        allData.add(holder);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.registerReceiver(receiver, new IntentFilter("REFRESH"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mActivity.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    };
}
