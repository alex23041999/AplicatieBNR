package com.example.cursbnr.CursBNR.CursValutar.Utile;

import org.xmlpull.v1.XmlPullParserFactory;

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
