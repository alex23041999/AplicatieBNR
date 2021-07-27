package com.example.cursbnr.GenerareRapoarte.Utile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


//dupa fiecare modificare a elementelor bazei de date(coloane, nume tabel..), reinstalez aplicatia pe telefon
public class DateBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CursBNRRapoarte.db";
    public static final String TABLE_NAME = "History_Raports";
    public static final String COL_Moneda = "Moneda";
    public static final String COL_DataStart = "DataStart";
    public static final String COL_DataSfarsit = "DataSfarsit";
    public static final String COL_TipRaport = "TipRaport";

    public DateBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" create table " + TABLE_NAME + "(Moneda TEXT, DataStart TEXT, DataSfarsit TEXT,TipRaport TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertValues(String moneda, String datainceput, String datasfarsit, String tipraport) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_Moneda, moneda);
        contentValues.put(COL_DataStart, datainceput);
        contentValues.put(COL_DataSfarsit, datasfarsit);
        contentValues.put(COL_TipRaport, tipraport);
        long result = db.insert(TABLE_NAME, null, contentValues);
        copyDbToExternal();
        if (result == -1)
            return false;
        else
            return true;
    }

    public ArrayList<String> GetMonede() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT Moneda FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public ArrayList<String> GetDataStart() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT DataStart FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public ArrayList<String> GetDataSfarsit() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT DataSfarsit FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

//    public void DeleteDatas(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        try (Cursor cursor = db.rawQuery()){
//
//        }
//    }
    public ArrayList<String> GetTip() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT TipRaport FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public boolean updateDate(String moneda, String datainceput, String datasfarsit, String tipraport) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_Moneda, moneda);
        contentValues.put(COL_DataStart, datainceput);
        contentValues.put(COL_DataSfarsit, datasfarsit);
        contentValues.put(COL_TipRaport, tipraport);
        db.update(TABLE_NAME, contentValues, "MONEDA= ?", new String[]{moneda});
        return true;
    }

    public Integer deleteDate(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "DATA = ?", new String[]{data});
    }

    public void clearDatabase(String TABLE_NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        db.execSQL(clearDBQuery);
    }

    public static void copyDbToExternal() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (sd.canWrite()) {
                String currentDBPath = "/data/user/0/com.example.cursbnr/databases/CursBNRRapoarte.db";
                String backupDBPath = DATABASE_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getCurrencyForSpinner(String querry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> data = new ArrayList<>();
        Cursor res = db.rawQuery(querry, null);
        try {
            if (res.moveToFirst()) {
                do {
                    data.add(res.getString(0));
                } while (res.moveToNext());
            }
            res.close();
        } catch (Exception e) {
            String v = e.toString();
        }
        return data;
    }
}
