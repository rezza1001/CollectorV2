package com.wadaro.collector.ui.assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.FormPost;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.InputBasicView;
import com.wadaro.collector.component.SelectHolder;
import com.wadaro.collector.component.SelectView;
import com.wadaro.collector.database.table.ConsumenDtlDB;
import com.wadaro.collector.database.table.SaveAssignmentDB;
import com.wadaro.collector.module.FailedWindow;
import com.wadaro.collector.module.FileProcessing;
import com.wadaro.collector.module.ImageResizer;
import com.wadaro.collector.module.SuccessWindow;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.module.find.FindHolder;
import com.wadaro.collector.module.find.FindWindow;
import com.wadaro.collector.util.MyCurrency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UpdateActivity extends MyActivity {

    private static final int REQ_REASON = 1;
    private static final int REQ_CHANGE_GOODS = 2;
    private static final int REQ_PHOTO = 3;

    private LinearLayout lnly_body_00,lnly_photo_00;
    private SelectView slvw_status_00,slvw_expiered_00,slvw_reason_00;
    private InputBasicView input_pay_00,input_note_00;
    private RoundedImageView imvw_product_00;
    private long total = 0;
    long debt = 0;
    long installment = 0;
    private String consumenName, productName;
    ArrayList<FindHolder> reasons = new ArrayList<>();
    ArrayList<FindHolder> payStatus = new ArrayList<>();
    private static final String photo_path      = "/Wadaro/collector/";

    @Override
    protected int setLayout() {
        return R.layout.assignment_activity_update;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        TextView txvw_title_00 = findViewById(R.id.txvw_titile_00);
        txvw_title_00.setText("Penugasan Penagihan");

        lnly_body_00    = findViewById(R.id.lnly_body_00);
        slvw_status_00  = findViewById(R.id.slvw_status_00);
        slvw_reason_00  = findViewById(R.id.slvw_reason_00);
        input_pay_00    = findViewById(R.id.input_pay_00);
        slvw_expiered_00 = findViewById(R.id.slvw_expiered_00);
        input_note_00    = findViewById(R.id.input_note_00);
        lnly_photo_00    = findViewById(R.id.lnly_photo_00);
        imvw_product_00  = findViewById(R.id.imvw_product_00);

        slvw_status_00.setHint("Status Pembayaran");

        slvw_reason_00.setHint("Alasan");
        slvw_reason_00.setVisibility(View.GONE);

        slvw_expiered_00.setHint("Tanggal Janji Bayar");
        slvw_expiered_00.setVisibility(View.GONE);

        input_pay_00.setLabel("Total Pembayaran (Rp)");
        input_pay_00.setInputType(InputType.TYPE_CLASS_NUMBER,InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input_pay_00.setVisibility(View.GONE);

        input_note_00.setLabel("Catatan");
        input_note_00.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE, InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        input_note_00.setVisibility(View.GONE);

        lnly_photo_00.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        slvw_status_00.setValue(new SelectHolder("314", "LUNAS"));
        input_pay_00.setVisibility(View.GONE);
        refresh();
    }

    @Override
    protected void initListener() {
        slvw_status_00.setSelectedListener(data -> {
            FindWindow findWindow = new FindWindow(mActivity);
            findWindow.setHeaderTitle(slvw_status_00.getHint());
            findWindow.show(payStatus);
            findWindow.setOnSelectedListener(data1 -> {
                slvw_status_00.setValue(new SelectHolder(data1.getKey(), data1.getValue()));
                slvw_reason_00.setEmpty();
                slvw_expiered_00.setEmpty();
                input_pay_00.setValue("");
                input_note_00.setValue("");
                FileProcessing.createFolder(mActivity,photo_path);
                FileProcessing.clearImage(mActivity,photo_path);

                switch (data1.getKey()) {
                    case "314":  // Lunas
                        slvw_reason_00.setVisibility(View.GONE);
                        slvw_expiered_00.setVisibility(View.GONE);
                        input_pay_00.setVisibility(View.GONE);
                        input_note_00.setVisibility(View.GONE);
                        lnly_photo_00.setVisibility(View.GONE);
                        total = installment;
                        break;
                    case "315":  // Titp
                        slvw_reason_00.setVisibility(View.GONE);
                        slvw_expiered_00.setVisibility(View.GONE);
                        input_pay_00.setVisibility(View.VISIBLE);
                        input_note_00.setVisibility(View.GONE);
                        lnly_photo_00.setVisibility(View.GONE);
                        break;
                    case "316":  // Mundur
                        slvw_reason_00.setVisibility(View.GONE);
                        slvw_expiered_00.setVisibility(View.VISIBLE);
                        input_pay_00.setVisibility(View.VISIBLE);
                        input_note_00.setVisibility(View.VISIBLE);
                        lnly_photo_00.setVisibility(View.GONE);
                        total = installment + debt;
                        break;
                    case "317":  // Tarik
                        slvw_expiered_00.setVisibility(View.GONE);
                        input_pay_00.setVisibility(View.GONE);
                        lnly_photo_00.setVisibility(View.GONE);
                        slvw_reason_00.setVisibility(View.VISIBLE);
                        input_note_00.setLabel("Catatan");
                        input_note_00.setVisibility(View.VISIBLE);
                        total = installment;
                        break;
                    case "318":  // Service
                        slvw_expiered_00.setVisibility(View.GONE);
                        input_pay_00.setVisibility(View.VISIBLE);
                        slvw_reason_00.setVisibility(View.GONE);
                        input_note_00.setLabel("Catatan Kerusakan *");
                        input_note_00.setVisibility(View.VISIBLE);
                        lnly_photo_00.setVisibility(View.VISIBLE);
                        total = installment;
                        break;
                }
            });
        });

        slvw_expiered_00.setSelectedListener(data -> openDate());

        slvw_reason_00.setSelectedListener(data -> {
            FindWindow findWindow = new FindWindow(mActivity);
            findWindow.setHeaderTitle(slvw_reason_00.getHint());
            findWindow.show(reasons);
            findWindow.setOnSelectedListener(data1 -> slvw_reason_00.setValue(new SelectHolder(data1.getKey(), data1.getValue())));
        });

        findViewById(R.id.bbtn_save_00).setOnClickListener(v -> audit());
        findViewById(R.id.bbtn_cancel_00).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());
        lnly_photo_00.setOnClickListener(v -> openCamera());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REASON && resultCode == RESULT_OK){
            assert data != null;
            slvw_reason_00.setValue(new SelectHolder(data.getStringExtra("DATA")));
        }
        else if (requestCode == REQ_CHANGE_GOODS && resultCode == RESULT_OK){
            refresh();
        }
        else if (requestCode == REQ_PHOTO && resultCode == RESULT_OK){
            Utility.showToastSuccess(mActivity,"Photo saved to draf");
            Glide.with(mActivity).load(FileProcessing.openImage(mActivity,photo_path,"imagereason.jpg")).into(imvw_product_00);
            ImageResizer resizer = new ImageResizer(mActivity);
            resizer.compress(photo_path+"imagereason.jpg",photo_path,"imagereason_compress.jpg");
        }
    }

    private void addInfo(String key, String value){
        KeyValView kv_namedb = new KeyValView(mActivity,null);
        kv_namedb.setKey(key);
        kv_namedb.setValue(value);
        lnly_body_00.addView(kv_namedb);
    }

    private void refresh(){
        lnly_body_00.removeAllViews();
        reasons.clear();
        if (offlineMode){
            ConsumenDtlDB dtlDB = new ConsumenDtlDB();
            dtlDB.getData(mActivity, getIntent().getStringExtra("billing_id"), getIntent().getStringExtra("consumen_id"),getIntent().getStringExtra("product_id"));
            try {
                buildData(new JSONObject(dtlDB.data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        PostManager post = new PostManager(mActivity,"billing-assignment/detail/consumen");
        post.addParam(new ObjectApi("billing_id",getIntent().getStringExtra("billing_id")));
        post.addParam(new ObjectApi("consumen_id",getIntent().getStringExtra("consumen_id")));
        post.addParam(new ObjectApi("product_id",getIntent().getStringExtra("product_id")));
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                buildData(obj);
            }
        });
    }

    private void buildData(JSONObject obj){
        try {
            JSONObject data = obj.getJSONObject("data");
            JSONObject billing = data.getJSONObject("billing");
            JSONArray jaStatus = data.getJSONArray("payment_status");
            JSONArray jaReasons = data.getJSONArray("reason_status");

            DateFormat f1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat f2 = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));
            lnly_body_00.removeAllViews();
            addInfo("Nomor Tagihan",billing.getString("billing_id"));
            addInfo("No. TTB",billing.getString("sales_receive_id"));
            addInfo("Koordinator",billing.getString("coordinator_name"));
            addInfo("Alamat",billing.getString("coordinator_address"));
            addInfo("No.KTP",billing.getString("coordinator_ktp"));
            addInfo("No.HP",billing.getString("coordinator_phone"));
            addInfo("Tanggal Kirim",f2.format(Objects.requireNonNull(f1.parse(billing.getString("delivery_date")))));
            addInfo("Nama JP",billing.getString("consumen_name"));
            addInfo("Nama Product",billing.getString("product_name"));
            addInfo("Angsuran ke",billing.getString("installment_period"));
            debt             = Long.parseLong(billing.getString("tunggakan").split("\\.")[0]);
            installment      = Long.parseLong(billing.getString("installment").split("\\.")[0]);
            total            = installment;

            addInfo("Angsuran",MyCurrency.toCurrnecy(installment));
            addInfo("Tunggakan / Titip Setor", MyCurrency.toCurrnecy(debt));
            addInfo("Total",MyCurrency.toCurrnecy(total));

            productName     = billing.getString("product_name");
            consumenName    = billing.getString("consumen_name");
            payStatus.clear();
            for (int i=0; i<jaStatus.length(); i++){
                JSONObject jo = jaStatus.getJSONObject(i);
                payStatus.add(new FindHolder(jo.getString("status_id"),jo.getString("status_name")));
            }
            reasons.clear();
            for (int i=0; i<jaReasons.length(); i++){
                JSONObject jo = jaReasons.getJSONObject(i);
                reasons.add(new FindHolder(jo.getString("status_id"),jo.getString("status_name")));
            }


        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void openDate(){
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", new Locale("id"));
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        DatePickerDialog  datePickerDialog = new DatePickerDialog(mActivity, (view, year1, monthOfYear, dayOfMonth) -> {
            try {
                Date date = format2.parse(year1 +"-"+ (monthOfYear + 1)+"-"+ dayOfMonth);
                assert date != null;
                slvw_expiered_00.setValue(new SelectHolder(format2.format(date),format2.format(date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }, year, month, day);
        datePickerDialog.show();
    }

    private void openCamera(){
        String[] PERMISSIONS = {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!hasPermissions(mActivity, PERMISSIONS)){
            ActivityCompat.requestPermissions(Objects.requireNonNull(mActivity), PERMISSIONS, 101);
        }
        else {
            String mediaPath = FileProcessing.getMainPath(mActivity).getAbsolutePath()+photo_path;
            String file =mediaPath+ "imagereason" +".jpg";
            File newfile = new File(file);
            try {
                newfile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            Uri outputFileUri = FileProcessing.getUriFormFile(mActivity,newfile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            startActivityForResult(cameraIntent, UpdateActivity.REQ_PHOTO);
        }
    }
    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void audit(){
        File sd     = FileProcessing.getMainPath(mActivity);

        String sTotal = input_pay_00.getValue();
        long payTotal = 0;

        if (!sTotal.isEmpty()){
            sTotal = sTotal.replace(".","");
            payTotal = Long.parseLong(sTotal);
        }

        if (slvw_expiered_00.getVisibility() == View.VISIBLE && slvw_expiered_00.getKey().isEmpty()){
            Utility.showToastError(mActivity,"Tanggal Janji Bayar Harus di isi");
            return;
        }

        if (input_pay_00.getVisibility() == View.VISIBLE ){
            String pStatus = slvw_status_00.getValue();
            if (input_pay_00.getValue().isEmpty()){
                Utility.showToastError(mActivity,"Total Pembayaran Harus di isi");
                return;
            }
            if (pStatus.equals("MUNDUR WAKTU") &&  (payTotal >= total)){
                Utility.showToastError(mActivity,"Total Pembayaran lebih atau sama dengan Total");
                return;
            }
            if (pStatus.equals("SERVICE BARANG") &&  (payTotal > total)){
                Utility.showToastError(mActivity,"Total Pembayaran lebih dari Total");
                return;
            }
            if (pStatus.equals("TITIP SETOR") &&  (payTotal <= total)){
                Utility.showToastError(mActivity,"Total Pembayaran kurang dari atau sama dengan total");
                return;
            }

        }

        if (input_note_00.getVisibility() == View.VISIBLE && input_note_00.getValue().isEmpty()){
            Utility.showToastError(mActivity,"Catatan Harus di isi");
            return;
        }
        if (slvw_reason_00.getVisibility() == View.VISIBLE && slvw_reason_00.getKey().isEmpty()){
            Utility.showToastError(mActivity,"Alasan Harus di isi");
            return;
        }
        if (lnly_photo_00.getVisibility() == View.VISIBLE && FileProcessing.openImage(mActivity,photo_path+"imagereason_compress.jpg") == null){
            Utility.showToastError(mActivity,"Photo barang terlebih dahulu");
            return;
        }
        FormPost post = new FormPost(mActivity,"billing-assignment/detail/consumen/save");
        if (Objects.requireNonNull(getIntent().getStringExtra("status")).equalsIgnoreCase("MUNDUR WAKTU")){
            post = new FormPost(mActivity,"billing-assignment/detail/consumen/re-payment");
        }
        post.addParam(new ObjectApi("billing_id",getIntent().getStringExtra("billing_id")));
        post.addParam(new ObjectApi("consumen_id",getIntent().getStringExtra("consumen_id")));
        post.addParam(new ObjectApi("product_id",getIntent().getStringExtra("product_id")));
        post.addParam(new ObjectApi("payment_status",slvw_status_00.getValue()));
        post.addParam(new ObjectApi("total",total));
        post.addParam(new ObjectApi("payment_total",payTotal));
        switch (slvw_status_00.getValue()) {
            case "LUNAS":
                post.addParam(new ObjectApi("payment_total", total));
                break;
            case "MUNDUR WAKTU":
                post.addParam(new ObjectApi("repayment_date", slvw_expiered_00.getKey()));
                post.addParam(new ObjectApi("payment_note", input_note_00.getValue()));
                break;
            case "CALON TARIK BARANG":
                post.addParam(new ObjectApi("payment_reason", slvw_reason_00.getValue()));
                post.addParam(new ObjectApi("payment_note", input_note_00.getValue()));
                break;
            case "SERVICE BARANG":
                post.addParam(new ObjectApi("payment_note", input_note_00.getValue()));
                post.addImage("photo", sd.getAbsolutePath() + photo_path + "imagereason_compress.jpg");
                break;
        }
        if (offlineMode){ // OFFLINE MODE
            saveToDraft(post);
            return;
        }

        post.execute();
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                SuccessWindow successWindow = new SuccessWindow(mActivity);
                successWindow.setDescription("Data berhasil disimpan!");
                successWindow.show();
                successWindow.setOnFinishListener(() -> {
                    sendBroadcast(new Intent("FINISH"));
                    new Handler().postDelayed(() -> mActivity.finish(),100);
                });
            }
            else {
                FailedWindow failedWindow = new FailedWindow(mActivity);
                failedWindow.setDescription(message);
                failedWindow.show();
            }
        });
    }

    private void saveToDraft(FormPost post){
        if (lnly_photo_00.getVisibility() == View.VISIBLE){
            String name = System.currentTimeMillis()+".jpeg";
            String path_file = photo_path+"draft/";

            File sd     = FileProcessing.getMainPath(mActivity);
            post.addImage("photo",sd.getAbsolutePath()+path_file+name);

            FileProcessing fileProcessing = new FileProcessing();
            fileProcessing.saveToTmp(mActivity,FileProcessing.openImage(mActivity,photo_path+"imagereason_compress.jpg"),path_file,name);
        }

        SaveAssignmentDB db = new SaveAssignmentDB();
        String id = getIntent().getStringExtra("billing_id")+"::"+getIntent().getStringExtra("consumen_id")+"::"+getIntent().getStringExtra("product_id");
        db.id       = id;
        db.data     = post.getData().toString();
        db.images   = post.getImages().toString();


        db.otherData   = consumenName+"::"+productName;
        db.insert(mActivity);

        sendBroadcast(new Intent("FINISH"));
        new Handler().postDelayed(() -> {
            Utility.showToastSuccess(mActivity, "Data berhasil disimpan ke draft");
            mActivity.finish();
        },1000);


    }

}
