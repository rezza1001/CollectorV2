package com.wadaro.collector.ui.assignment;

import android.annotation.SuppressLint;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.util.MyCurrency;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmationActivity extends MyActivity {

    private LinearLayout lnly_body_00;

    @Override
    protected int setLayout() {
        return R.layout.assignment_activity_confirmation;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        TextView txvw_title_00 = findViewById(R.id.txvw_titile_00);
        txvw_title_00.setText("Data Penugasan Penagihan");

        lnly_body_00 = findViewById(R.id.lnly_body_00);
    }

    @Override
    protected void initData() {
        String billingID = getIntent().getStringExtra("billing_payment_id");
        String param = "?billing_payment_id="+billingID;
        PostManager post = new PostManager(mActivity,"billing-assignment/confirm"+param);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK) {
                try {
                    JSONObject data = obj.getJSONArray("data").getJSONObject(0);
                    create("No. Kwitansi",data.getString("billing_payment_id"));
                    create("No. Tagihan",data.getString("billing_id"));
                    create("No. TTB",data.getString("sales_receive_id"));
                    create("Koordinator",data.getString("coordinator_name"));
                    create("Alamat",data.getString("coordinator_address"));
                    create("No. KTP",data.getString("coordinator_ktp"));
                    create("No. HP",data.getString("coordinator_phone"));
                    create("Tgl. Kirim",data.getString("delivery_date"));
                    create("Tgl. Jatuh Tempo",data.getString("payment_date").split(" ")[0]);
                    create("Nama JP",data.getString("consumen_name"));
                    create("Nama Barang",data.getString("product_name"));
                    create("Angsuran Ke",data.getString("installment_period"));
                    create("Angsuran", MyCurrency.toCurrnecy(data.getString("installment").split("\\.")[0]));
                    create("Total", MyCurrency.toCurrnecy(data.getString("payment_total").split("\\.")[0]));
                    create("Tanggal Pembayaran",data.getString("payment_date"));
                    create("Status Pembayaran",data.getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Utility.showToastError(mActivity,message);
            }
        });
    }

    @Override
    protected void initListener() {
        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());
    }

    private void create(String key, String value){
        KeyValView kv_namedb = new KeyValView(mActivity,null);
        kv_namedb.setKey(key);
        kv_namedb.setValue(value);
        lnly_body_00.addView(kv_namedb);
    }
}
