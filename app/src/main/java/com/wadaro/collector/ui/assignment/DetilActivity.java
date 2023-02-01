package com.wadaro.collector.ui.assignment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.WarningWindow;
import com.wadaro.collector.database.table.AssignmentDtlDB;
import com.wadaro.collector.database.table.AssignmentFinishDB;
import com.wadaro.collector.database.table.SaveAssignmentDB;
import com.wadaro.collector.database.table.SubmitDB;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.ui.assignment.adapter.OrderAdapter;
import com.wadaro.collector.ui.assignment.adapter.OrderHolder;
import com.wadaro.collector.util.MyCurrency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DetilActivity extends MyActivity {
    private static final int REQ_EDIt = 11;

    private LinearLayout lnly_body_00;
    private final ArrayList<OrderHolder> orders = new ArrayList<>();
    private OrderAdapter mAdapter ;
    private TextView txvw_size_00,txvw_money_00;
    private String mBillID = "";
    private String mSalesReceiveId = "";


    @Override
    protected int setLayout() {
        return R.layout.assignment_activity_detail;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        lnly_body_00 = findViewById(R.id.lnly_body_00);
        txvw_size_00 = findViewById(R.id.txvw_size_00);
        txvw_money_00 = findViewById(R.id.txvw_money_00);
        RecyclerView rcvw_data_00 = findViewById(R.id.rcvw_data_00);
        rcvw_data_00.setLayoutManager(new LinearLayoutManager(mActivity));
        rcvw_data_00.setNestedScrollingEnabled(false);
        mAdapter = new OrderAdapter(mActivity, orders);
        rcvw_data_00.setAdapter(mAdapter);

        TextView txvw_title_00 = findViewById(R.id.txvw_titile_00);
        txvw_title_00.setText("Penugasan Pengiriman");

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initData() {
        mBillID = getIntent().getStringExtra("BILLING_ID");
        refresh();
    }

    @SuppressLint("SetTextI18n")
    private void refresh(){
        try {
            JSONObject dataPass = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("DATA")));
            SubmitDB submitDB = new SubmitDB();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        lnly_body_00.removeAllViews();
        txvw_money_00.setText("0");
        if (offlineMode){
            AssignmentDtlDB dtlDB = new AssignmentDtlDB();
            dtlDB.getData(mActivity, mBillID);
            JSONObject data ;
            try {
                data = new JSONObject(dtlDB.data);
                buildData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        String param = "?billing_id="+mBillID;
        PostManager post = new PostManager(mActivity,"billing-assignment/detail"+param);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data = obj.getJSONObject("data");
                    buildData(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void buildData(JSONObject data){
        try {
            JSONArray details = data.getJSONArray("details");

            DateFormat f1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat f2 = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));

            mSalesReceiveId = data.getString("sales_receive_id");
            create("Nomor Tagihan",data.getString("billing_id"));
            create("No. TTB",data.getString("sales_receive_id"));
            create("Koordinator",data.getString("coordinator_name"));
            create("Alamat",data.getString("coordinator_name"));
            create("No. KTP",data.getString("coordinator_ktp"));
            create("No. HP",data.getString("coordinator_phone"));
            create("Tanggal Jatuh Tempo", f2.format(Objects.requireNonNull(f1.parse(data.getString("delivery_date")))));

            orders.clear();
            long receive = 0;
            for (int i=0; i<details.length(); i++){
                JSONObject datil = details.getJSONObject(i);
                OrderHolder holder  = new OrderHolder();
                holder.id           = datil.getString("billing_detail_id");
                holder.paymentID    = data.getString("billing_payment_id");
                holder.jpName       = datil.getString("consumen_name");
                holder.installment  = datil.getString("payment_total");
                holder.period       = datil.getString("installment_period");
                holder.status       = datil.getString("status");
                holder.jpID         = datil.getString("consumen_id");

                if (datil.has("product_id")){
                    holder.productId    = datil.getString("product_id");
                }
                holder.billingId    = data.getString("billing_id");
                String id = data.getString("billing_id")+"::"+holder.jpID+"::"+holder.productId;
                SaveAssignmentDB assignmentDB = new SaveAssignmentDB();
                assignmentDB.getData(mActivity, id);
                if (!assignmentDB.data.isEmpty()){
                    JSONObject post = new JSONObject(assignmentDB.data);
                    holder.status       = "Draft ("+post.getString("payment_status")+")";
                }

                orders.add(holder);
                receive = receive + (Long.parseLong(datil.getString("installment").split("\\.")[0]));
            }
            mAdapter.notifyDataSetChanged();
            txvw_money_00.setText("Rp. "+ MyCurrency.toCurrnecy(receive));
            txvw_size_00.setText("Total ada "+ orders.size()+" data");
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIt && resultCode == RESULT_OK){
            assert data != null;
            new Handler().postDelayed(() -> {
                Intent intent = new Intent("REFRESH");
                sendBroadcast(intent);
                mActivity.finish();
            },200);

        }
        else if (requestCode == REQ_EDIt && resultCode == RESULT_CANCELED) {
            mActivity.finish();
        }
    }

    @Override
    protected void initListener() {
        mAdapter.setOnSelectedListener(data -> {
            boolean statusIndicator = !data.status.equals("null")
                    && (!data.status.equals("")
                    && !data.status.contains("Draft")
                    && !data.status.equalsIgnoreCase("MUNDUR WAKTU"));
            Intent intent = getIntent();
            intent.putExtra("sales_receive_id", mSalesReceiveId);
            intent.putExtra("detail_id", data.id);
            intent.putExtra("billing_payment_id", data.paymentID);
            intent.putExtra("billing_id", data.billingId);
            intent.putExtra("consumen_id", data.jpID);
            intent.putExtra("product_id", data.productId);
            intent.putExtra("status", data.status);
            if (statusIndicator){
                intent.setClass(mActivity, ConfirmationActivity.class);
            }
            else {
                intent.setClass(mActivity, UpdateActivity.class);
            }
            startActivity(intent);
        });

        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.bbtn_finish_00).setOnClickListener(v -> {
            WarningWindow warningWindow = new WarningWindow(mActivity);
            warningWindow.show("Informasi","Anda yakin untuk menyelesaikan penagihan ini?");
            warningWindow.setOnSelectedListener(status -> {
                if (status == 2){
                    saveData();
                }
            });
        });
    }

    private void create(String key, String value){
        KeyValView kv_namedb = new KeyValView(mActivity,null);
        kv_namedb.setKey(key);
        kv_namedb.setValue(value);
        lnly_body_00.addView(kv_namedb);
    }

    private void saveData(){
        PostManager post = new PostManager(mActivity,"billing-assignment/detail/consumen/finish");
        post.addParam(new ObjectApi("billing_id", getIntent().getStringExtra("BILLING_ID")));
        if (offlineMode){
            AssignmentFinishDB db = new AssignmentFinishDB();
            db.id = getIntent().getStringExtra("BILLING_ID");
            db.data = post.getData().toString();
            db.insert(mActivity);
            sendBroadcast(new Intent("FINISH"));
            new Handler().postDelayed(() -> {
                Utility.showToastSuccess(mActivity,"Data Berhasil disimpan");
                mActivity.finish();
            },500);
            return;
        }
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                sendBroadcast(new Intent("FINISH"));
                mActivity.finish();
            }
            else {
                Utility.showToastError(mActivity, message);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter("FINISH"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals("FINISH")){
                refresh();
            }
        }
    };


}
