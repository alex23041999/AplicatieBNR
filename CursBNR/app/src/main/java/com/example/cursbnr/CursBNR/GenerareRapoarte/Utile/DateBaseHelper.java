package com.example.cursbnr.CursBNR.GenerareRapoarte.Utile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.example.cursbnr.Inventar.Utile.ObjectInventar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


//clasa ce defineste DataBase-ul folosit pentru aplicatie
public class DateBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CursBNRRapoarte.db";

    //primul tabel este destinat istoricului de rapoarte
    public static final String TABLE_NAME = "History_Raports";
    public static final String COL_Moneda = "Moneda";
    public static final String COL_DataStart = "DataStart";
    public static final String COL_DataSfarsit = "DataSfarsit";
    public static final String COL_TipRaport = "TipRaport";

    //al doilea tabel este destinat produselor din inventar
    public static final String TABLE_NAME1 = "Inventar";
    public static final String COL_ID = "Id";
    public static final String COL_Denumire = "Denumire";
    public static final String COL_Pret = "Pret";
    public static final String COL_CodBare = "CodBare";
    public static final String COL_Cantitate = "Cantitate";

    public DateBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" create table " + TABLE_NAME + "(Moneda TEXT, DataStart TEXT, DataSfarsit TEXT,TipRaport TEXT)");
        db.execSQL(" create table " + TABLE_NAME1 + "(Id INTEGER PRIMARY KEY AUTOINCREMENT, Denumire TEXT , Pret FLOAT , CodBare TEXT, Cantitate FLOAT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
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

    public boolean insertValuesInventar(ObjectInventar objectInventar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_Denumire, objectInventar.getDenumire());
        contentValues.put(COL_Pret, objectInventar.getPret());
        contentValues.put(COL_CodBare, objectInventar.getCodbare());
        contentValues.put(COL_Cantitate, objectInventar.getCantitate());
        long result = db.insert(TABLE_NAME1, null, contentValues);
        copyDbToExternal();

        return result != -1;
    }

    public boolean isEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean empty = false;
        Cursor cursor = db.rawQuery("SELECT * FROM Inventar", null);
        if (cursor.moveToFirst()) {
            empty = false;
        }else{
            empty = true;
        }
        return empty;
    }

    public ArrayList<ObjectInventar> getObjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ObjectInventar> objectInventars = new ArrayList<>();
        int i = 0;
        try (Cursor cursor = db.rawQuery("SELECT * FROM Inventar LIMIT 15 OFFSET(SELECT COUNT (*) FROM Inventar)-15;", null)) {
            while (cursor.moveToNext()) {
                objectInventars.add(i, new ObjectInventar(cursor.getString(1), cursor.getFloat(2), cursor.getString(3), cursor.getFloat(4)));
                i++;
            }
        }
        return objectInventars;
    }

    public ArrayList<String> getMonede() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT Moneda FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public ArrayList<String> getDataStart() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT DataStart FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public ArrayList<String> getDataSfarsit() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT DataSfarsit FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public ArrayList<String> getTip() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> mon = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT TipRaport FROM History_Raports LIMIT 10 OFFSET (SELECT COUNT(*) FROM History_Raports)-10;", null)) {
            while (cursor.moveToNext()) {
                mon.add(cursor.getString(0));
            }
            return mon;
        }
    }

    public void deleteDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        String delete = "DELETE FROM History_Raports WHERE ROWID IN (SELECT ROWID FROM History_Raports ORDER BY ROWID DESC LIMIT -1 OFFSET 10)";
        db.execSQL(delete);
        String del = "DELETE FROM Inventar WHERE ROWID IN (SELECT ROWID FROM Inventar ORDER BY ROWID DESC LIMIT -1 OFFSET 15)";
        db.execSQL(del);
    }

    public void deleteDatasInventar() {
        SQLiteDatabase db = this.getWritableDatabase();
        String del = "DELETE FROM Inventar WHERE ROWID IN (SELECT ROWID FROM Inventar ORDER BY ROWID DESC LIMIT -1 OFFSET 15)";
        db.execSQL(del);
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

    public boolean updateDataBaseInventar(String denumire, Float cantitateNoua) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_Cantitate, cantitateNoua);
        db.update(TABLE_NAME1, contentValues, "Denumire ='" + denumire + "'", null);
        return true;
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        String clear = "DELETE FROM " + TABLE_NAME1;
        db.execSQL(clearDBQuery);
        db.execSQL(clear);
    }

    // functie prin care se realizeaza o copie a bazei in telefon
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

}
