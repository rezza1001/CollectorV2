package com.wadaro.collector.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.Global;
import com.wadaro.collector.database.table.UserDB;
import com.wadaro.collector.module.MyDevice;
import com.wadaro.collector.ui.home.HomePageActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;
    MyDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        device  = new MyDevice(this);
        /*
         * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company
         */
        new Handler().postDelayed(() -> {

            // init sharedPreference
            Global.startAppIniData(SplashScreenActivity.this);
            checkApps();

        }, SPLASH_TIME_OUT);

    }

    private void redirect(){

        UserDB userDB = new UserDB();
        userDB.getData(this);
        if (userDB.user_id.isEmpty()){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        else {
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
        }
        finish();
    }

    private void checkApps(){
        PostManager post = new PostManager(this,"https://erp.wadaro.id/wadaro-erp/android/api/v1/","check-version-app/collector");
        post.showloading(false);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data = obj.getJSONObject("data");
                    int version = Integer.parseInt(data.getString("version"));
                    Log.d("SplashScreenActivity","version DB "+ version+" : "+device.getVersionCode());
                    if (device.getVersionCode() < version){
                        startActivity(new Intent(SplashScreenActivity.this, UpdateAppsActivity.class));
                        SplashScreenActivity.this.finish();
                    }
                    else {
                        redirect();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                redirect();
            }
        });
    }
}
