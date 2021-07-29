package com.example.cursbnr.CursValutar.Activitati;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursValutar.Utile.RecyclerCurs_Adapter;
import com.example.cursbnr.CursValutar.Utile.UrlParser;
import com.example.cursbnr.GenerareRapoarte.Activitati.GenerareRapoarte;
import com.example.cursbnr.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CursValutar extends Activity {
    ArrayList<String> monede;
    ArrayList<String> valori;
    RecyclerCurs_Adapter adapter;
    RecyclerView recyclerView;
    int[] flags;
    TextView tv_cursvalutar, tv_datacurenta;
    BroadcastReceiver broadcastReceiver3;
    static final String url_bnr = "https://www.bnr.ro/nbrfxrates.xml";
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curs_valutar);
        InitComponents();

        monede = new ArrayList<>();
        valori = new ArrayList<>();

        initRV();
        registeredNetwork();
        fetchXML();
    }

    private void InitComponents() {
        tv_cursvalutar = findViewById(R.id.tv_cursvalutar);
        tv_datacurenta = findViewById(R.id.tv_datacurenta);
        recyclerView = findViewById(R.id.recyclerview_cursvalutar);
    }

    private void initRV() {
        try {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            flags = new int[32];
            adapter = new RecyclerCurs_Adapter(monede, valori, flags, this);
            recyclerView.setAdapter(adapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_recyclercursvalutar));
            recyclerView.addItemDecoration(dividerItemDecoration);
            broadcastReceiver3 = new CheckingConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getContentFromUrl() throws IOException {
        URL url = new URL(url_bnr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        if (inputStream != null) {
            inputStream.close();
        }
        return result.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fetchXML() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                XmlPullParserFactory xmlPullParserFactory;
                try {
                    InputStream is = new ByteArrayInputStream(getContentFromUrl().getBytes(StandardCharsets.UTF_8));
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlPullParserFactory.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    parsareXML(parser);
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private Thread worker;

    public void parsareXML(XmlPullParser parser) throws XmlPullParserException, IOException {
                Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int event = parser.getEventType();
                    final UrlParser[] linie_curenta = {null};
                    String getData = "";
                    int i = 0;
                    while (event != XmlPullParser.END_DOCUMENT) {
                        String nume_camp;
                        switch (event) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                nume_camp = parser.getName();
                                if (nume_camp.equals("Cube")) {
                                    getData = parser.getAttributeValue(null, "date");
                                    linie_curenta[0] = new UrlParser();
                                } else if (linie_curenta[0] != null) {
                                    if (nume_camp.equals("Rate")) {
                                        monede.add(parser.getAttributeValue(null, "currency"));
                                        valori.add(parser.nextText());
                                        SetFlags(i);
                                        i++;
                                    }
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                break;
                        }
                        event = parser.next();
                    }

                    String finalGetData = getData;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_datacurenta.setText(finalGetData);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    protected void registeredNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver3, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisteredNetwork() {
        try {
            unregisterReceiver(broadcastReceiver3);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisteredNetwork();
    }

    public void SetFlags(int i) {
        if ((monede.get(i)).equals("AED")) {
            flags[0] = R.drawable.aed;
        } else if ((monede.get(i)).equals("AUD")) {
            flags[1] = R.drawable.aud;
        } else if ((monede.get(i)).equals("BGN")) {
            flags[2] = R.drawable.bgn;
        } else if ((monede.get(i)).equals("BRL")) {
            flags[3] = R.drawable.brl;
        } else if ((monede.get(i)).equals("CAD")) {
            flags[4] = R.drawable.cad;
        } else if ((monede.get(i)).equals("CHF")) {
            flags[5] = R.drawable.chf;
        } else if ((monede.get(i)).equals("CNY")) {
            flags[6] = R.drawable.cny;
        } else if ((monede.get(i)).equals("CZK")) {
            flags[7] = R.drawable.czk;
        } else if ((monede.get(i)).equals("DKK")) {
            flags[8] = R.drawable.dkk;
        } else if ((monede.get(i)).equals("EGP")) {
            flags[9] = R.drawable.egp;
        } else if ((monede.get(i)).equals("EUR")) {
            flags[10] = R.drawable.eur;
        } else if ((monede.get(i)).equals("GBP")) {
            flags[11] = R.drawable.gbp;
        } else if ((monede.get(i)).equals("HRK")) {
            flags[12] = R.drawable.hrk;
        } else if ((monede.get(i)).equals("HUF")) {
            flags[13] = R.drawable.huf;
        } else if ((monede.get(i)).equals("INR")) {
            flags[14] = R.drawable.inr;
        } else if ((monede.get(i)).equals("JPY")) {
            flags[15] = R.drawable.jpy;
        } else if ((monede.get(i)).equals("KRW")) {
            flags[16] = R.drawable.krw;
        } else if ((monede.get(i)).equals("MDL")) {
            flags[17] = R.drawable.mdl;
        } else if ((monede.get(i)).equals("MXN")) {
            flags[18] = R.drawable.mxn;
        } else if ((monede.get(i)).equals("NOK")) {
            flags[19] = R.drawable.nok;
        } else if ((monede.get(i)).equals("NZD")) {
            flags[20] = R.drawable.nzd;
        } else if ((monede.get(i)).equals("PLN")) {
            flags[21] = R.drawable.pln;
        } else if ((monede.get(i)).equals("RSD")) {
            flags[22] = R.drawable.rsd;
        } else if ((monede.get(i)).equals("RUB")) {
            flags[23] = R.drawable.rub;
        } else if ((monede.get(i)).equals("SEK")) {
            flags[24] = R.drawable.sek;
        } else if ((monede.get(i)).equals("THB")) {
            flags[25] = R.drawable.thb;
        } else if ((monede.get(i)).equals("TRY")) {
            flags[26] = R.drawable.try1;
        } else if ((monede.get(i)).equals("UAH")) {
            flags[27] = R.drawable.uah;
        } else if ((monede.get(i)).equals("USD")) {
            flags[28] = R.drawable.usd;
        } else if ((monede.get(i)).equals("XAU")) {
            flags[29] = R.drawable.xau;
        } else if ((monede.get(i)).equals("XDR")) {
            flags[30] = R.drawable.xdr;
        } else if ((monede.get(i)).equals("ZAR")) {
            flags[31] = R.drawable.zar;
        }
    }
}