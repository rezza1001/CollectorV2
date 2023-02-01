package com.wadaro.collector.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wadaro.collector.R;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.module.FileProcessing;
import com.wadaro.collector.ui.assignment.AssignmentFrg;
import com.wadaro.collector.ui.billing.BillingFragment;
import com.wadaro.collector.ui.overcredit.OverCreditActivity;
import com.wadaro.collector.ui.profile.ProfileFrg;
import com.wadaro.collector.ui.recap.RecapFragment;

public class HomePageActivity extends MyActivity {

    private FrameLayout frame_body_00;
    private BottomNavigationView navigationView;
    private MenuItem menuItem;

    @Override
    protected int setLayout() {
        return R.layout.ui_home_activity_main;
    }

    @Override
    protected void initLayout() {
        frame_body_00   = findViewById(R.id.frame_body_00);
        navigationView  = findViewById(R.id.bottom_nav_bar);
        navigationView.inflateMenu(R.menu.menu_demobooker);
        setCheckedBottomMenu(R.id.bottom_home);
        selectedMenu(0);
    }

    void setCheckedBottomMenu(int rIdmenu){
        Menu menu = navigationView.getMenu();
        menuItem = menu.findItem(rIdmenu);
        if(menuItem!=null) menuItem.setChecked(true);

    }

    @Override
    protected void initData() {
        FileProcessing.createFolder(mActivity,"Wadaro");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void initListener() {
        navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.bottom_home:
                    selectedMenu(0);
                    return true;
                case R.id.bottom_booking:
                    selectedMenu(1);
                    return true;
                case R.id.bottom_data:
                    selectedMenu(2);
                    return true;
                case R.id.bottom_penugasan:
                    selectedMenu(3);
                    return true;
                case R.id.bottom_profile:
                    selectedMenu(4);
                    return true;
            }
            return false;
        });
    }

    private void selectedMenu(int menu){
        Fragment fragment ;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (menu){
            default:
                fragment = HomeFrg.newInstance();
                fragmentTransaction.replace(frame_body_00.getId(), fragment,"main");
                fragmentTransaction.detach(fragment);
                fragmentTransaction.attach(fragment);
                fragmentTransaction.commit();
                break;
            case 1:
                fragment = AssignmentFrg.newInstance();
                fragmentTransaction.replace(frame_body_00.getId(), fragment,"main");
                fragmentTransaction.detach(fragment);
                fragmentTransaction.attach(fragment);
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case 2:
                fragment = BillingFragment.newInstance();
                fragmentTransaction.replace(frame_body_00.getId(), fragment,"main");
                fragmentTransaction.detach(fragment);
                fragmentTransaction.attach(fragment);
                fragmentTransaction.commit();
                break;
            case 3:
                fragment = RecapFragment.newInstance();
                fragmentTransaction.replace(frame_body_00.getId(), fragment,"main");
                fragmentTransaction.detach(fragment);
                fragmentTransaction.attach(fragment);
                fragmentTransaction.commit();
                break;
            case 4:
                fragment = ProfileFrg.newInstance();
                fragmentTransaction.replace(frame_body_00.getId(), fragment,"main");
                fragmentTransaction.detach(fragment);
                fragmentTransaction.attach(fragment);
                fragmentTransaction.commit();
                break;
            case 5:
                startActivity(new Intent(mActivity, OverCreditActivity.class));
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OPEN_ASSIGNMENT");
        intentFilter.addAction("DELIVERY");
        intentFilter.addAction("REJECT");
        intentFilter.addAction("OPEN_PROFILE");
        intentFilter.addAction("OVER_CREDIT");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("OPEN_ASSIGNMENT")){
                setCheckedBottomMenu(R.id.bottom_booking);
                selectedMenu(1);
            }
            else if (intent.getAction().equals("DELIVERY")){
                setCheckedBottomMenu(R.id.bottom_data);
                selectedMenu(2);
            }
            else if (intent.getAction().equals("REJECT")){
                setCheckedBottomMenu(R.id.bottom_data);
                selectedMenu(3);
            }
            else if (intent.getAction().equals("OPEN_PROFILE")){
                setCheckedBottomMenu(R.id.bottom_profile);
                selectedMenu(4);
            }
            else if (intent.getAction().equals("OVER_CREDIT")){
//                setCheckedBottomMenu(R.id.bottom_profile);
                selectedMenu(5);
            }
        }
    };
}
