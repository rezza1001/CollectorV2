package com.wadaro.collector.ui.overcredit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Handler;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.InputBasicView;
import com.wadaro.collector.component.SelectHolder;
import com.wadaro.collector.component.SelectView;
import com.wadaro.collector.database.table.AssignmentDtlDB;
import com.wadaro.collector.database.table.SubmitDB;
import com.wadaro.collector.module.FailedWindow;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.ui.assignment.KeyValView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UpdateActivity extends MyActivity {
    private static final int REQ_EDIt = 11;

    private LinearLayout lnly_body_00;
    private InputBasicView input_name_00,input_phone_00,input_address_00,input_identity_00;
    private SelectView slvw_date_00;


    @Override
    protected int setLayout() {
        return R.layout.overcredit_activity_update;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        lnly_body_00        = findViewById(R.id.lnly_body_00);
        input_name_00       = findViewById(R.id.input_name_00);
        input_phone_00      = findViewById(R.id.input_phone_00);
        input_address_00    = findViewById(R.id.input_address_00);
        input_identity_00   = findViewById(R.id.input_identity_00);
        slvw_date_00   = findViewById(R.id.slvw_date_00);
        TextView txvw_title_00 = findViewById(R.id.txvw_titile_00);
        txvw_title_00.setText("Data Over Credit");

        input_name_00.setLabel("Nama *");

        input_phone_00.setLabel("No. Tlp");
        input_phone_00.setInputType(InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE);

        input_identity_00.setLabel("No. KTP");
        input_identity_00.setInputType(InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_NUMBER);

        input_address_00.setLabel("Alamat *");
        input_address_00.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        slvw_date_00.setHint("Tanggal Tagih *");
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

        slvw_date_00.setSelectedListener(data -> openDate());

        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.bbtn_process_00).setOnClickListener(v -> save());
    }

    private void openDate(){
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", new Locale("id"));
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity, (view, year1, monthOfYear, dayOfMonth) -> {
            try {
                Date date = format2.parse(year1 +"-"+ (monthOfYear + 1)+"-"+ dayOfMonth);
                assert date != null;
                slvw_date_00.setValue(new SelectHolder(format2.format(date),format2.format(date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }, year, month, day);
        datePickerDialog.show();
    }

    private void create(String key, String value){
        KeyValView kv_namedb = new KeyValView(mActivity,null);
        kv_namedb.setKey(key);
        kv_namedb.setValue(value);
        lnly_body_00.addView(kv_namedb);
    }

    private void save(){
        String name     = input_name_00.getValue();
        String phone    = input_phone_00.getValue();
        String ktp      = input_identity_00.getValue();
        String address  = input_address_00.getValue();
        String date     = slvw_date_00.getValue();

        if (name.isEmpty()){
            Utility.showToastError(mActivity, "Nama Harus Diisi!");
            return;
        }
        if (phone.isEmpty()){
            Utility.showToastError(mActivity, "Nomor Hp Harus Diisi!");
            return;
        }
        if (ktp.isEmpty()){
            Utility.showToastError(mActivity, "Nomor KTP Harus Diisi!");
            return;
        }
        if (address.isEmpty()){
            Utility.showToastError(mActivity, "Alamat Harus Diisi!");
            return;
        }
        if (date.isEmpty()){
            Utility.showToastError(mActivity, "Tanggal Tagih Harus Diisi!");
            return;
        }

        PostManager post = new PostManager(mActivity,"over-credit/detail/save");
        post.addParam(new ObjectApi("billing_id",getIntent().getStringExtra("billing_id")));
        post.addParam(new ObjectApi("product_id",getIntent().getStringExtra("product_id")));
        post.addParam(new ObjectApi("consumen_id",getIntent().getStringExtra("consumen_id")));
        post.addParam(new ObjectApi("consumen_name",name));
        post.addParam(new ObjectApi("consumen_phone",phone));
        post.addParam(new ObjectApi("consumen_nik",ktp));
        post.addParam(new ObjectApi("consumen_address",address));
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                Utility.showToastSuccess(mActivity, message);
                sendBroadcast(new Intent("REFRESH"));
                new Handler().postDelayed(() -> mActivity.finish(),100);
            }
            else {
                FailedWindow window = new FailedWindow(mActivity);
                window.setDescription(message);
                window.show();
            }
        });
    }


}
