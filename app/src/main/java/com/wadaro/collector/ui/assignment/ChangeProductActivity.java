package com.wadaro.collector.ui.assignment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wadaro.collector.R;
import com.wadaro.collector.api.ErrorCode;
import com.wadaro.collector.api.ObjectApi;
import com.wadaro.collector.api.PostManager;
import com.wadaro.collector.base.MyActivity;
import com.wadaro.collector.component.SelectActivity;
import com.wadaro.collector.component.SelectHolder;
import com.wadaro.collector.component.SelectView;
import com.wadaro.collector.module.Utility;
import com.wadaro.collector.ui.assignment.adapter.GoodsHolder;
import com.wadaro.collector.util.SpellingNumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChangeProductActivity extends MyActivity {
    private static final int REQ_GOODS = 3;
    private static final int REQ_QTY = 4;

    private TextView txvw_title_00;
    private SelectView slvw_goods_00,slvw_qty_00;
    private ArrayList<String> listProducts = new ArrayList<>();
    private HashMap<String, JSONObject> mapProduct = new HashMap<>();

    @Override
    protected int setLayout() {
        return R.layout.assignment_activity_addorder;
    }

    @Override
    protected void initLayout() {
        txvw_title_00 = findViewById(R.id.txvw_titile_00);
        slvw_goods_00 = findViewById(R.id.slvw_goods_00);
        slvw_qty_00 = findViewById(R.id.slvw_qty_00);

        slvw_goods_00.setHint("Nama Barang");
        slvw_qty_00.setHint("Quantity");

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initData() {
        txvw_title_00.setText("Edit Pesanan");
        GoodsHolder holder = new GoodsHolder();
        holder.unPack(getIntent().getStringExtra("DATA"));
        try {
            JSONObject dataJo = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("DATA")));
            mapProduct.put(holder.goods,dataJo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        slvw_goods_00.setValue(new SelectHolder(holder.goods, holder.goodsName));
        slvw_qty_00.setValue(new SelectHolder(holder.qty, holder.qty+" ( "+SpellingNumber.convert(Integer.parseInt(holder.qty),"ID")+" )"));

        String param = "?branch_id="+getIntent().getStringExtra("BRANCH");
        PostManager post = new PostManager(mActivity,"data-assignment/product/change"+param);
        post.execute("GET");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                listProducts.clear();
                try {
                    JSONArray products = obj.getJSONArray("data");
                    for (int i=0; i<products.length(); i++){
                        JSONObject prod = products.getJSONObject(i);
                        SelectHolder select = new SelectHolder();
                        select.id = prod.getString("product_id");
                        select.value = prod.getString("product_name");
                        listProducts.add(select.toString());
                        mapProduct.put(select.id,prod);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void initListener() {
        slvw_goods_00.setSelectedListener(data -> {
            Intent intent = new Intent(mActivity, SelectActivity.class);
            intent.putStringArrayListExtra("DATA",listProducts);
            startActivityForResult(intent, REQ_GOODS);
        });
        slvw_qty_00.setSelectedListener(data -> {
            Intent intent = new Intent(mActivity, SelectActivity.class);
            ArrayList<String> find = new ArrayList<>();
            for (int i=1; i<=2; i++){
                find.add(new SelectHolder(i+"",i+" ( "+SpellingNumber.convert(i,"ID")+" )").toString());
            }
            intent.putStringArrayListExtra("DATA",find);
            startActivityForResult(intent, REQ_QTY);
        });

        findViewById(R.id.bbtn_save_00).setOnClickListener(v -> {
            try {
                save();
            } catch (JSONException e) {
                Utility.showToastError(mActivity, e.getMessage());
                e.printStackTrace();
            }
        });
        findViewById(R.id.bbtn_cancel_00).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.imvw_back_00).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GOODS && resultCode == RESULT_OK){
            assert data != null;
            slvw_goods_00.setValue(new SelectHolder(data.getStringExtra("DATA")));
        }
        else if (requestCode == REQ_QTY && resultCode == RESULT_OK){
            assert data != null;
            slvw_qty_00.setValue(new SelectHolder(data.getStringExtra("DATA")));
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void save() throws JSONException {
        if (slvw_goods_00.getValue().isEmpty()){
            Utility.showToastError(mActivity,slvw_goods_00.getHint()+" harus diisi!");
            return;
        }
        if (slvw_qty_00.getValue().isEmpty()){
            Utility.showToastError(mActivity,slvw_qty_00.getHint()+" harus diisi!");
            return;
        }
        JSONObject product = mapProduct.get(slvw_goods_00.getKey());
        assert product != null;
        String tenor = product.getString("tenor");
        String price = product.getString("selling_price");
        String installment = product.getString("installment");

        PostManager post = new PostManager(mActivity,"data-assignment/product/save");
        post.addParam(new ObjectApi("sales_receive_detail_id",getIntent().getStringExtra("ID")));
        post.addParam(new ObjectApi("sales_receive_id",getIntent().getStringExtra("SALES_RECEIVE")));
        post.addParam(new ObjectApi("sales_id",getIntent().getStringExtra("SALES_ID")));
        post.addParam(new ObjectApi("qty", slvw_qty_00.getKey()));
        post.addParam(new ObjectApi("product_id",slvw_goods_00.getKey()));
        post.addParam(new ObjectApi("tenor", tenor));
        post.addParam(new ObjectApi("price", price));
        post.addParam(new ObjectApi("installment", installment));
        post.execute("POST");
        post.setOnReceiveListener((obj, code, message) -> {
            if (code == ErrorCode.OK){
                Utility.showToastSuccess(mActivity, message);
                setResult(RESULT_OK);
                mActivity.finish();
            }
            else {
                Utility.showToastError(mActivity, message);
            }
        });
    }


}
