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

public class SubmitDB extends MasterDB {

    public static final String TAG          = "SubmitDB";
    public static final String TABLE_NAME   = "SubmitDB";

    public static final String RECEIVE_DTL_ID = "receive_dtl_id";
    public static final String TYPE         = "typeSend";
    public static final String DATA         = "data";

    public String rcveDtlID = "";
    public String type = "";
    public String data   = "";

    public String getCreateTable() {
        String create = "create table " + TABLE_NAME + " "
                + "(" +
                " " + RECEIVE_DTL_ID + " text NULL," +
                " " + TYPE + " text NULL," +
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
    protected SubmitDB build(Cursor res) {
        SubmitDB boking = new SubmitDB();
        boking.rcveDtlID = res.getString(res.getColumnIndex(RECEIVE_DTL_ID));
        boking.type = res.getString(res.getColumnIndex(TYPE));
        boking.data    = res.getString(res.getColumnIndex(DATA));
        return boking;
    }

    @Override
    protected void buildSingle(Cursor res) {
        this.rcveDtlID = res.getString(res.getColumnIndex(RECEIVE_DTL_ID));
        this.type = res.getString(res.getColumnIndex(TYPE));
        this.data    = res.getString(res.getColumnIndex(DATA));
    }

    public ContentValues createContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(RECEIVE_DTL_ID, rcveDtlID);
        contentValues.put(TYPE, type);
        contentValues.put(DATA, data);
        return contentValues;
    }

    @Override
    public boolean insert(Context context) {
        return super.insert(context);
    }

    @Override
    public void delete(Context context, String id) {
        super.delete(context, RECEIVE_DTL_ID +"='"+id+"'");
    }

    public void insertBulk(Context context, ArrayList<SubmitDB> data){
        DatabaseManager pDB = new DatabaseManager(context);
        String sql = "INSERT INTO "+ TABLE_NAME +" VALUES (?,?,?)";
        SQLiteStatement statement = pDB.getWritableDatabase().compileStatement(sql);
        pDB.getWritableDatabase().beginTransaction();
        for (SubmitDB item: data) {
            statement.clearBindings();
            statement.bindString(1, item.rcveDtlID);
            statement.bindString(2, item.type);
            statement.bindString(3, item.data);
            try {
                statement.execute();
            }catch (SQLiteConstraintException e){
                Log.e(TAG,"ERROR INSERT "+ item.rcveDtlID +" "+item.type +" >> "+ Objects.requireNonNull(e.getMessage()));
            }
        }
        pDB.getWritableDatabase().setTransactionSuccessful();
        pDB.getWritableDatabase().endTransaction();
        pDB.close();
    }

    public ArrayList<SubmitDB> getData(Context context){
        ArrayList<SubmitDB> data = new ArrayList<>();

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
        String query = "select *  from " + TABLE_NAME +" WHERE "+ RECEIVE_DTL_ID +"='"+id+"'";
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
