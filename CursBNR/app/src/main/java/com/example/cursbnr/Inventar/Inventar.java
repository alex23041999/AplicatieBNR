package com.example.cursbnr.Inventar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursBNR.CursValutar.Utile.CheckingConnection;
import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.DateBaseHelper;
import com.example.cursbnr.Inventar.Listener.OnRecyclerViewRow;
import com.example.cursbnr.Inventar.Utile.BarcodeScan;
import com.example.cursbnr.Inventar.Utile.FakeApiResponse;
import com.example.cursbnr.Inventar.Utile.JsonFakeApi;
import com.example.cursbnr.Inventar.Utile.ObjectInventar;
import com.example.cursbnr.Inventar.Utile.RecyclerViewInventar_Adapter;
import com.example.cursbnr.Inventar.retrofit.ApiServiceGenerator;
import com.example.cursbnr.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Inventar extends AppCompatActivity implements OnRecyclerViewRow {
    private CompositeDisposable compositeDisposable;
    BroadcastReceiver broadcastReceiver;
    Button btn_scanare, btn_trimitereCSV;
    EditText et_codBare, et_cantitate;
    RecyclerView recyclerView_inventar;
    RecyclerViewInventar_Adapter adapter;
    View inflatedView;
    DateBaseHelper dateBaseHelper1;
    ArrayList<ObjectInventar> objectInventars;
    RecyclerView.SmoothScroller smoothScroller;
    private static final String file_name = "proiectPracticaCSV.csv";
    FileWriter mFileWriter;
    private File f;
    public static String codbareTAG;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventar);
        initComponents();
        context = this;

        objectInventars = new ArrayList<>();
        broadcastReceiver = new CheckingConnection();
        dateBaseHelper1 = new DateBaseHelper(Inventar.this);
        smoothScroller = new
                LinearSmoothScroller(this.getApplicationContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };

        registeredNetwork();
        initRV();
        doneKeyboardCodBare();
        doneKeyboardCantitate();
        editTextCodbare();
        setMaxLenghtEditText(et_codBare);
        btnScanareOnClick();
        btnTrimitereOnClick();
    }

    private void initComponents() {
        broadcastReceiver = new CheckingConnection();
        btn_scanare = findViewById(R.id.btn_scanare);
        btn_trimitereCSV = findViewById(R.id.btn_trimitere);
        recyclerView_inventar = findViewById(R.id.recycler_inventar);
        et_codBare = findViewById(R.id.et_codbare);
        inflatedView = getLayoutInflater().inflate(R.layout.recyclerview_inventar, null);
        et_cantitate = inflatedView.findViewById(R.id.cantitate_produs);
    }

    protected void onStart() {
        super.onStart();
        compositeDisposable = new CompositeDisposable();
        ProgressDialog progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Procesare date");
        String mesagge = new String("Datele se ??ncarc?? !");
        progressDialog.setMessage(mesagge);
        progressDialog.show();
        compositeDisposable.add(Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            retrofit();
                            progressDialog.hide();
                            progressDialog.dismiss();
                        }
                ));
    }

    //btnTrimitereOnClick -> functie in care se verifica exista si marimea fisierul CSV , iar in caz ca acesta exista si contine elementele , va fi trimis pe calea selectata
    private void btnTrimitereOnClick() {
        btn_trimitereCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String baseDir = android.os.Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
                    String filePath = baseDir + File.separator + file_name;
                    f = new File(filePath);
                    CSVWriter writer;

                    if (f.exists() && !f.isDirectory()) {
                        mFileWriter = new FileWriter(filePath, false);
                        writer = new CSVWriter(mFileWriter);
                    } else {
                        writer = new CSVWriter(new FileWriter(filePath));
                    }
                    String[] s = {"Denumire", "Pret", "Cod de bare", "Cantitate"};
                    writer.writeNext(s);
                    for (ObjectInventar object : objectInventars) {
                        String[] item = {object.getDenumire(), object.getPret().toString(), object.getCodbare(), object.getCantitate().toString()};
                        writer.writeNext(item);
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (f.exists() && f.length() > 0) {
                        Context context = getApplicationContext();
                        Uri path = FileProvider.getUriForFile(context, "com.example.cursbnr.fileprovider", f);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/csv");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Produse inventar");
                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(sendIntent, "Send E-mail"));
                    }else{
                        Toast.makeText(Inventar.this, "Fisierul nu poate fi trimis, reincercati!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //btnScanareOnClick -> functie prin care pornim camera telefonului si scanam un cod de bare
    private void btnScanareOnClick() {
        btn_scanare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(Inventar.this);
                intentIntegrator.setCaptureActivity(BarcodeScan.class);
                intentIntegrator.setPrompt("Scan the codebare");
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.initiateScan();
            }
        });
    }

    //onActivityResult -> functie in care primim codul scanat cu ajutorul camerei si pe care il introducem in EditText
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "Nu s-a gasit niciun cod", Toast.LENGTH_SHORT).show();
            } else if (intentResult.getContents().length() == 12) {
                et_codBare.setText(intentResult.getContents());
                et_codBare.clearFocus();
            } else {
                et_codBare.getText().clear();
                et_codBare.clearFocus();
                Toast.makeText(this, "Codul de bare nu exista !", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //editTextCodbare -> functie prin care urmarim EditText-ul ce contine codul de bare scanat/introdus pentru a realizeaza operatiile dorite
    private void editTextCodbare() {
        et_codBare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int i = -1;
                for (ObjectInventar item : objectInventars) {
                    if (item.getCodbare().equals(s.toString())) {
                        i = objectInventars.indexOf(item);
                        objectInventars.get(i).setCantitate(objectInventars.get(i).getCantitate() + 1);
                        new DateBaseHelper(context).updateDataBaseInventar(objectInventars.get(i).getDenumire(), objectInventars.get(i).getCantitate());
                        codbareTAG = objectInventars.get(i).getCodbare();
                        smoothScroller.setTargetPosition(i);
                        recyclerView_inventar.getLayoutManager().startSmoothScroll(smoothScroller);
                    }
                }
                adapter.notifyDataSetChanged();
                if(i == -1 && s.length() == 12){
                    Toast.makeText(Inventar.this, "Codul de bare introdus nu exista!", Toast.LENGTH_LONG).show();
                    et_codBare.getText().clear();
                }
            }
        });
    }

    private void setMaxLenghtEditText(EditText editText) {
        InputFilter[] editFilters = editText.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.LengthFilter(12);
        editText.setFilters(newFilters);
    }

    //retrofit -> functia in care am introdus valorile preluate din Json-ul creat in baza noastra de date
    private void retrofit() {
        if (dateBaseHelper1.isEmpty()) {
            final JsonFakeApi service = ApiServiceGenerator.createService(JsonFakeApi.class);
            Call<FakeApiResponse> call = service.getProduse();
            call.enqueue(new Callback<FakeApiResponse>() {
                @Override
                public void onResponse(Call<FakeApiResponse> call, Response<FakeApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(Inventar.this, "" + response.code(), Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            for (int i = 0; i < response.body().produse.size(); i++) {
                                dateBaseHelper1.insertValuesInventar(response.body().produse.get(i));
                            }
                            dateBaseHelper1.deleteDatasInventar();
                            objectInventars.clear();
                            for (int i = 0; i < dateBaseHelper1.getObjects().size(); i++) {
                                objectInventars.add(i, dateBaseHelper1.getObjects().get(i));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<FakeApiResponse> call, Throwable t) {
                    Toast.makeText(Inventar.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (!dateBaseHelper1.isEmpty()) {
            objectInventars.clear();
            for (int i = 0; i < dateBaseHelper1.getObjects().size(); i++) {
                objectInventars.add(i, dateBaseHelper1.getObjects().get(i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void initRV() {
        try {
            adapter = new RecyclerViewInventar_Adapter(objectInventars, this, this);
            recyclerView_inventar.setAdapter(adapter);
            recyclerView_inventar.setHasFixedSize(true);
            recyclerView_inventar.setLayoutManager(new LinearLayoutManager(this));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL); // linie delimitare randuri
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_recyclerview));
            recyclerView_inventar.addItemDecoration(dividerItemDecoration);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doneKeyboardCantitate() {
        et_cantitate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        et_cantitate.clearFocus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
    }

    private void doneKeyboardCodBare() {
        et_codBare.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        if (et_codBare.getText().toString().length() != 12) {
                            Toast.makeText(Inventar.this, "Cod de bare invalid", Toast.LENGTH_SHORT).show();
                            et_codBare.getText().clear();
                            et_codBare.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        } else if (et_codBare.getText().toString().length() == 12) {
                            et_codBare.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
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

    @Override
    public void onClick(int rowCount) {
    }

}