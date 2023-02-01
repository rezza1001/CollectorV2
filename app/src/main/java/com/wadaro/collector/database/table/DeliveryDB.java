package com.wadaro.collector.database.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.wadaro.collector.database.DatabaseManager;
import com.wadaro.collector.database.MasterDB;

import java.util.ArrayList;
import java.util.Objects;

public class DeliveryDB extends MasterDB {

    public static final String TAG          = "DeliveryDB";
    public static final String TABLE_NAME   = "DeliveryDB";

    public static final String RECEIVE_ID   = "receive_id";
    public static final String SALES_ID     = "sales_id";
    public static final String DATA         = "data";

    public String receiveID = "";
    public String salesID = "";
    public String data   = "";

    public String getCreateTable() {
        String create = "create table " + TABLE_NAME + " "
                + "(" +
                " " + RECEIVE_ID + " text NULL," +
                " " + SALES_ID + " text NULL," +
                " " + DATA    + " text NULL" +
                "  )";
        Log.d(TAG,create);
        return create;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    protected DeliveryDB build(Cursor res) {
        DeliveryDB boking = new DeliveryDB();
        boking.receiveID = res.getString(res.getColumnIndex(RECEIVE_ID));
        boking.salesID = res.getString(res.getColumnIndex(SALES_ID));
        boking.data    = res.getString(res.getColumnIndex(DATA));
        return boking;
    }

    @Override
    protected void buildSingle(Cursor res) {
        this.receiveID = res.getString(res.getColumnIndex(RECEIVE_ID));
        this.salesID = res.getString(res.getColumnIndex(SALES_ID));
        this.data    = res.getString(res.getColumnIndex(DATA));
    }

    public ContentValues createContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(RECEIVE_ID, receiveID);
        contentValues.put(SALES_ID, salesID);
        contentValues.put(DATA, data);
        return contentValues;
    }

    @Override
    public boolean insert(Context context) {
        return super.insert(context);
    }

    @Override
    public void delete(Context context, String id) {
        super.delete(context, RECEIVE_ID +"='"+id+"'");
    }

    public void insertBulk(Context context, ArrayList<DeliveryDB> data){
        DatabaseManager pDB = new DatabaseManager(context);
        String sql = "INSERT INTO "+ TABLE_NAME +" VALUES (?,?,?)";
        SQLiteStatement statement = pDB.getWritableDatabase().compileStatement(sql);
        pDB.getWritableDatabase().beginTransaction();
        for (DeliveryDB item: data) {
            statement.clearBindings();
            statement.bindString(1, item.receiveID);
            statement.bindString(2, item.salesID);
            statement.bindString(3, item.data);
            try {
                statement.execute();
            }catch (SQLiteConstraintException e){
                Log.e(TAG,"ERROR INSERT "+ item.receiveID +" "+item.salesID +" >> "+ Objects.requireNonNull(e.getMessage()));
            }
        }
        pDB.getWritableDatabase().setTransactionSuccessful();
        pDB.getWritableDatabase().endTransaction();
        pDB.close();
    }

    public ArrayList<DeliveryDB> getData(Context context){
        ArrayList<DeliveryDB> data = new ArrayList<>();

        DatabaseManager pDB = new DatabaseManager(context);
        SQLiteDatabase db = pDB.getReadableDatabase();
        Cursor res = db.rawQuery("select *  from " + TABLE_NAME , null);
        try {
            while (res.moveToNext()){
                data.add(build(res));
            }
            pDB.close();
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        finally {
            res.close();
            pDB.close();
        }

        return  data;
    }

    public void getData(Context context, String id){
        DatabaseManager pDB = new DatabaseManager(context);
        SQLiteDatabase db = pDB.getReadableDatabase();
        String query = "select *  from " + TABLE_NAME +" WHERE "+ RECEIVE_ID +"='"+id+"'";
        Cursor res = db.rawQuery(query, null);
        Log.d(TAG,query);
        try {
            while (res.moveToNext()){
                buildSingle(res);
            }
            pDB.close();
        }catch (Exception e){
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        finally {
            res.close();
            pDB.close();
        }
    }

}
