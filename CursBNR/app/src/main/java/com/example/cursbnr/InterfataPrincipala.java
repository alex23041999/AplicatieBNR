package com.example.cursbnr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursBNR.HomeScreen;
import com.example.cursbnr.Inventar.Inventar;

public class InterfataPrincipala extends Activity {

    Button btnInventar, btnCursBNR, btn_ok;
    Dialog dialog;
    BroadcastReceiver broadcastReceiver;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfata_principala);


        btnCursBNR = findViewById(R.id.btn_cursBNR);
        btnInventar = findViewById(R.id.btn_inventar);
        broadcastReceiver = new CheckingConnection();
        AlertDialog.Builder builder = new AlertDialog.Builder(InterfataPrincipala.this);
        LayoutInflater layoutInflater = (LayoutInflater) InterfataPrincipala.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.storagepermission_alert, null);
        btn_ok = view.findViewById(R.id.btn_storagealert_ok);
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();

        if (ContextCompat.checkSelfPermission(InterfataPrincipala.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(InterfataPrincipala.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
        registeredNetwork();
        onBtnCursBNRClick();
        onBtnInventarClick();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dialog.dismiss();
            } else if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                new AlertDialog.Builder(InterfataPrincipala.this).setMessage("Trebuie sa permiteti stocarea pentru a folosi aplicatia!").setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // navigate to settings
                        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        settingIntent.setData(uri);
                        startActivity(settingIntent);
                    }
                }).setNegativeButton("Inchidere", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // leave
                        System.exit(0);
                    }
                }).show();
            }
        }
    }

    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(InterfataPrincipala.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(InterfataPrincipala.this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    private void onBtnCursBNRClick() {
        btnCursBNR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(InterfataPrincipala.this, HomeScreen.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onBtnInventarClick() {
        btnInventar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                    try {
                        Intent intent = new Intent(InterfataPrincipala.this, Inventar.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    new AlertDialog.Builder(InterfataPrincipala.this).setMessage("Trebuie sa permiteti stocarea pentru a folosi aplicatia!").setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // navigate to settings
                            Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            settingIntent.setData(uri);
                            startActivity(settingIntent);
                        }
                    }).setNegativeButton("Inchidere", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // leave
                            System.exit(0);
                        }
                    }).show();
                }
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