package com.wadaro.collector.ui.offline;

import android.content.Context;

import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.component.WarningWindow;
import com.wadaro.collector.database.table.AssignmentDB;
import com.wadaro.collector.database.table.AssignmentDtlDB;
import com.wadaro.collector.database.table.ConsumenDtlDB;
import com.wadaro.collector.database.table.SaveAssignmentDB;
import com.wadaro.collector.module.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SynchData {

    private final Context mActivity;
    String today;
    private final DownloadDialog downloadStatus;

    public SynchData(Context context){
        mActivity = context;
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", new Locale("id"));
        today = format2.format(new Date());
        today = "2020-12-08";

        downloadStatus = new DownloadDialog(context);
        WarningWindow warningWindow = new WarningWindow(context);
        warningWindow.show("Perhatian","Pastikan anda sudah mengupload data sebelumnya, karena data draft akan di hapus dan pastikan anda memiliki jaringan yang bagus untuk download data");
        warningWindow.setOnSelectedListener(status -> {
            if (status == 2){
                AssignmentDB db1 = new AssignmentDB();
                db1.clearData(mActivity);
                AssignmentDtlDB db2 = new AssignmentDtlDB();
                db2.clearData(mActivity);
                ConsumenDtlDB db3 = new ConsumenDtlDB();
                db3.clearData(mActivity);
                SaveAssignmentDB db4 = new SaveAssignmentDB();
                db4.clearData(mActivity);

                downloadStatus.show();
                loadAssignment();
            }
        });
    }

    private void loadAssignment(){
        PostManager post = new PostManager(mActivity,"billing-assignment");
        post.showloading(false);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    ArrayList<AssignmentDB> assignmentDBS = new ArrayList<>();

                    JSONObject data = obj.getJSONObject("data");
                    JSONArray biling = data.getJSONArray("biling");
                    for (int i=0; i<biling.length(); i++) {
                        JSONObject objData = biling.getJSONObject(i);
                        AssignmentDB holder = new AssignmentDB();
                        holder.billingId = objData.getString("billing_id");
                        holder.salesId   = objData.getString("sales_receive_id");
                        holder.data = objData.toString();
                        assignmentDBS.add(holder);
                    }
                    AssignmentDB db = new AssignmentDB();
                    db.clearData(mActivity);
                    db.insertBulk(mActivity, assignmentDBS);
                    downloadStatus.setCompleteStep(0);
                    buildDetailAssignment();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                downloadStatus.setErrorStep(0);
            }
        });
    }

    private int maxData = 0;
    ArrayList<AssignmentDB> assignmentDBS = new ArrayList<>();
    private void buildDetailAssignment(){
        AssignmentDB db = new AssignmentDB();
        assignmentDBS = db.getData(mActivity);
        maxData = assignmentDBS.size();
        loadDetailAssignment();
    }

    private void loadDetailAssignment(){
        int current = maxData - assignmentDBS.size();
        downloadStatus.updateProgress(current, maxData);
        if (assignmentDBS.size() == 0){
            downloadStatus.setCompleteStep(1);
            buildDtlConsumen();
            return;
        }
        String param = "?billing_id="+assignmentDBS.get(0).billingId;
        PostManager post = new PostManager(mActivity,"billing-assignment/detail"+param);
        post.showloading(false);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                try {
                    JSONObject data = obj.getJSONObject("data");
                    JSONArray details = data.getJSONArray("details");

                    AssignmentDtlDB assignmentDB = new AssignmentDtlDB();
                    assignmentDB.billingID  = data.getString("billing_id");
                    assignmentDB.data       = data.toString();
                    assignmentDB.details    = details.toString();
                    assignmentDB.insert(mActivity);
                    assignmentDBS.remove(0);
                    loadDetailAssignment();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                downloadStatus.setErrorStep(1);
            }
        });
    }

    ArrayList<AssignmentDtlDB> dtlDBS = new ArrayList<>();
    private void buildDtlConsumen(){
        AssignmentDtlDB db = new AssignmentDtlDB();
        dtlDBS = db.getData(mActivity);
        requestConsument();
    }

    ArrayList<JSONObject> orderDb = new ArrayList<>();
    private void requestConsument(){
        if (dtlDBS.size() == 0){
            downloadStatus.setCompleteStep(2);
            Utility.showToastSuccess(mActivity,"Load data berhasil");
            return;
        }

        try {
            JSONArray ja = new JSONArray(dtlDBS.get(0).details);

            for (int i=0; i<ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                orderDb.add(jo);
            }
            maxData = orderDb.size();
            load(dtlDBS.get(0).billingID);
            dtlDBS.remove(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void load(String billingID){
        int current = maxData - orderDb.size();
        downloadStatus.updateProgress(current, maxData);
        if (orderDb.size() == 0){
            requestConsument();
            return;
        }
        JSONObject jo = orderDb.get(0);
        String consumentId = "";
        String productId = "";
        try {
            consumentId = jo.getString("consumen_id");
            productId   = jo.getString("product_id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String finalProductId = productId;
        String finalConsumentId = consumentId;

        PostManager post = new PostManager(mActivity,"billing-assignment/detail/consumen");
        post.addParam(new ObjectApi("billing_id",billingID));
        post.addParam(new ObjectApi("consumen_id",consumentId));
        post.addParam(new ObjectApi("product_id",productId));
        post.showloading(false);
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                ConsumenDtlDB  dtlDB = new ConsumenDtlDB();
                dtlDB.productID = finalProductId;
                dtlDB.consumenID = finalConsumentId;
                dtlDB.billingID = billingID;
                dtlDB.data = obj.toString();
                dtlDB.insert(mActivity);

                orderDb.remove(0);
                load(billingID);
            }
            else {
                downloadStatus.setErrorStep(2);
            }
        });
    }


}
