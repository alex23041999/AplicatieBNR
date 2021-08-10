package com.example.cursbnr.CursBNR.GenerareRapoarte.Activitati;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursBNR.CursValutar.Utile.UrlParser;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.DateBaseHelper;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.MonedaValoare;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.RecyclerView_TipLista_Adapter;
import com.example.cursbnr.CursBNR.HomeScreen;
import com.example.cursbnr.CursBNR.IstoricRapoarte.Activitati.IstoricRapoarte;
import com.example.cursbnr.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class GenerareRapoarte extends AppCompatActivity {
    private CompositeDisposable compositeDisposable;
    TextView tv_selectaremoneda;
    Spinner sp_selectaremoneda;
    DatePickerDialog datePickerDialog;
    EditText et_datastart, et_datafinal;
    LineChart linechart;
    ToggleButton btn_grafic, btn_lista;
    Button btn_salvare;
    RecyclerView recyclerView_tipLista;
    RecyclerView_TipLista_Adapter adapter;
    ArrayList<String> monede;
    ArrayList<String> valori;
    ArrayList<String> zile;
    ArrayList<String> values; //valori spinner
    ArrayList<String> intervale;
    ArrayList<Float> min;
    ArrayList<Float> max;
    LinearLayout linearLayout;
    BroadcastReceiver broadcastReceiver1;
    static final String url_bnr = "https://www.bnr.ro/nbrfxrates.xml";
    static final String url_2021 = "https://www.bnr.ro/files/xml/years/nbrfxrates2021.xml";
    ProgressDialog progressDialog;
    DateBaseHelper dateBaseHelper;
    Map<String, Float> mapmin;
    Map<String, Float> mapmax;
    Intent intent;
    boolean typeGrafic = false;
    boolean typeLista = false;
    boolean typeExist = false;
    int i = 0;
    IstoricRapoarte istoricRapoarte;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generare_rapoarte);

        InitComponents();

        valori = new ArrayList<>();
        zile = new ArrayList<>();
        monede = new ArrayList<>();
        intervale = new ArrayList<>();
        min = new ArrayList<>();
        max = new ArrayList<>();
        values = new ArrayList<>();
        broadcastReceiver1 = new CheckingConnection();
        dateBaseHelper = new DateBaseHelper(GenerareRapoarte.this);

        initRV();
        EtStartDateClick();
        EtEndDateClick();
        OnToggleBtnGraficClick();
        OnToggleBtnListaClick();
        OnClickButtonSalvare();
        registeredNetwork();
        SpinnerToast();
        populateSpinnerAdapter();
    }

    private void InitComponents() {
        sp_selectaremoneda = findViewById(R.id.spinner_selectaremoneda);
        tv_selectaremoneda = findViewById(R.id.tv_selectmoneda);
        et_datastart = findViewById(R.id.et_datainceput);
        et_datafinal = findViewById(R.id.et_datasfarsit);
        btn_grafic = findViewById(R.id.btn_grafic);
        btn_lista = findViewById(R.id.btn_lista);
        linechart = findViewById(R.id.lc_grafic);
        recyclerView_tipLista = findViewById(R.id.recycler_generarerapoarte);
        linearLayout = findViewById(R.id.linearlayout_tiplista);
        btn_salvare = findViewById(R.id.btn_salvare);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onStart() {
        super.onStart();
        compositeDisposable = new CompositeDisposable();
        ProgressDialog progressDialog = new ProgressDialog(this,R.style.ProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Procesare date");
        String mesagge = new String("Datele se încarcă !");
        progressDialog.setMessage(mesagge);
        progressDialog.show();
        compositeDisposable.add(Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            istoricRapoarteIntent();
                            progressDialog.hide();
                        }
                ));
    }

    private void istoricRapoarteIntent() {
        intent = getIntent();

        String tip = "";
        if (intent.hasExtra("tip")) {
            tip = intent.getStringExtra("tip");
        }

        String moneda = "";
        if (intent.hasExtra("moneda")) {
            moneda = intent.getStringExtra("moneda");
            sp_selectaremoneda.setSelection(values.indexOf(moneda));
        }

        String dataInceput = "";
        if (intent.hasExtra("dataInceput")) {
            dataInceput = intent.getStringExtra("dataInceput");
            et_datastart.setText(dataInceput);
        }

        String dataSfarsit = "";
        if (intent.hasExtra("dataSfarsit")) {
            dataSfarsit = intent.getStringExtra("dataSfarsit");
            et_datafinal.setText(dataSfarsit);
        }

        if (!tip.isEmpty()) {
            if (tip.equals("Tip grafic")) {
                getValuesFromBNRForGrafic(moneda, dataInceput, dataSfarsit);
                typeExist = true;
            } else if (tip.equals("Tip lista")) {
                getValuesFromBNRForLista(moneda, dataInceput, dataSfarsit);
                typeExist = true;
            }
        }
    }

    private void initRV() {
        try {
            recyclerView_tipLista.setHasFixedSize(true);
            recyclerView_tipLista.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerView_TipLista_Adapter(monede, intervale, min, max, this);
            recyclerView_tipLista.setAdapter(adapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL); // linie delimitare randuri
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_recyclerview));
            recyclerView_tipLista.addItemDecoration(dividerItemDecoration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetIntervale(String di, String ds) {
        for (int i = 0; i < monede.size(); i++) {
            intervale.add(di + "\n" + ds);
        }
    }

    private void SpinnerToast() {
        sp_selectaremoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                if (sp_selectaremoneda.getSelectedItem().toString().equals("Selectare moneda")) {

                } else {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


    }

    private void ClearGrafic() {
        zile.clear();
        valori.clear();
    }

    private void ClearLista() {
        monede.clear();
        intervale.clear();
        min.clear();
        max.clear();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void populateSpinnerAdapter() {
        values.add("Selectare moneda");
        values.add("Toate monedele");
        fetchXML();
        ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(this, R.layout.spinner_style, values);
        adapter_spinner.setDropDownViewResource(R.layout.dropdown_itembackground);
        sp_selectaremoneda.setAdapter(adapter_spinner);
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

    public void parsareXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        final UrlParser[] linie_curenta = {null};
        while (event != XmlPullParser.END_DOCUMENT) {
            String nume_camp;
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    nume_camp = parser.getName();
                    if (nume_camp.equals("Cube")) {
                        linie_curenta[0] = new UrlParser();
                    } else if (linie_curenta[0] != null) {
                        if (nume_camp.equals("Rate")) {
                            values.add(parser.getAttributeValue(null, "currency"));
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            event = parser.next();
        }
    }

    private void Calendar(EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(GenerareRapoarte.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String str = year + "-" + (month + 1) + "-" + dayOfMonth;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date date = simpleDateFormat.parse(str);
                            Calendar c = Calendar.getInstance();
                            c.setTime(date);
                            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                editText.getText().clear();
                                Toast.makeText(GenerareRapoarte.this, "Nu exista actualizari ale cursului in weekend!", Toast.LENGTH_SHORT).show();
                            } else {
                                editText.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                }, year, month, day);

                Calendar min = Calendar.getInstance();
                min.set(Calendar.DAY_OF_YEAR, 1);
                datePickerDialog.getDatePicker().setMinDate(min.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

    }

    private void EtStartDateClick() {
        et_datastart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar(et_datastart);
            }
        });
    }

    private void EtEndDateClick() {
        et_datafinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar(et_datafinal);
            }
        });
    }

    private void setLinechartValues() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < valori.size(); i++) {
            entries.add(new Entry(i, Float.parseFloat(valori.get(i))));
        }

        LineDataSet linedataset = new LineDataSet(entries, "Valori moneda");
        linedataset.setColor(Color.parseColor("#a2dae2"));
        linedataset.setLineWidth(2);
        linedataset.setValueTextSize(10);
        linedataset.setValueTextColor(Color.parseColor("#ff1d0b"));

        LineData lineData = new LineData(linedataset);
        linechart.setData(lineData);
        linechart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(zile));
        linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        linechart.getXAxis().setTextColor(Color.parseColor("#a2dae2"));
        linechart.getXAxis().setTextSize(5);

        linechart.invalidate();
        linechart.setVisibleXRangeMaximum(5);
        linechart.setVisibleXRangeMinimum(2);
        linechart.moveViewToX(1);
    }

    private Integer CompareDates(EditText start, EditText sfarsit) {
        Date begin = null;
        Date end = null;
        int i = 0;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            begin = dateFormat.parse(start.getText().toString());
            end = dateFormat.parse(sfarsit.getText().toString());
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(begin);
            Calendar calendarFinal = Calendar.getInstance();
            calendarFinal.setTime(end);
            if (calendarFinal.before(calendarStart)) {
                start.getText().clear();
                sfarsit.getText().clear();
                i++;
            } else {
                i--;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return i;
    }

    private void OnToggleBtnGraficClick() {
        btn_grafic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_lista.setChecked(false);
                    if (et_datastart.getText().toString().trim().equals("") || et_datafinal.getText().toString().trim().equals("")) {
                        Toast.makeText(GenerareRapoarte.this, "Completati toate campurile!", Toast.LENGTH_SHORT).show();
                        linechart.setVisibility(View.INVISIBLE);
                    } else if (CompareDates(et_datastart, et_datafinal) > 0) {
                        linechart.setVisibility(View.INVISIBLE);
                        Toast.makeText(GenerareRapoarte.this, "Selectati o ordine cronologica a datelor!", Toast.LENGTH_SHORT).show();
                    } else if (sp_selectaremoneda.getSelectedItem().toString().equals("Toate monedele") || sp_selectaremoneda.getSelectedItem().toString().equals("Selectare moneda")) {
                        Toast.makeText(GenerareRapoarte.this, "Raportul de tip grafic poate fi afisat doar pentru o moneda!", Toast.LENGTH_SHORT).show();
                    } else {
                        ClearGrafic();
                        typeGrafic = true;
                        typeLista = false;
                        getValuesFromBNRForGrafic(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString());
                    }
                } else {
                }
            }
        });
    }

    private void OnToggleBtnListaClick() {
        btn_lista.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_grafic.setChecked(false);
                    if (et_datastart.getText().toString().trim().equals("") || et_datafinal.getText().toString().trim().equals("")) {
                        Toast.makeText(GenerareRapoarte.this, "Completati toate campurile!", Toast.LENGTH_SHORT).show();
                        linearLayout.setVisibility(View.INVISIBLE);
                    } else if (CompareDates(et_datastart, et_datafinal) > 0) {
                        linechart.setVisibility(View.INVISIBLE);
                        Toast.makeText(GenerareRapoarte.this, "Selectati o ordine cronologica a datelor!", Toast.LENGTH_SHORT).show();
                    } else if (!sp_selectaremoneda.getSelectedItem().toString().equals("Toate monedele")) {
                        Toast.makeText(GenerareRapoarte.this, "Raportul de tip lista poate fi afisat doar pentru toate monedele!", Toast.LENGTH_SHORT).show();
                    } else {
                        ClearLista();
                        typeLista = true;
                        typeGrafic = false;
                        getValuesFromBNRForLista(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString());
                    }
                } else {
                    //
                }
            }
        });
    }

    private void OnClickButtonSalvare() {
        btn_salvare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeGrafic == true) {
                    Boolean result;
                    result = dateBaseHelper.insertValues(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString(), "Tip grafic");
                    if (result) {
                        Toast.makeText(GenerareRapoarte.this, "Tipul de raport a fost salvat cu succes !", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GenerareRapoarte.this, "Esec la salvare !", Toast.LENGTH_SHORT).show();
                    }
                } else if (typeLista == true) {
                    Boolean result;
                    result = dateBaseHelper.insertValues("Toate monedele", et_datastart.getText().toString(), et_datafinal.getText().toString(), "Tip lista");
                    if (result) {
                        Toast.makeText(GenerareRapoarte.this, "Tipul de raport a fost salvat cu succes !", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GenerareRapoarte.this, "Esec la salvare !", Toast.LENGTH_SHORT).show();
                    }
                } else if (typeExist == true && typeLista == false && typeGrafic == false) {
                    Toast.makeText(GenerareRapoarte.this, "Raportul există deja în istoric.", Toast.LENGTH_SHORT).show();
                } else if (typeLista == false && typeGrafic == false) {
                    Toast.makeText(GenerareRapoarte.this, "Selectati o varianta de salvare a raportului !", Toast.LENGTH_SHORT).show();
                } else if (typeExist == true && typeLista == false && typeGrafic == false) {
                    Toast.makeText(GenerareRapoarte.this, "Raportul există deja în istoric.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
//        dateBaseHelper.DeleteDatas();
//        dateBaseHelper.GetMonede();
//        dateBaseHelper.GetDataStart();
//        dateBaseHelper.GetDataSfarsit();
//        dateBaseHelper.GetTip();
//        istoricRapoarte.SetMonede();
//        istoricRapoarte.SetDateStart();
//        istoricRapoarte.SetDateFinal();
//        istoricRapoarte.SetTipuri();
        Intent intent = new Intent(GenerareRapoarte.this, HomeScreen.class);
        startActivity(intent);
        adapter.notifyDataSetChanged();
        super.onBackPressed();
        finish();
    }

    public String GetContentFromUrl() throws IOException {
        URL url = new URL(url_2021);
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

    private Thread worker;

    public void ParsareRaportXMLForGrafic(XmlPullParser parser, String m, String di, String ds) throws XmlPullParserException, IOException {
        final int[] event = {parser.getEventType()};
        final String[] date = {new String()};
        MonedaValoare monedaValoare = new MonedaValoare();
        final MonedaValoare[] linie_curenta = {null};

        worker = new Thread(new Runnable() {
            private void updateUI(final boolean download) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // verificare status gasire date
                        if (download) {
                            progressDialog.hide();
                            setLinechartValues();
                            linechart.setVisibility(View.VISIBLE);
                            linearLayout.setVisibility(View.INVISIBLE);
                            btn_lista.setTextColor(Color.parseColor("#ff0000"));
                            btn_grafic.setTextColor(Color.parseColor("#7fff00"));
                            ClearLista();
                        } else {
                            linechart.setVisibility(View.INVISIBLE);
                            Toast.makeText(GenerareRapoarte.this, "Nu s-au gasit date!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });
            }

            public boolean download() {
                boolean isDownload = false;
                while (event[0] != XmlPullParser.END_DOCUMENT) {
                    String numecamp;
                    int i = 0;
                    switch (event[0]) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            numecamp = parser.getName();
                            if (numecamp.equals("Cube")) {
                                date[0] = null;
                                date[0] = parser.getAttributeValue(null, "date");
                                Date begin = null;
                                Date end = null;
                                Date data = null;
                                try {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    begin = dateFormat.parse(di);
                                    end = dateFormat.parse(ds);
                                    data = dateFormat.parse(date[0]);
                                    Calendar calendarStart = Calendar.getInstance();
                                    calendarStart.setTime(begin);
                                    Calendar calendarFinal = Calendar.getInstance();
                                    calendarFinal.setTime(end);
                                    Calendar calendarData = Calendar.getInstance();
                                    calendarData.setTime(data);
                                    if (calendarData.before(calendarStart)) {

                                    } else {
                                        linie_curenta[0] = new MonedaValoare();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (linie_curenta[0] != null) {
                                while (true) {
                                    try {
                                        if (!(parser.getEventType() != XmlPullParser.END_TAG))
                                            break;
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    }
                                    String name = parser.getName();
                                    if (name.equals("Rate")) {
                                        Date begin = null;
                                        Date end = null;
                                        Date data = null;
                                        try {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                            begin = dateFormat.parse(di);
                                            end = dateFormat.parse(ds);
                                            data = dateFormat.parse(date[0]);
                                            Calendar calendarStart = Calendar.getInstance();
                                            calendarStart.setTime(begin);
                                            Calendar calendarFinal = Calendar.getInstance();
                                            calendarFinal.setTime(end);
                                            Calendar calendarData = Calendar.getInstance();
                                            calendarData.setTime(data);
                                            if (((calendarData.after(calendarStart) && calendarData.before(calendarFinal))
                                                    || calendarData.equals(calendarStart)
                                                    || calendarData.equals(calendarFinal))) {

                                                if (m.equals(parser.getAttributeValue(null, "currency"))) {
                                                    valori.add(parser.nextText());
                                                    zile.add(date[0]);
                                                    isDownload = true;
                                                } else {
                                                    parser.nextText();
                                                }
                                            }

                                            //daca data din xml > data sfarsit, oprim parsarea
                                            if (calendarData.after(calendarFinal)) {
                                                return isDownload;

                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        } catch (XmlPullParserException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            try {
                                parser.next();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    try {
                        event[0] = parser.next();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                return isDownload;
            }

            @Override
            public void run() {
                updateUI(download());
            }
        });
        worker.start();
    }

    public void getValuesFromBNRForGrafic(String m, String di, String ds) {
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(GenerareRapoarte.this,R.style.ProgressDialogStyle);
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        String mesagge = new String("Vă rugăm să așteptați !");
                        progressDialog.setMessage(mesagge);
                        progressDialog.show();
                    }
                });

                XmlPullParserFactory xmlPullParserFactory;
                try {
                    InputStream is = new ByteArrayInputStream(GetContentFromUrl().getBytes(StandardCharsets.UTF_8));
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlPullParserFactory.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    ParsareRaportXMLForGrafic(parser, m, di, ds);
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

    private Thread worker1;

    public void ParsareRaportXMLForLista(XmlPullParser parser, String m, String di, String ds) throws XmlPullParserException, IOException {
        //parsare xml
        mapmin = new LinkedHashMap<>();
        mapmax = new LinkedHashMap<>();
        final int[] event = {parser.getEventType()};
        final String[] date = {new String()};
        MonedaValoare monedaValoare = new MonedaValoare();
        final MonedaValoare[] linie_curenta = {null};

        worker1 = new Thread(new Runnable() {
            private void updateUI(final boolean download) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        // verificare status gasire date
                        if (download) {
                            for (Map.Entry<String, Float> map : mapmin.entrySet()) {
                                monede.add(map.getKey());
                                min.add(map.getValue());
                            }
                            for (Map.Entry<String, Float> map1 : mapmax.entrySet()) {
                                max.add(map1.getValue());
                            }
                            monede = (ArrayList<String>) monede.stream().sorted().collect(Collectors.toList());
                            SetIntervale(di, ds);
                            progressDialog.hide();
                            valori.clear();
                            zile.clear();
                            linearLayout.setVisibility(View.VISIBLE);
                            linechart.setVisibility(View.INVISIBLE);
                            btn_grafic.setTextColor(Color.parseColor("#ff0000"));
                            btn_lista.setTextColor(Color.parseColor("#7fff00"));
                            adapter.notifyDataSetChanged();
                        } else {
                            linearLayout.setVisibility(View.INVISIBLE);
                            Toast.makeText(GenerareRapoarte.this, "Nu s-au gasit date!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });
            }

            public boolean download() {
                boolean isDownload = false;
                while (event[0] != XmlPullParser.END_DOCUMENT) {
                    String numecamp;
                    switch (event[0]) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            numecamp = parser.getName();
                            if (numecamp.equals("Cube")) {
                                date[0] = null;
                                date[0] = parser.getAttributeValue(null, "date");
                                Date begin = null;
                                Date end = null;
                                Date data = null;
                                try {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    begin = dateFormat.parse(di);
                                    end = dateFormat.parse(ds);
                                    data = dateFormat.parse(date[0]);
                                    Calendar calendarStart = Calendar.getInstance();
                                    calendarStart.setTime(begin);
                                    Calendar calendarFinal = Calendar.getInstance();
                                    calendarFinal.setTime(end);
                                    Calendar calendarData = Calendar.getInstance();
                                    calendarData.setTime(data);
                                    if (calendarData.before(calendarStart)) {
                                        //parser.nextText();
                                    } else {
                                        linie_curenta[0] = new MonedaValoare();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (linie_curenta[0] != null) {
                                while (true) {
                                    try {
                                        if (!(parser.getEventType() != XmlPullParser.END_TAG))
                                            break;
                                    } catch (XmlPullParserException e) {
                                        e.printStackTrace();
                                    }
                                    String name = parser.getName();
                                    if (name.equals("Rate")) {
                                        Date begin = null;
                                        Date end = null;
                                        Date data = null;
                                        try {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                            begin = dateFormat.parse(di);
                                            end = dateFormat.parse(ds);
                                            data = dateFormat.parse(date[0]);
                                            Calendar calendarStart = Calendar.getInstance();
                                            calendarStart.setTime(begin);
                                            Calendar calendarFinal = Calendar.getInstance();
                                            calendarFinal.setTime(end);
                                            Calendar calendarData = Calendar.getInstance();
                                            calendarData.setTime(data);
                                            if (((calendarData.after(calendarStart) && calendarData.before(calendarFinal))
                                                    || calendarData.equals(calendarStart)
                                                    || calendarData.equals(calendarFinal))) {
                                                String s = new String(parser.getAttributeValue(null, "currency"));
                                                parser.next();
                                                if (mapmin.containsKey(s)) {
                                                    if (Float.parseFloat(String.valueOf(mapmin.get(s))) > Float.parseFloat(parser.getText())) {
                                                        mapmin.put(s, Float.parseFloat(parser.getText()));
                                                    }
                                                } else {
                                                    mapmin.put(s, Float.parseFloat(parser.getText()));
                                                }
                                                if (mapmax.containsKey(s)) {
                                                    if (Float.parseFloat(String.valueOf(mapmax.get(s))) < Float.parseFloat(parser.getText())) {
                                                        mapmax.put(s, Float.parseFloat(parser.getText()));
                                                    }
                                                } else {
                                                    mapmax.put(s, Float.parseFloat(parser.getText()));
                                                }
                                                parser.nextTag();
                                                isDownload = true;
                                            }

                                            //daca data din xml > data sfarsit, oprim parsarea
                                            if (calendarData.after(calendarFinal)) {
                                                return isDownload;

                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        } catch (XmlPullParserException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    try {
                        event[0] = parser.next();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                return isDownload;
            }

            @Override
            public void run() {
                updateUI(download());
            }
        });
        worker1.start();
    }

    public void getValuesFromBNRForLista(String m, String di, String ds) {
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(GenerareRapoarte.this,R.style.ProgressDialogStyle);
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        String mesagge = new String("Vă rugăm să așteptați !");
                        progressDialog.setMessage(mesagge);
                        progressDialog.show();
                    }
                });

                XmlPullParserFactory xmlPullParserFactory;
                try {
                    InputStream is = new ByteArrayInputStream(GetContentFromUrl().getBytes(StandardCharsets.UTF_8));
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlPullParserFactory.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    ParsareRaportXMLForLista(parser, m, di, ds);
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

    protected void registeredNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisteredNetwork() {
        try {
            unregisterReceiver(broadcastReceiver1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
        unregisteredNetwork();
    }

}