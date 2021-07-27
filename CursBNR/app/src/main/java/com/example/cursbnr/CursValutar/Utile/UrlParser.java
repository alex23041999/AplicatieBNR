package com.example.cursbnr.CursValutar.Utile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UrlParser {
    public String moneda, valoare;
    private String urlString;
    private XmlPullParserFactory xmlPullParserFactory;

    public String getMoneda(String moneda) {
        return moneda;
    }

    public String getValoare(String valoare) {
        return valoare;
    }

    //public String getData_curenta(String data_curenta){return data_curenta;}
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public void setValoare(String valoare) {
        this.valoare = valoare;
    }
    //public void setData_curenta(String data_curenta){this.data_curenta = data_curenta;}

}
