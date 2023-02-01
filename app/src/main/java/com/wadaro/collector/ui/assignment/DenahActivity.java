package com.wadaro.collector.ui.assignment;

import android.annotation.SuppressLint;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wadaro.collector.R;
import com.wadaro.collector.api.ImageDownloader;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.ActionButton;
import com.wadaro.collector.module.Utility;

public class DenahActivity extends MyActivity {

    private ActionButton bbtn_download_00;
    @Override
    protected int setLayout() {
        return R.layout.assignment_activity_denah;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initLayout() {
        TextView txvw_titile_00 = findViewById(R.id.txvw_titile_00);
        txvw_titile_00.setText("Denah Lokasi");

        bbtn_download_00 = findViewById(R.id.bbtn_download_00);
        bbtn_download_00.setText("Download");
        bbtn_download_00.setColor("0b599a");

    }

    @Override
    protected void initData() {
        ImageView imvw_denah_00 = findViewById(R.id.imvw_denah_00);
        Glide.with(mActivity).load(getIntent().getStringExtra("URL")).into(imvw_denah_00);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());

        bbtn_download_00.setOnclikLIstener(() -> {
            String name = "deanh_"+System.currentTimeMillis()+".jpeg";
            ImageDownloader downloader = new ImageDownloader(mActivity,getIntent().getStringExtra("URL"),"/Wadaro/com.wadaro.collector",name);
            downloader.execute();
            downloader.setOnDownloadListener(new ImageDownloader.OnDownloadListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onFinih(String path, String id) {
                    Utility.showToastSuccess(mActivity, "Berhasil di download "+path+"/"+id);
                }
            });
        });
    }
}
