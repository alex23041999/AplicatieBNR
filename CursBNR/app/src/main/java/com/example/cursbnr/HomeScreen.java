package com.example.cursbnr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.example.cursbnr.CursValutar.Activitati.CursValutar;
import com.example.cursbnr.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.GenerareRapoarte.Activitati.GenerareRapoarte;
import com.example.cursbnr.IstoricRapoarte.Activitati.IstoricRapoarte;


public class HomeScreen extends Activity {
    AppCompatButton CursInventar, GenerareRapoarte, IstoricRapoarte;
    BroadcastReceiver broadcastReceiver;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitComponents();


        BtnCursInventarClick();
        BtnGenerareRapoarteClick();
        BtnIstoricRapoarteClick();
        registeredNetwork();
    }

    private void InitComponents() {
        CursInventar = findViewById(R.id.btn_cursvalutar);
        GenerareRapoarte = findViewById(R.id.btn_generarerapoarte);
        IstoricRapoarte = findViewById(R.id.btn_istoricrapoarte);
        broadcastReceiver = new CheckingConnection();
    }

    private void BtnCursInventarClick() {
        CursInventar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this, CursValutar.class);
                startActivity(intent);
            }
        });
    }

    private void BtnGenerareRapoarteClick() {
        GenerareRapoarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this, GenerareRapoarte.class);
                startActivity(intent);
            }
        });
    }

    private void BtnIstoricRapoarteClick() {
        IstoricRapoarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this, IstoricRapoarte.class);
                startActivity(intent);
            }
        });
    }

    protected void registeredNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisteredNetwork() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisteredNetwork();
    }

}