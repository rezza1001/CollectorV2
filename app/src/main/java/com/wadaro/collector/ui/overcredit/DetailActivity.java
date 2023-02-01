package com.wadaro.collector.ui.overcredit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.database.table.AssignmentDtlDB;
import com.wadaro.collector.database.table.SubmitDB;
import com.wadaro.collector.ui.assignment.KeyValView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class DetailActivity extends MyActivity {
    private static final int REQ_EDIt = 11;

    private LinearLayout lnly_body_00;


    @Override
    protected int setLayout() {
        return R.layout.overcredit_activity_detail;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        lnly_body_00 = findViewById(R.id.lnly_body_00);
        TextView txvw_title_00 = findViewById(R.id.txvw_titile_00);
        txvw_title_00.setText("Data Over Credit");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initData() {
        refresh();
    }

    @SuppressLint("SetTextI18n")
    private void refresh(){
//        try {
//            JSONObject dataPass = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("DATA")));
//            SubmitDB submitDB = new SubmitDB();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        lnly_body_00.removeAllViews();
        if (offlineMode){
            AssignmentDtlDB dtlDB = new AssignmentDtlDB();
            dtlDB.getData(mActivity, "");
            JSONObject data ;
            try {
                data = new JSONObject(dtlDB.data);
                buildData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        PostManager post = new PostManager(mActivity,"over-credit/detail");
        post.addParam(new ObjectApi("billing_id",getIntent().getStringExtra("billing_id")));
        post.addParam(new ObjectApi("consumen_id",getIntent().getStringExtra("consumen_id")));
        post.addParam(new ObjectApi("product_id",getIntent().getStringExtra("product_id")));
        post.execute("POST");
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
            DateFormat f1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat f2 = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));
            JSONObject details = data.getJSONObject("details");

            create("No. TTB",data.getString("billing_id"));
            create("No. Kwitansi",data.getString("sales_receive_id"));
            create("Koordinator",data.getString("coordinator_name"));
            create("Alamat",data.getString("coordinator_name"));
            create("No. KTP",data.getString("coordinator_ktp"));
            create("No. HP",data.getString("coordinator_phone"));
            create("Tgl Kirim", f2.format(Objects.requireNonNull(f1.parse(data.getString("delivery_date")))));
            create("Tgl Jatuh Tempo", f2.format(Objects.requireNonNull(f1.parse(data.getString("due_date")))));
            create("Nama Konsumen",details.getString("consumen_name"));
            create("Nama Barang",details.getString("product_name"));
            create("Angsuran",details.getString("installment"));
            create("Angsuran Ke",details.getString("installment_period"));

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

        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.bbtn_process_00).setOnClickListener(v -> {
            Intent intent = getIntent();
            intent.setClass(mActivity, UpdateActivity.class);
            startActivity(intent);
        });
    }

    private void create(String key, String value){
        KeyValView kv_namedb = new KeyValView(mActivity,null);
        kv_namedb.setKey(key);
        kv_namedb.setValue(value);
        lnly_body_00.addView(kv_namedb);
    }

}
