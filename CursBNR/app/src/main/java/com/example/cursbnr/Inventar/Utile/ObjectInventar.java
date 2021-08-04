package com.example.cursbnr.Inventar.Utile;

public class ObjectInventar {
    Float cantitate;
    String codbare;
    String denumire;
    Float pret;

    public ObjectInventar(String denumire, Float pret,String codbare, Float cantitate){
        this.denumire = denumire;
        this.pret = pret;
        this.codbare = codbare;
        this.cantitate = cantitate;
    }

    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire= denumire;
    }

    public Float getPret() {
        return pret;
    }

    public void setPret(Float pret){ this.pret = pret;}

    public String getCodbare() {
        return codbare;
    }

    public void setCodbare(String codbare){ this.codbare = codbare;}

    public Float getCantitate(){return cantitate;}

    public void setCantitate(Float cantitate) {
        this.cantitate = cantitate;
    }

    public void setEditTextValue(String toString) {
    }
}
