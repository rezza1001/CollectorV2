package com.wadaro.collector.ui.auth;

import android.widget.EditText;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.module.FailedWindow;
import com.wadaro.collector.module.SuccessWindow;
import com.wadaro.collector.module.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPwdActivity extends MyActivity {

    private EditText edtx_nik_00;

    @Override
    protected int setLayout() {
        return R.layout.activity_lupa_password;
    }

    @Override
    protected void initLayout() {
        edtx_nik_00 = findViewById(R.id.edtx_nik_00);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        findViewById(R.id.bbtn_reset_00).setOnClickListener(v -> reset());
    }

    private void reset(){
        String nik = edtx_nik_00.getText().toString();
        if (nik.isEmpty()){
            Utility.showToastError(mActivity,"Silahkan isi nik anda!");
            return;
        }

        PostManager post = new PostManager(mActivity,"forgot-password");
        post.addParam(new ObjectApi("user_name",nik));
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data  = obj.getJSONObject("data");
                    String responseMessage = "Verifikasi sudah di kirim ke "+data.getString("email")+". Silahkan cek email anda";
                    SuccessWindow success = new SuccessWindow(mActivity);
                    success.setDescription(responseMessage);
                    success.show();
                    success.setOnFinishListener(() -> mActivity.finish());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utility.showToastError(mActivity,"Error "+ e.getMessage());
                }
            }
            else {
                FailedWindow faild = new FailedWindow(mActivity);
                faild.setDescription(message);
                faild.show();
            }
        });
    }
}
