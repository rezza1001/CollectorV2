package com.wadaro.collector.ui.assignment.adapter;

import org.json.JSONException;
import org.json.JSONObject;

public class GoodsHolder {

    public String goods         = "";
    public String goodsName     = "";
    public String qty           = "";
    public int tenor            = 0;
    public long installment     = 0;
    public long selling_price     = 0;


    public JSONObject pack(){
        JSONObject data = new JSONObject();
        try {
            data.put("goods",goods);
            data.put("goodsName",goodsName);
            data.put("qty",qty);
            data.put("tenor",tenor);
            data.put("installment",installment);
            data.put("selling_price",selling_price);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void unPack(String pData){
        try {
            JSONObject data = new JSONObject(pData);
            goods           = data.getString("goods");
            goodsName       = data.getString("goodsName");
            qty             = data.getString("qty");
            tenor           = data.getInt("tenor");
            installment     = data.getLong("installment");
            selling_price   = data.getLong("selling_price");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
