package com.example.cursbnr.CursBNR.IstoricRapoarte.Activitati;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Activitati.GenerareRapoarte;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.DateBaseHelper;
import com.example.cursbnr.CursBNR.IstoricRapoarte.Utile.RecyclerView_Istoric_Adapter;
import com.example.cursbnr.CursBNR.IstoricRapoarte.listener.OnRecyclerViewRowClick;
import com.example.cursbnr.R;

import java.util.ArrayList;
import java.util.List;

public class IstoricRapoarte extends Activity implements OnRecyclerViewRowClick {
    RecyclerView recyclerView_Istoric;
    RecyclerView_Istoric_Adapter adapter;
    List<String> moneda;
    List<String> tip_raport;
    List<String> datastart;
    List<String> datafinal;
    BroadcastReceiver broadcastReceiver2;
    DateBaseHelper dateBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_istoric_rapoarte);
        initComponents();
        registeredNetwork();

        moneda = new ArrayList<>();
        tip_raport = new ArrayList<>();
        datastart = new ArrayList<>();
        datafinal = new ArrayList<>();
        recyclerView_Istoric.setHasFixedSize(true);
        recyclerView_Istoric.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerView_Istoric_Adapter(moneda, datastart, datafinal, tip_raport, this, this);
        recyclerView_Istoric.setAdapter(adapter);

        dateBaseHelper = new DateBaseHelper(IstoricRapoarte.this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_recyclercursvalutar));
        recyclerView_Istoric.addItemDecoration(dividerItemDecoration);
        broadcastReceiver2 = new CheckingConnection();

        dateBaseHelper.deleteDatas();
        setMonede();
        setDateStart();
        setDateFinal();
        setTipuri();
        adapter.notifyDataSetChanged();
    }

    private void initComponents() {
        recyclerView_Istoric = findViewById(R.id.recycler_istoricrapoarte);
    }

    public void setMonede() {
        int i = 0;
        while (i < dateBaseHelper.getMonede().size()) {
            moneda.add(dateBaseHelper.getMonede().get(i));
            i++;
        }
    }

    public void setDateStart() {
        int i = 0;
        while (i < dateBaseHelper.getMonede().size()) {
            datastart.add(dateBaseHelper.getDataStart().get(i));
            i++;
        }
    }

    public void setDateFinal() {
        int i = 0;
        while (i < dateBaseHelper.getMonede().size()) {
            datafinal.add(dateBaseHelper.getDataSfarsit().get(i));
            i++;
        }
    }

    public void setTipuri() {
        int i = 0;
        while (i < dateBaseHelper.getMonede().size()) {
            tip_raport.add(dateBaseHelper.getTip().get(i));
            i++;
        }
    }

    protected void registeredNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver2, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisteredNetwork() {
        try {
            unregisterReceiver(broadcastReceiver2);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisteredNetwork();
    }

    //onClick -> functie prin care setam elementele ce vor urma sa fie trimise prin intent catre clasa GenerareRapoarte, la apasarea pe un rand din lista
    @Override
    public void onClick(int rowCount) {
        Intent intent = new Intent(this, GenerareRapoarte.class);
        intent.putExtra("moneda", moneda.get(rowCount));
        intent.putExtra("dataInceput", datastart.get(rowCount));
        intent.putExtra("dataSfarsit", datafinal.get(rowCount));
        intent.putExtra("tip", tip_raport.get(rowCount));

        startActivity(intent);
    }

}