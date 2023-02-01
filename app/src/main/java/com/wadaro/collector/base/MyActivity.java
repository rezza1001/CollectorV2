package com.wadaro.collector.base;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadaro.collector.R;
import com.wadaro.collector.component.WarningWindow;
import com.wadaro.collector.database.table.UserDB;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.util.MyPreference;
import com.wadaro.collector.util.NetworkUtil;


public abstract class MyActivity extends AppCompatActivity {

    protected String TAG = "MyActivity";
    protected Activity mActivity;
    private RelativeLayout loadingview;
    protected UserDB mUser;
    protected boolean offlineMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayout());
        mActivity = this;
        TAG = mActivity.getClass().getName();
        mUser = new UserDB();
        mUser.getData(mActivity);

        checkConnection();

        // DEFAULT
        createLoading();
        initLayout();
        initData();
        initListener();

    }

    protected abstract int setLayout();
    protected abstract void initLayout();
    protected abstract void initData();
    protected abstract void initListener();

    protected void setCustomColorNavbar(int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    protected void checkConnection(){
//        if (NetworkUtil.getConnectivityStatus(mActivity) && NetworkUtil.isNetworkAvailable(mActivity)){
//            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,0);
//        }
//        else {
//            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,1);
//        }
        if (MyPreference.getInt(mActivity, Global.PREF_OFFLINE_MODE) == 1){
            offlineMode = true;
            setImageNetwork(Color.GRAY);
        }
        else {
            if (NetworkUtil.getConnectivityStatus(mActivity) && NetworkUtil.isNetworkAvailable(mActivity)){
                if (NetworkUtil.getConnection(mActivity).type < 8){
                    WarningWindow warningWindow = new WarningWindow(mActivity);
                    warningWindow.show("Perhatian", "Koneksi anda tidak bagus, Apakah anda akan menggunakan mode Offline?");
                    warningWindow.setOnSelectedListener(status -> {
                        if (status == 2){
                            offlineMode = true;
                            setImageNetwork(Color.GRAY);
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,1);
                        }
                        else {
                            offlineMode = false;
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,0);
                            setImageNetwork(getResources().getColor(R.color.but_cancel));
                        }
                    });
                }
                else {
                    offlineMode = false;
                    MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,0);
                    setImageNetwork(getResources().getColor(R.color.colorPrimary));
                }

            }
            else {
                offlineMode = true;
                setImageNetwork(Color.GRAY);
                MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,1);
            }

        }
    }

    private void setImageNetwork(int color){
        ImageView imvw_network_00 = findViewById(R.id.imvw_network_00);
        TextView txvw_network_00 = findViewById(R.id.txvw_network_00);
        if (imvw_network_00 != null){
            imvw_network_00.setColorFilter(color);
            Log.d("TAGRZ","offlineMode "+offlineMode);
            if (offlineMode){
                txvw_network_00.setText("Offline");
            }
            else {
                txvw_network_00.setText("Online");
            }

            imvw_network_00.setOnClickListener(v -> {
                WarningWindow warningWindow = new WarningWindow(mActivity);
                if (MyPreference.getInt(mActivity, Global.PREF_OFFLINE_MODE) == 0){
                    warningWindow.show("Perhatian", "Anda dalam mode online. Anda yakin untuk masuk mode offline?");
                    warningWindow.setOnSelectedListener(status -> {
                        if (status == 2){
                            setImageNetwork(Color.GRAY);
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,1);
                            offlineMode = true;
                            txvw_network_00.setText("Offline");
                        }
                        else {
                            txvw_network_00.setText("Online");
                            offlineMode = false;
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,0);
                            setImageNetwork(getResources().getColor(R.color.colorPrimary));
                        }
                    });
                }
                else {
                    warningWindow.show("Perhatian", "Anda dalam mode offline. Anda akan masuk mode online");
                    warningWindow.setOnSelectedListener(status -> {
                        if (status == 1){
                            offlineMode = true;
                            setImageNetwork(Color.GRAY);
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,1);
                            txvw_network_00.setText("Offline");
                        }
                        else {
                            txvw_network_00.setText("Online");
                            offlineMode = false;
                            MyPreference.save(mActivity,Global.PREF_OFFLINE_MODE,0);
                            setImageNetwork(getResources().getColor(R.color.colorPrimary));
                        }
                    });
                }
            });
        }
    }

    private void createLoading(){
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        loadingview = new RelativeLayout(mActivity, null);
        ViewGroup.LayoutParams lp = viewGroup.getLayoutParams();
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        loadingview.setVisibility(View.GONE);
        viewGroup.addView(loadingview,lp);

        ProgressBar progressBar = new ProgressBar(mActivity, null,android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utility.getPixelValue(mActivity,25));
        lp2.topMargin = Utility.getPixelValue(mActivity, -12);
        loadingview.addView(progressBar, lp2);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#1c94fc"), PorterDuff.Mode.SRC_IN);
        setLoadingBarColor(2,0);
    }

    protected void showLoadingBar(int margintop, int background){
        margintop = margintop - 12;
        loadingview.setBackgroundColor(background);
        loadingview.setVisibility(View.VISIBLE);
        loadingview.setOnClickListener(null);

        ProgressBar progressBar = (ProgressBar) loadingview.getChildAt(0);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
        lp.topMargin = Utility.getPixelValue(mActivity, margintop);
    }

    protected void setLoadingBarColor(int margintop, int background){
        margintop = margintop - 12;
        loadingview.setBackgroundColor(background);
        loadingview.setOnClickListener(null);

        ProgressBar progressBar = (ProgressBar) loadingview.getChildAt(0);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
        lp.topMargin = Utility.getPixelValue(mActivity, margintop);
    }

    protected RelativeLayout getLoadingBar(){
        return loadingview;
    }

    protected void hideLoadingBar(){
        loadingview.setVisibility(View.GONE);
    }
}