package com.example.cursbnr.GenerareRapoarte.Utile;

public class MonedaValoare {
    String moneda;
    String valoare;
    String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getValoare() {
        return valoare;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public void setValoare(String valoare) {
        this.valoare = valoare;
    }

    public MonedaValoare() {
        this.moneda = moneda;
        this.valoare = valoare;
        this.data = data;
    }

}
