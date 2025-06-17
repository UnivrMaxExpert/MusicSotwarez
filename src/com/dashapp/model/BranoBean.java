package com.dashapp.model;

import com.dashapp.view.ViewNavigator;

import javax.swing.text.View;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
    /*Classe che diviene il dato trasportato da controller a db utilizzato per i brani*/
public class BranoBean implements Serializable
{
    private int id;
    private int utente;
    private String titolo;
    private List<String> autori = new ArrayList<>();
    private Genere genere;
    private String file;
    private int anno;
    private boolean concerto;
    private Ruoli ruolo;

    public BranoBean() {
    }

    public BranoBean(String titolo, int utente, Genere genere, String file , int anno, boolean concerto, String... autorix) {
        this.titolo = titolo;
        this.utente = utente;
        this.genere = genere;
        autori = new ArrayList<>();
        autori.addAll(Arrays.asList(autorix));
        this.file = file;
        this.concerto = concerto;
        this.anno = anno;

    }

    public BranoBean(int id, int utente, String titolo, Genere genere, String file, int anno,  boolean concerto, String... autorix) {
        this.id = id;
        this.utente = utente;
        this.titolo = titolo;
        this.genere = genere;
        this.file = file;
        this.anno = anno;
        autori.addAll(Arrays.asList(autorix));
        this.concerto = concerto;
    }

    public BranoBean(String titolo, Genere genere, String file , String... autorix) {
        this.titolo = titolo;
        this.genere = genere;
        autori = new ArrayList<>();
        autori.addAll(Arrays.asList(autorix));
        this.file = file;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getAutori() {
        return String.join(",", autori);
    }

    public Genere getGenere() {
        return genere;
    }

    public String getFile() {
        return file;
    }

    public int getAnno() {
        return anno;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public boolean isConcerto() {
        return concerto;
    }

    public void setConcerto(boolean concerto) {
        this.concerto = concerto;
    }

    @Override
    public String toString() {
        return "BranoBean{" +
                "id=" + id +
                ", utente=" + utente +
                ", titolo='" + titolo + '\'' +
                ", autori=" + autori +
                ", genere=" + genere +
                ", file='" + file + '\'' +
                ", anno=" + anno +
                ", concerto=" + concerto +
                '}';
    }

    public int getUtente() {
        return utente;
    }

    public void setUtente(int utente) {
        this.utente = utente;
    }

    public Ruoli getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruoli ruolo) {
        this.ruolo = ruolo;
    }
}
