package com.wadaro.collector;

import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.wadaro.collector.api.Config;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.OnOneOffClickListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mochamad Rezza Gumilang on 01/May/2021.
 * Class Info :
 */

public class TestActivity extends MyActivity {
    @Override
    protected int setLayout() {
        return R.layout.testactivity;
    }

    @Override
    protected void initLayout() {
        findViewById(R.id.bbtn_save_00).setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                Calendar calendar = Calendar.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String,String> data = new HashMap<>();
                data.put("apiUrl", "Testing");
                db.collection("DBUG_"+calendar.get(Calendar.YEAR)+"_"+calendar.get(Calendar.MONTH))
                        .add(data)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e1 -> Log.w(TAG, "Error adding document", e1));
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }
}
