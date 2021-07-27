package com.example.cursbnr.GenerareRapoarte.Utile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.example.cursbnr.GenerareRapoarte.Activitati.GenerareRapoarte;
import com.example.cursbnr.HomeScreen;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

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