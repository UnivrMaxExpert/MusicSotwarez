package com.dashapp.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

public class MetaBean implements Serializable
{
    private Integer id;
    private int idBrano;
    private int idUtente;
    private String titolo;
    private Date dataRegistrazione;
    private Time segmentoInizio;
    private Time segmentoFine;
    private String commento;
    private List<String> strumenti;
    private List<String> esecutori;

    public MetaBean(int id, int idBrano, int idUtente, String titolo, Date dataRegistrazione, Time segmentoInizio, Time segmentoFine, String commento, List<String> strumenti, List<String> esecutori) {
        this.id = id;
        this.idBrano = idBrano;
        this.idUtente = idUtente;
        this.titolo = titolo;
        this.dataRegistrazione = dataRegistrazione;
        this.segmentoInizio = segmentoInizio;
        this.segmentoFine = segmentoFine;
        this.commento = commento;
        this.strumenti = strumenti;
        this.esecutori = esecutori;
    }

    public MetaBean(int id, int idBrano, int idUtente, String titolo, Date dataRegistrazione, Time segmentoInizio, Time segmentoFine, String commento) {
        this.id = id;
        this.idBrano = idBrano;
        this.idUtente = idUtente;
        this.titolo = titolo;
        this.dataRegistrazione = dataRegistrazione;
        this.segmentoInizio = segmentoInizio;
        this.segmentoFine = segmentoFine;
        this.commento = commento;
    }

    public MetaBean() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBrano() {
        return idBrano;
    }

    public void setIdBrano(int idBrano) {
        this.idBrano = idBrano;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Date getDataRegistrazione() {
        return dataRegistrazione;
    }

    public void setDataRegistrazione(Date dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }

    public Time getSegmentoInizio() {
        return segmentoInizio;
    }

    public void setSegmentoInizio(Time segmentoInizio) {
        this.segmentoInizio = segmentoInizio;
    }

    public Time getSegmentoFine() {
        return segmentoFine;
    }

    public void setSegmentoFine(Time segmentoFine) {
        this.segmentoFine = segmentoFine;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public List<String> getStrumenti() {
        return strumenti;
    }

    public void setStrumenti(List<String> strumenti) {
        this.strumenti = strumenti;
    }

    public List<String> getEsecutori() {
        return esecutori;
    }

    public void setEsecutori(List<String> esecutori) {
        this.esecutori = esecutori;
    }

    @Override
    public String toString() {
        return "MetaBean{" +
                "id=" + id +
                ", idBrano=" + idBrano +
                ", idUtente=" + idUtente +
                ", titolo='" + titolo + '\'' +
                ", dataRegistrazione=" + dataRegistrazione +
                ", segmentoInizio=" + segmentoInizio +
                ", segmentoFine=" + segmentoFine +
                ", commento='" + commento + '\'' +
                ", strumenti=" + strumenti +
                ", esecutori=" + esecutori +
                '}';
    }
}
