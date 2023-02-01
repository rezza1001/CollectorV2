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

public class AssignmentDtlDB extends MasterDB {

    public static final String TAG          = "AssignmentDtlDB";
    public static final String TABLE_NAME   = "AssignmentDtlDB";

    public static final String BILLING_ID = "billing_id";
    public static final String CONSUMEN_ID = "consumen_id";
    public static final String DETAILS = "details";
    public static final String DATA         = "data";

    public String billingID = "";
    public String consumenID = "";
    public String details = "";
    public String data   = "";

    public String getCreateTable() {
        String create = "create table " + TABLE_NAME + " "
                + "(" +
                " " + BILLING_ID + " text NULL," +
                " " + CONSUMEN_ID + " text NULL," +
                " " + DETAILS + " text NULL," +
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
    protected AssignmentDtlDB build(Cursor res) {
        AssignmentDtlDB boking = new AssignmentDtlDB();
        boking.billingID = res.getString(res.getColumnIndex(BILLING_ID));
        boking.consumenID = res.getString(res.getColumnIndex(CONSUMEN_ID));
        boking.details    = res.getString(res.getColumnIndex(DETAILS));
        boking.data    = res.getString(res.getColumnIndex(DATA));
        return boking;
    }

    @Override
    protected void buildSingle(Cursor res) {
        this.billingID  = res.getString(res.getColumnIndex(BILLING_ID));
        this.consumenID = res.getString(res.getColumnIndex(CONSUMEN_ID));
        this.details    = res.getString(res.getColumnIndex(DETAILS));
        this.data       = res.getString(res.getColumnIndex(DATA));
        Log.d(TAG,"billingID : "+billingID);
    }

    public ContentValues createContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(BILLING_ID, billingID);
        contentValues.put(CONSUMEN_ID, consumenID);
        contentValues.put(DATA, data);
        contentValues.put(DETAILS, details);
        return contentValues;
    }

    @Override
    public boolean insert(Context context) {
        return super.insert(context);
    }

    @Override
    public void delete(Context context, String id) {
        super.delete(context, BILLING_ID +"="+id);
    }

    public void insertBulk(Context context, ArrayList<AssignmentDtlDB> data){
        DatabaseManager pDB = new DatabaseManager(context);
        String sql = "INSERT INTO "+ TABLE_NAME +" VALUES (?,?,?,?)";
        SQLiteStatement statement = pDB.getWritableDatabase().compileStatement(sql);
        pDB.getWritableDatabase().beginTransaction();
        for (AssignmentDtlDB item: data) {
            statement.clearBindings();
            statement.bindString(1, item.billingID);
            statement.bindString(2, item.consumenID);
            statement.bindString(3, item.details);
            statement.bindString(4, item.data);
            try {
                statement.execute();
            }catch (SQLiteConstraintException e){
                Log.e(TAG,"ERROR INSERT "+ item.billingID +" "+item.consumenID +" >> "+ Objects.requireNonNull(e.getMessage()));
            }
        }
        pDB.getWritableDatabase().setTransactionSuccessful();
        pDB.getWritableDatabase().endTransaction();
        pDB.close();
    }

    public ArrayList<AssignmentDtlDB> getData(Context context){
        ArrayList<AssignmentDtlDB> data = new ArrayList<>();

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
        String query = "select *  from " + TABLE_NAME +" WHERE "+ BILLING_ID +"='"+id+"'";
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
