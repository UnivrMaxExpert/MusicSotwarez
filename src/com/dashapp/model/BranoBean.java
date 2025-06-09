package com.dashapp.model;

import com.dashapp.view.ViewNavigator;

import javax.swing.text.View;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public BranoBean() {
    }

    public BranoBean(String titolo, Genere genere, String file , int anno, boolean concerto, String... autorix) {
        this.titolo = titolo;
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
        return "id=" + id +
                ", titolo='" + titolo + '\'' +
                ", autori=" + autori +
                ", genere=" + genere +
                ", file='" + file + '\'' +
                ", anno=" + anno;
    }
}
