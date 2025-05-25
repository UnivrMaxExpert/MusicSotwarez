package com.dashapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BranoBean implements Serializable
{
    private int id;
    private String titolo;
    private List<String> autori;
    private Genere genere;
    private String file;
    private int anno;

    public BranoBean() {
    }

    public BranoBean(String titolo, Genere genere, String file , int anno , String... autorix) {
        this.titolo = titolo;
        this.genere = genere;
        autori = new ArrayList<>();
        autori.addAll(Arrays.asList(autorix));
        this.file = file;
        this.anno = anno;
    }

    public BranoBean(String titolo, Genere genere, String file ,String... autorix) {
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
}
