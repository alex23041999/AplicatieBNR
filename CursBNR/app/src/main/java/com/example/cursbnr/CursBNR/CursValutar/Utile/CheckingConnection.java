package com.example.cursbnr.CursBNR.CursValutar.Utile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.cursbnr.R;

// clasa utilizata pentru verificarea conexiunii la internet
public class CheckingConnection extends BroadcastReceiver {
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (isConnectedOrNot(context)) {
        } else {
            showAlert();
        }
    }

    public boolean isConnectedOrNot(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.internet_alert, null);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        builder.setView(view);
        builder.setCancelable(false);

        final Dialog dialog = builder.create();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectedOrNot(mContext)) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
}
