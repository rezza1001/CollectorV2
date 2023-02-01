package com.wadaro.collector.ui.offline;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.FormPost;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.database.table.AssignmentDB;
import com.wadaro.collector.database.table.AssignmentDtlDB;
import com.wadaro.collector.database.table.AssignmentFinishDB;
import com.wadaro.collector.database.table.ConsumenDtlDB;
import com.wadaro.collector.database.table.DeliveryDB;
import com.wadaro.collector.database.table.SaveAssignmentDB;
import com.wadaro.collector.database.table.SubmitDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DraftSurveyActivity extends MyActivity {

    private DraftAdapter mAdapter;
    private final ArrayList<Bundle> allData = new ArrayList<>();
    private UploadDialog loadingWindow;
    private final int [] size = new int[1];

    @Override
    protected int setLayout() {
        return R.layout.draft_activity_activitydraft;
    }

    @Override
    protected void initLayout() {

        RecyclerView rcvw_data_00 = findViewById(R.id.rcvw_data_00);
        rcvw_data_00.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new DraftAdapter(mActivity, allData);
        rcvw_data_00.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {
        allData.clear();
        SaveAssignmentDB submitDB = new SaveAssignmentDB();
        ArrayList<SaveAssignmentDB> dbs = submitDB.getData(mActivity);
        size[0] = dbs.size();

        for (SaveAssignmentDB mDb : dbs){
            try {
                JSONObject obj = new JSONObject(mDb.data);
                Bundle bundle = new Bundle();
                bundle.putString("billing_id",obj.getString("billing_id"));
                bundle.putString("payment_status",obj.getString("payment_status"));

                bundle.putString("consumen",mDb.otherData.split("::")[0]);
                bundle.putString("product",mDb.otherData.split("::")[1]);
                allData.add(bundle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {
        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.mrly_action_00).setOnClickListener(v -> {

            if (allData.size() > 0){
                loadingWindow = new UploadDialog(mActivity);
                loadingWindow.show();
                buildSender();
            }
        });
    }

    private void buildSender(){
        SaveAssignmentDB submitDB = new SaveAssignmentDB();
        ArrayList<SaveAssignmentDB> db = submitDB.getData(mActivity);

        int current = size[0] - db.size() ;
        loadingWindow.updateProgress(current, size[0]);

        if (db.size() > 0){
            SaveAssignmentDB sendDb = db.get(0);
            uploadData(sendDb);
            return;
        }
        loadingWindow.setCompleteStep(0);
        allData.clear();
        mAdapter.notifyDataSetChanged();
        buildDtlSave();
    }

    private void uploadData(SaveAssignmentDB sendDb){
        try {
            JSONObject objSend = new JSONObject(sendDb.data);
            FormPost post = new FormPost(mActivity,"billing-assignment/detail/consumen/save");
            objSend.put("total", Long.parseLong(objSend.getString("total")));
            objSend.put("payment_total", Long.parseLong(objSend.getString("payment_total")));
            Log.d(TAG,"sendDb.images "+sendDb.images);
            post.setData(objSend);
            post.setImages(new JSONObject(sendDb.images));
            post.showLoading(false);
            post.execute();
            post.setOnReceiveListener((obj, code, message) -> {
                if (code == ErrorCode.OK){
                    sendDb.delete(mActivity, sendDb.id);
                    buildSender();
                }
                else {
                    loadingWindow.setErrorStep(0);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildDtlSave(){
        AssignmentFinishDB finishDB = new AssignmentFinishDB();
        ArrayList<AssignmentFinishDB> finishDBS = finishDB.getData(mActivity);
        if (finishDBS.size() > 0){
            uploadDtlSave(finishDBS.get(0));
            return;
        }

        AssignmentDB db1 = new AssignmentDB();
        db1.clearData(mActivity);
        AssignmentDtlDB db2 = new AssignmentDtlDB();
        db2.clearData(mActivity);
        ConsumenDtlDB db3 = new ConsumenDtlDB();
        db3.clearData(mActivity);
        finishDB.clearData(mActivity);

        loadingWindow.setCompleteStep(1);

    }

    private void uploadDtlSave(AssignmentFinishDB finishDB){
        try {
            JSONObject objSend = new JSONObject(finishDB.data);
            PostManager post = new PostManager(mActivity,"billing-assignment/detail/consumen/finish");
            post.setData(objSend);
            post.showloading(false);
            post.execute("POST");
            post.setOnReceiveListener((obj, code, message) -> {
                if (code == ErrorCode.OK){
                    finishDB.delete(mActivity, finishDB.id);
                    buildDtlSave();
                }
                else {
                    loadingWindow.setErrorStep(1);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
