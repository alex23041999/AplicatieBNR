package com.example.cursbnr.Inventar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    Button btn_scanare, btn_salvareCSV;
    EditText et_codBare, et_cantitate;
    RecyclerView recyclerView_inventar;
    RecyclerViewInventar_Adapter adapter;
    View inflatedView;
    DateBaseHelper dateBaseHelper1;
    ArrayList<ObjectInventar> objectInventars;
    RecyclerView.SmoothScroller smoothScroller;
    private static final String File_name = "proiectPracticaCSV.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventar);
        initComponents();

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
        initRvElements();
        doneKeyboardCodBare();
        doneKeyboardCantitate();
        editTextCodbare();
        setMaxLenghtEditText(et_codBare);
        btnScanareOnClick();
        btnSalvareCSVonClick();
    }

    private void initComponents() {
        broadcastReceiver = new CheckingConnection();
        btn_scanare = findViewById(R.id.btn_scanare);
        btn_salvareCSV = findViewById(R.id.btn_salvare);
        recyclerView_inventar = findViewById(R.id.recycler_inventar);
        et_codBare = findViewById(R.id.et_codbare);
        inflatedView = getLayoutInflater().inflate(R.layout.recyclerview_inventar, null);
        et_cantitate = inflatedView.findViewById(R.id.cantitate_produs);
    }

    private void btnSalvareCSVonClick(){
        btn_salvareCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String filePath = Environment.getExternalStorageDirectory().toString()+ File_name;
                    File file = new File(Environment.getExternalStorageDirectory(),File_name);
                    CSVWriter csvWriter;
                    if(!file.exists())
                    {
                        FileWriter mFileWriter = new FileWriter(filePath, true);
                        csvWriter = new CSVWriter(mFileWriter);
                    }
                    else
                    {
                        csvWriter = new CSVWriter(new FileWriter(filePath));
                    }
                    String[] data = {"Ship Name", "Scientist Name", "...", };
                    csvWriter.writeNext(data);
                    csvWriter.close();
                    Toast.makeText(Inventar.this, "locatie: "+filePath, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void btnScanareOnClick(){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult!=null){
            if(intentResult.getContents() == null){
                Toast.makeText(this, "Eroare", Toast.LENGTH_SHORT).show();
            }else if (intentResult.getContents().length() == 12){
                et_codBare.setText(intentResult.getContents());
                et_codBare.clearFocus();
            }else{
                et_codBare.getText().clear();
                et_codBare.clearFocus();
                Toast.makeText(this, "Codul de bare nu exista !", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

//     protected void onStart() {
//        super.onStart();
//        compositeDisposable = new CompositeDisposable();
//        ProgressDialog progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setTitle("Procesare date");
//        String mesagge = new String("Datele se încarcă !");
//        progressDialog.setMessage(mesagge);
//        progressDialog.show();
//        compositeDisposable.add(Observable.timer(2, TimeUnit.SECONDS)
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        result -> {
//                            retrofit();
//                            progressDialog.hide();
//                        }
//                ));
//    }

    private void editTextCodbare(){
        et_codBare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //adapter.filterList(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int i = 0;
                if(s.toString().length() == 12){
                    for (ObjectInventar item : objectInventars) {
                        if (item.getCodbare().equals(s.toString())) {
                           i = objectInventars.indexOf(item);
                        }
                    }
                    if(i == 0){
                        Toast.makeText(Inventar.this, "Codul de bare introdus nu exista !", Toast.LENGTH_SHORT).show();
                        et_codBare.getText().clear();
                    }else{
                        Toast.makeText(Inventar.this, "Codul de bare introdus apartine produsului: " + objectInventars.get(i).getDenumire().toUpperCase(), Toast.LENGTH_LONG).show();
                        smoothScroller.setTargetPosition(i);
                       recyclerView_inventar.getLayoutManager().startSmoothScroll(smoothScroller);
                        et_codBare.clearFocus();
                    }
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setMaxLenghtEditText(EditText editText){
        InputFilter[] editFilters = editText.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.LengthFilter(12);
        editText.setFilters(newFilters);
    }

    private void initRvElements(){
        compositeDisposable = new CompositeDisposable();
        ProgressDialog progressDialog = new ProgressDialog(Inventar.this, R.style.ProgressDialogStyle);
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
                            if(!dateBaseHelper1.isEmpty()){
                                objectInventars.clear();
                                for(int i=0;i<dateBaseHelper1.getObjects().size();i++){
                                    objectInventars.add(i,dateBaseHelper1.getObjects().get(i));
                                }
                            }else{
                                Toast.makeText(this, "Baza de date este goala !", Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                            progressDialog.hide();
                        }
                ));
    }

    private void retrofit() {
        final JsonFakeApi service = ApiServiceGenerator.createService(JsonFakeApi.class);
        Call<FakeApiResponse> call = service.getProduse();
        call.enqueue(new Callback<FakeApiResponse>() {
            @Override
            public void onResponse(Call<FakeApiResponse> call, Response<FakeApiResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Inventar.this, "" + response.code(), Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        for(int i=0; i < response.body().produse.size(); i++){
                            dateBaseHelper1.insertValuesInventar(response.body().produse.get(i));
                        }
                        dateBaseHelper1.DeleteDatasInventar();
                        objectInventars.clear();
                        if(!dateBaseHelper1.isEmpty()){
                            for(int i=0;i<dateBaseHelper1.getObjects().size();i++){
                                objectInventars.add(i,dateBaseHelper1.getObjects().get(i));
                            }
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

                        et_cantitate.getText().clear();
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
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

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
    public void onClick(int rowCount){
    }
}