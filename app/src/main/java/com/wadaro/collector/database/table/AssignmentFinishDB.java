package com.wadaro.collector.database.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wadaro.collector.database.DatabaseManager;
import com.wadaro.collector.database.MasterDB;

import java.util.ArrayList;
import java.util.Objects;

public class AssignmentFinishDB extends MasterDB {

    public static final String TAG          = "AssignmentFinishDB";
    public static final String TABLE_NAME   = "AssignmentFinishDB";

    public static final String ID      = "id";
    public static final String DATA      = "data";

    public String id     = "";
    public String data   = "";

    public String getCreateTable() {
        String create = "create table " + TABLE_NAME + " "
                + "(" +
                " " + ID    + " text NULL," +
                " " + DATA    + " text NULL"+
                "  )";
        Log.d(TAG,create);
        return create;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    protected AssignmentFinishDB build(Cursor res) {
        AssignmentFinishDB boking = new AssignmentFinishDB();
        boking.id    = res.getString(res.getColumnIndex(ID));
        boking.data    = res.getString(res.getColumnIndex(DATA));
        return boking;
    }

    @Override
    protected void buildSingle(Cursor res) {
        this.id    = res.getString(res.getColumnIndex(ID));
        this.data    = res.getString(res.getColumnIndex(DATA));
    }

    public ContentValues createContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(DATA, data);
        return contentValues;
    }

    @Override
    public boolean insert(Context context) {
        delete(context,id);
        return super.insert(context);
    }

    @Override
    public void delete(Context context, String id) {
        super.delete(context,ID+"='"+id+"'");
    }

    public ArrayList<AssignmentFinishDB> getData(Context context){
        ArrayList<AssignmentFinishDB> data = new ArrayList<>();

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
        String query = "select *  from " + TABLE_NAME +" WHERE "+ID+"='"+id+"'";
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
