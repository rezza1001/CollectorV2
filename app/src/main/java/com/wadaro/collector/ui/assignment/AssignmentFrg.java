package com.wadaro.collector.ui.assignment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.wadaro.collector.R;
import com.wadaro.collector.base.MyFragment;
import com.wadaro.collector.ui.assignment.adapter.AssignmentPagerAdapter;
import com.wadaro.collector.ui.assignment.fragment.MapFragment;
import com.wadaro.collector.ui.assignment.fragment.TabelFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AssignmentFrg extends MyFragment {

    private TextView txvw_date_00;
    private DatePickerDialog datePickerDialog;
    private RelativeLayout rvly_search;
    private ImageView imvw_search;
    private EditText edtx_search;
    private boolean onSearch =false;

    DateFormat format1 = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));

    public static AssignmentFrg newInstance() {

        Bundle args = new Bundle();

        AssignmentFrg fragment = new AssignmentFrg();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setlayout() {
        return R.layout.ui_databooking_frg_main;
    }

    @Override
    protected void initLayout(View view) {

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.pager);
        txvw_date_00 = view.findViewById(R.id.txvw_date_00);
        rvly_search = view.findViewById(R.id.rvly_search);
        imvw_search = view.findViewById(R.id.imvw_search);
        edtx_search = view.findViewById(R.id.edtx_search);


        MapFragment mapFragment = new MapFragment();

        AssignmentPagerAdapter adapter = new AssignmentPagerAdapter(getChildFragmentManager());
        adapter.addFragment(TabelFragment.newInstance(), getString(R.string.tabel_fragment));
        adapter.addFragment(mapFragment, getString(R.string.map_fragment));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void initData() {
        txvw_date_00.setText(format1.format(new Date()));
        sendBroadCast();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initListener() {
        rvly_search.setOnClickListener(v -> {
            edtx_search.clearAnimation();
            if (!onSearch){
                onSearch = true;
                imvw_search.setImageResource(R.drawable.ic_clear);
                edtx_search.setVisibility(View.VISIBLE);
                edtx_search.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.zoom_in));
            }
            else {
                onSearch = false;
                imvw_search.setImageResource(R.drawable.places_ic_search);
                edtx_search.setVisibility(View.GONE);
                edtx_search.setText(null);
                senBroadcastSearch("");
            }

        });

        edtx_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                senBroadcastSearch(s.toString());
            }
        });
    }

    private void sendBroadCast(){
        Intent intent = new Intent("PARAMETER");
        intent.putExtra("DATE",txvw_date_00.getText().toString());
        new Handler().postDelayed(() -> mActivity.sendBroadcast(intent),100);
    }

    private void senBroadcastSearch(String text){
        Intent intent = new Intent("SEARCH");
        intent.putExtra("value",text);
        intent.putExtra("status",onSearch);
        mActivity.sendBroadcast(intent);
    }


}
