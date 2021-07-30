package com.example.cursbnr.CursBNR.GenerareRapoarte.Utile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;

import com.example.cursbnr.CursBNR.GenerareRapoarte.Activitati.GenerareRapoarte;

public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    ProgressDialog progressDialog;
    Activity activity;
    static final String url_10rapoarte = "https://www.bnr.ro/nbrfxrates10days.xml";

    public MyAsyncTask(GenerareRapoarte homeScreen) {
        this.activity = homeScreen;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Va rugam sa asteptati !");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //GenerareRapoarte.getValuesFromBNR();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        new Handler()
                .postDelayed(new Runnable() {
                                 @Override
                                 public void run() {

                                     progressDialog.hide();
                                 }
                             }, 5000
                );
    }
}