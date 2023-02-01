package com.wadaro.collector.ui.assignment.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.Global;
import com.wadaro.collector.base.MyFragment;
import com.wadaro.collector.database.table.AssignmentDB;
import com.wadaro.collector.database.table.DeliveryDB;
import com.wadaro.collector.ui.assignment.DenahActivity;
import com.wadaro.collector.ui.assignment.DetilActivity;
import com.wadaro.collector.ui.assignment.adapter.Assignment;
import com.wadaro.collector.ui.assignment.adapter.DeliveryAdapter;
import com.wadaro.collector.ui.assignment.adapter.DenahAdapter;
import com.wadaro.collector.util.MyPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;


public class TabelFragment extends MyFragment {

    private final ArrayList<Assignment> allData = new ArrayList<>();
    private final ArrayList<Assignment> filterData = new ArrayList<>();
    private final ArrayList<String> allDeanh = new ArrayList<>();
    private DenahAdapter mDenahAdapter;
    private DeliveryAdapter mAdapkter;
    private TextView txvw_size_00;

    private final ArrayList<String> dataMaps = new ArrayList<>();

    public static TabelFragment newInstance() {
        Bundle args = new Bundle();

        TabelFragment fragment = new TabelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setlayout() {
        return R.layout.fragment_tabel;
    }

    @Override
    protected void initLayout(View view) {

        RecyclerView rcvw_data_00 = view.findViewById(R.id.rcvw_data_00);
        rcvw_data_00.setLayoutManager(new LinearLayoutManager(mActivity));
        rcvw_data_00.setNestedScrollingEnabled(true);

        mAdapkter = new DeliveryAdapter(mActivity, filterData);
        rcvw_data_00.setAdapter(mAdapkter);

        txvw_size_00 = view.findViewById(R.id.txvw_size_00);

        RecyclerView rcvw_denah_00 = view.findViewById(R.id.rcvw_denah_00);
        rcvw_denah_00.setNestedScrollingEnabled(true);
        rcvw_denah_00.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mDenahAdapter = new DenahAdapter(mActivity, allDeanh);
        rcvw_denah_00.setAdapter(mDenahAdapter);
    }

    @Override
    protected void initListener() {
        mAdapkter.setOnSelectedListener(data -> {
            Intent intent = new Intent(mActivity, DetilActivity.class);
            intent.putExtra("DATA", data.data.toString());
            intent.putExtra("TTB", data.noTTB);
            intent.putExtra("BILLING_ID", data.billingId);
            startActivity(intent);
        });

        mDenahAdapter.setOnSelectedListener(data -> {
            Intent intent = new Intent(mActivity, DenahActivity.class);
            intent.putExtra("URL", data);
            startActivity(intent);
        });

    }

    @Override
    protected void initData() {
    }

    @SuppressLint("SetTextI18n")
    private void loadData(){
        allData.clear();
        allDeanh.clear();
        dataMaps.clear();

        if (MyPreference.getInt(mActivity, Global.PREF_OFFLINE_MODE) == 1){
            AssignmentDB assignmentDB = new AssignmentDB();
            for (AssignmentDB dbs : assignmentDB.getData(mActivity)){
                try {
                    JSONObject obj = new JSONObject(dbs.data);
                    buildData(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        PostManager post = new PostManager(mActivity,"billing-assignment");
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data = obj.getJSONObject("data");
                    JSONArray biling = data.getJSONArray("biling");
                    for (int i=0; i<biling.length(); i++){
                        JSONObject objData = biling.getJSONObject(i);
                        buildData(objData);
                    }

                    JSONArray denah = data.getJSONArray("denah");
                    for (int i=0; i<denah.length(); i++){
                        String image = denah.getString(i);
                        allDeanh.add(image);
                    }
                    mDenahAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            searchData("");

            txvw_size_00.setText("Total ada "+allData.size()+" lokasi penagihan");
            if (dataMaps.size() > 0){
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent("LOAD_DATA");
                    intent.putStringArrayListExtra("data",dataMaps );
                    if (isAdded()){
                        Objects.requireNonNull(getActivity()).sendBroadcast(intent);
                    }
                },1000);

            }
        });
    }

    private void buildData(JSONObject objData) throws JSONException {
        Assignment book = new Assignment();
        book.billingId = objData.getString("billing_id");
        book.noTTB = objData.getString("sales_receive_id");
        book.coordinatorName = objData.getString("coordinator_name");
        book.tenor = objData.getLong("installment_period");
        book.total = objData.getLong("invoice_total");
        book.data = objData;

        DeliveryDB deliveryDB = new DeliveryDB();
        deliveryDB.getData(mActivity, book.billingId);
        if (deliveryDB.data.isEmpty()){
            allData.add(book);
        }

        if (objData.getString("coordinator_location").split(",").length > 1){
            String latitude = objData.getString("coordinator_location").split(",")[0];
            String longitude = objData.getString("coordinator_location").split(",")[1];
            dataMaps.add("1::"+longitude+"::"+latitude);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PARAMETER");
        intentFilter.addAction("SEARCH");
        intentFilter.addAction("REFRESH");
        intentFilter.addAction("FINISH");
        mActivity.registerReceiver(receiver, intentFilter);
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

    private void searchData(String value){
        filterData.clear();
        if (value.isEmpty()){
            filterData.addAll(allData);
        }
        else {
            for (Assignment assignment: allData){
                if (assignment.coordinatorName.toLowerCase().contains(value.toLowerCase())){
                    filterData.add(assignment);
                }
            }
        }

        mAdapkter.notifyDataSetChanged();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "FINISH")){
                loadData();
            }
            else if (Objects.equals(intent.getAction(), "PARAMETER")){
                loadData();
            }
            else if (Objects.equals(intent.getAction(), "SEARCH")){
                String value = intent.getStringExtra("value");
                searchData(value);
            }
        }
    };
}
