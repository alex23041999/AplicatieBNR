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
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.MyAsyncTask;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.RecyclerView_TipLista_Adapter;
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
    int i = 0;
    ArrayAdapter<String> adapter_spinner;
    MyAsyncTask myAsyncTask;
    GenerareRapoarte generareRapoarte;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generare_rapoarte);

        initComponents();

        valori = new ArrayList<>();
        zile = new ArrayList<>();
        monede = new ArrayList<>();
        intervale = new ArrayList<>();
        min = new ArrayList<>();
        max = new ArrayList<>();
        values = new ArrayList<>();
        broadcastReceiver1 = new CheckingConnection();
        dateBaseHelper = new DateBaseHelper(GenerareRapoarte.this);

        populateSpinnerAdapter();
        etStartDateClick();
        etEndDateClick();
        onToggleBtnGraficClick();
        onToggleBtnListaClick();
        onClickButtonSalvare();
        registeredNetwork();
        spinnerItemSelected();
        initRV();
    }

    private void initComponents() {
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
        try {
            compositeDisposable = new CompositeDisposable();
            ProgressDialog progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Procesare date");
            String mesagge = new String("Datele se încarcă !");
            progressDialog.setMessage(mesagge);
            progressDialog.show();
            compositeDisposable.add(Observable.timer(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> {
                                istoricRapoarteIntent();
                                progressDialog.hide();
                            }
                    ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //istoricRapoarteIntent -> functie prin care se verifica valorile primite prin intent din clasa IstoricRapoarte si atribuite campurilor din GenerareRapoarte
    private void istoricRapoarteIntent() {
        try{
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
                if (tip.equals("Grafic")) {
                    getValuesFromBNRForGrafic(intent.getStringExtra("moneda"), dataInceput, dataSfarsit);
                } else if (tip.equals("Lista")) {
                    getValuesFromBNRForLista(intent.getStringExtra("moneda"), dataInceput, dataSfarsit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void setIntervale(String di, String ds) {
        for (int i = 0; i < monede.size(); i++) {
            intervale.add(di + "\n" + ds);
        }
    }

    private void spinnerItemSelected() {
        sp_selectaremoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                try {
                    linechart.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.INVISIBLE);
                    btn_grafic.setChecked(false);
                    btn_lista.setChecked(false);
                    clearGrafic();
                    clearLista();
                    btn_grafic.setTextColor(getResources().getColor(R.color.white));
                    btn_lista.setTextColor(getResources().getColor(R.color.white));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void clearGrafic() {
        zile.clear();
        valori.clear();
    }

    private void clearLista() {
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
        adapter_spinner = new ArrayAdapter<>(this, R.layout.spinner_style, values);
        adapter_spinner.setDropDownViewResource(R.layout.dropdown_itembackground);
        sp_selectaremoneda.setAdapter(adapter_spinner);
    }

    //getContentFromUrl -> functie prin care se stabileste conexiunea la Url si se preia continutul acestuia
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

    //fetchXML -> functie prin care input-ului parser-ului i se atribuie continutul din Url si in care se realizeaza parsarea propriu-zisa
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
                    parsareXMLForSpinner(parser);
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

    //parsareXML -> functia in care se defineste procesul de parsare, pentru a obtine datele necesare
    public void parsareXMLForSpinner(XmlPullParser parser) throws XmlPullParserException, IOException {

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
        try {
            adapter_spinner.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //calendar -> functie prin care se genereaza calendarul din care selectam perioadele
    private void calendar(EditText editText) {
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

    private void etStartDateClick() {
        et_datastart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar(et_datastart);
            }
        });
    }

    private void etEndDateClick() {
        et_datafinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar(et_datafinal);
            }
        });
    }

    private void setLinechartValues() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < valori.size(); i++) {
            entries.add(new Entry(i, Float.parseFloat(valori.get(i))));
        }

        LineDataSet linedataset = new LineDataSet(entries, "Valori moneda");
        linedataset.setColor(Color.parseColor("#78BE20"));
        linedataset.setLineWidth(2);
        linedataset.setValueTextSize(10);
        linedataset.setValueTextColor(Color.parseColor("#000000"));
        linechart.getDescription().setEnabled(false);
        linechart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorAccent));
        linechart.getAxisRight().setDrawLabels(false);

        LineData lineData = new LineData(linedataset);
        linechart.setData(lineData);
        linechart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(zile));
        linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        linechart.getXAxis().setTextColor(getResources().getColor(R.color.lm_darkBlue));
        linechart.getXAxis().setTextSize(8);

        linechart.invalidate();
        linechart.setVisibleXRangeMaximum(5);
        linechart.setVisibleXRangeMinimum(2);
        linechart.moveViewToX(1);
    }

    private Integer compareDates(EditText start, EditText sfarsit) {
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

    private void onToggleBtnGraficClick() {
        btn_grafic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_grafic.setChecked(false);
                    btn_lista.setChecked(false);
                    if (et_datastart.getText().toString().trim().equals("") || et_datafinal.getText().toString().trim().equals("")) {
                        Toast.makeText(GenerareRapoarte.this, "Completati toate campurile!", Toast.LENGTH_SHORT).show();
                        linechart.setVisibility(View.INVISIBLE);
                    } else if (compareDates(et_datastart, et_datafinal) > 0) {
                        linechart.setVisibility(View.INVISIBLE);
                        Toast.makeText(GenerareRapoarte.this, "Selectati o ordine cronologica a datelor!", Toast.LENGTH_SHORT).show();
                    } else if (sp_selectaremoneda.getSelectedItem().toString().equals("Toate monedele") || sp_selectaremoneda.getSelectedItem().toString().equals("Selectare moneda")) {
                        Toast.makeText(GenerareRapoarte.this, "Raportul de tip grafic poate fi afisat doar pentru o moneda!", Toast.LENGTH_SHORT).show();
                    } else {
                        clearGrafic();
                        getValuesFromBNRForGrafic(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString());
                    }
                } else {
                    //
                }
            }
        });
    }

    private void onToggleBtnListaClick() {
        btn_lista.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_lista.setChecked(false);
                    btn_grafic.setChecked(false);
                    if (et_datastart.getText().toString().trim().equals("") || et_datafinal.getText().toString().trim().equals("")) {
                        Toast.makeText(GenerareRapoarte.this, "Completati toate campurile!", Toast.LENGTH_SHORT).show();
                        linearLayout.setVisibility(View.INVISIBLE);
                    } else if (compareDates(et_datastart, et_datafinal) > 0) {
                        linechart.setVisibility(View.INVISIBLE);
                        Toast.makeText(GenerareRapoarte.this, "Selectati o ordine cronologica a datelor!", Toast.LENGTH_SHORT).show();
                    } else if (!sp_selectaremoneda.getSelectedItem().toString().equals("Toate monedele")) {
                        Toast.makeText(GenerareRapoarte.this, "Raportul de tip lista poate fi afisat doar pentru toate monedele!", Toast.LENGTH_SHORT).show();
                    } else {
                        clearLista();
                        getValuesFromBNRForLista(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString());
                    }
                } else {

                }
            }
        });
    }

    private void onClickButtonSalvare() {
        btn_salvare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sp_selectaremoneda.getSelectedItem().toString().equals("Toate monedele") && !et_datastart.getText().toString().equals("") && !et_datafinal.getText().toString().equals("") && min.size() > 0) {
                    Boolean result;
                    result = dateBaseHelper.insertValues(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString(), "Lista");
                    if (result) {
                        Toast.makeText(GenerareRapoarte.this, "Tipul de raport a fost salvat cu succes !", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GenerareRapoarte.this, "Esec la salvare !", Toast.LENGTH_SHORT).show();
                    }
                } else if (!sp_selectaremoneda.getSelectedItem().toString().equals("Toate monelede") && !sp_selectaremoneda.getSelectedItem().toString().equals("Selectare moneda") && !et_datastart.getText().toString().equals("") && !et_datafinal.getText().toString().equals("") && valori.size() > 0) {
                    Boolean result;
                    result = dateBaseHelper.insertValues(sp_selectaremoneda.getSelectedItem().toString(), et_datastart.getText().toString(), et_datafinal.getText().toString(), "Grafic");
                    if (result) {
                        Toast.makeText(GenerareRapoarte.this, "Tipul de raport a fost salvat cu succes !", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GenerareRapoarte.this, "Esec la salvare !", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GenerareRapoarte.this, "Toate datele trebuie completate si raportul generat inainte de salvare!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //getContentFromUrlAnual ->functie prin care se stabileste conexiunea la Url-ul anual si se preia continutul acestuia
    public String getContentFromUrlAnual() throws IOException {
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

    //parsareRaportXMLForGrafic -> functia in care se defineste procesul de parsare, pentru a obtine datele necesare pentru a obtine datele necesare trasarii graficului
    public void parsareRaportXMLForGrafic(XmlPullParser parser, String m, String di, String ds) throws XmlPullParserException, IOException {
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
                            btn_lista.setTextColor(getResources().getColor(R.color.white));
                            btn_grafic.setTextColor(getResources().getColor(R.color.black));
                            clearLista();
                        } else {
                            linechart.setVisibility(View.INVISIBLE);
                            Toast.makeText(GenerareRapoarte.this, "Nu s-au gasit date!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });
            }

            //download ->functie ce returneaza "true" in cazul in care sunt gasite date
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
                                            //verificari ale datei intalnite in Url, intrucat trebuie preluate doar valorile din intervalul ales de utilizator
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

    //getValuesFromBNRForGrafic -> functie prin care input-ului parser-ului i se atribuie continutul din Url si in care se realizeaza parsarea propriu-zisa
    public void getValuesFromBNRForGrafic(String m, String di, String ds) {
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(GenerareRapoarte.this, R.style.ProgressDialogStyle);
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
                    InputStream is = new ByteArrayInputStream(getContentFromUrlAnual().getBytes(StandardCharsets.UTF_8));
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlPullParserFactory.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    parsareRaportXMLForGrafic(parser, m, di, ds);
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

    //parsareRaportXMLForLista -> functia in care se defineste procesul de parsare, pentru a obtine datele necesare pentru formarea listei
    public void parsareRaportXMLForLista(XmlPullParser parser, String m, String di, String ds) throws XmlPullParserException, IOException {
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
                            setIntervale(di, ds);
                            progressDialog.hide();
                            valori.clear();
                            zile.clear();
                            linearLayout.setVisibility(View.VISIBLE);
                            linechart.setVisibility(View.INVISIBLE);
                            btn_grafic.setTextColor(Color.parseColor("#FFFFFF"));
                            btn_lista.setTextColor(Color.parseColor("#000000"));
                            adapter.notifyDataSetChanged();
                        } else {
                            linearLayout.setVisibility(View.INVISIBLE);
                            Toast.makeText(GenerareRapoarte.this, "Nu s-au gasit date!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });
            }

            //download ->functie ce returneaza "true" in cazul in care sunt gasite date
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
                                            //verificari ale datei intalnite in Url, intrucat trebuie preluate doar valorile din intervalul ales de utilizator
                                            if (((calendarData.after(calendarStart) && calendarData.before(calendarFinal))
                                                    || calendarData.equals(calendarStart)
                                                    || calendarData.equals(calendarFinal))) {
                                                String s = new String(parser.getAttributeValue(null, "currency"));
                                                parser.next();
                                                //pentru stabilirea valorilor de minim si maxim pentru lista, am folosit doua LinkedHashMaps
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
                                            e.printStackTrace(); }
                                    } } }
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
                    } }
                return isDownload;
            }
            @Override
            public void run() {
                updateUI(download());
            }
        });
        worker1.start();
    }

    //getValuesFromBNRForLista -> functie prin care input-ului parser-ului i se atribuie continutul din Url si in care se realizeaza parsarea propriu-zisa
    public void getValuesFromBNRForLista(String m, String di, String ds) {
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(GenerareRapoarte.this, R.style.ProgressDialogStyle);
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
                    InputStream is = new ByteArrayInputStream(getContentFromUrlAnual().getBytes(StandardCharsets.UTF_8));
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlPullParserFactory.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    parsareRaportXMLForLista(parser, m, di, ds);
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