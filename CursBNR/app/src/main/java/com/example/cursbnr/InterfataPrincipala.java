package com.example.cursbnr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cursbnr.CursBNR.HomeScreen;
import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.Inventar.Inventar;

public class InterfataPrincipala extends Activity {

    Button btnInventar,btnCursBNR;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfata_principala);

        btnCursBNR = findViewById(R.id.btn_cursBNR);
        btnInventar = findViewById(R.id.btn_inventar);
        broadcastReceiver = new CheckingConnection();

        registeredNetwork();
        OnBtnCursBNRClick();
        OnBtnInventarClick();
    }

    private void OnBtnCursBNRClick(){
        btnCursBNR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InterfataPrincipala.this, HomeScreen.class);
                startActivity(intent);
            }
        });
    }

    private void OnBtnInventarClick(){
        btnInventar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InterfataPrincipala.this, Inventar.class);
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