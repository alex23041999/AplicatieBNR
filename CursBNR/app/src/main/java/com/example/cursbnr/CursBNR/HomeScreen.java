package com.example.cursbnr.CursBNR;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.example.cursbnr.CursBNR.AnimationUtils.CircleImageView;
import com.example.cursbnr.CursBNR.AnimationUtils.CircleLayout;
import com.example.cursbnr.CursBNR.CursValutar.Activitati.CursValutar;
import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Activitati.GenerareRapoarte;
import com.example.cursbnr.CursBNR.IstoricRapoarte.Activitati.IstoricRapoarte;
import com.example.cursbnr.R;

public class HomeScreen extends Activity implements CircleLayout.OnItemSelectedListener,
        CircleLayout.OnItemClickListener, CircleLayout.OnRotationFinishedListener, CircleLayout.OnCenterClickListener {
    BroadcastReceiver broadcastReceiver;
    protected CircleLayout circleLayout;
    protected TextView selectedTextView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitComponents();
        context = HomeScreen.this;

        registeredNetwork();

        // Set listeners
        circleLayout.setOnItemSelectedListener(this);
        circleLayout.setOnItemClickListener(this);
        circleLayout.setOnRotationFinishedListener(this);
        circleLayout.setOnCenterClickListener(this);
        String name = null;
        View view = circleLayout.getSelectedItem();
        if (view instanceof CircleImageView) {
            name = ((CircleImageView) view).getName();
        }
        selectedTextView.setText(name);
    }

    @Override
    public void onItemSelected(View view) {
        final String name;
        if (view instanceof CircleImageView) {
            name = ((CircleImageView) view).getName();
        } else {
            name = null;
        }
        selectedTextView.setText(name);

        switch (view.getId()) {
            case R.id.cursvalutar:
                // Handle calendar selection
                break;
            case R.id.generareRapoarte:
                // Handle cloud selection
                break;
            case R.id.istoric:
                // Handle mail selection
                break;
        }
    }

    @Override
    public void onItemClick(View view) {
        String name = null;
        if (view instanceof CircleImageView) {
            name = ((CircleImageView) view).getName();
        }
        selectedTextView.setText(name);
        switch (view.getId()) {
            case R.id.cursvalutar:
                Intent intentCursValutar = new Intent(HomeScreen.this, CursValutar.class);
                startActivity(intentCursValutar);
                break;
            case R.id.generareRapoarte:
                Intent intentGenerareRapoarte = new Intent(HomeScreen.this, GenerareRapoarte.class);
                startActivity(intentGenerareRapoarte);
                break;
            case R.id.istoric:
                Intent intentIstoricRapoarte = new Intent(HomeScreen.this, IstoricRapoarte.class);
                startActivity(intentIstoricRapoarte);
                break;

        }
    }

    @Override
    public void onRotationFinished(View view) {
        Animation animation = new RotateAnimation(0, 360, view.getWidth() / 2, view.getHeight() / 2);
        animation.setDuration(250);
        view.startAnimation(animation);
    }

    @Override
    public void onCenterClick() {
        //Toast.makeText(getApplicationContext(), R.string.center_click, Toast.LENGTH_SHORT).show();
    }

    private void InitComponents() {
        circleLayout = (CircleLayout) findViewById(R.id.circle_layout);
        selectedTextView = (TextView) findViewById(R.id.selected_textView);
        broadcastReceiver = new CheckingConnection();
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