package com.dashapp.model;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
    /*Classe dato per le note*/
public class NotaBean {
    private int id;
    private int idBrano;
    private int idUtente;
    private Time durata;
    private Date dataRegistrazione;
    private String luogoRegistrazione;
    private Time segmentoInizio;
    private Time segmentoFine;
    private String testoLibero;
    private List<String> strumenti;
    private List<String> esecutori;

    // Getters e setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdBrano() { return idBrano; }
    public void setIdBrano(int idBrano) { this.idBrano = idBrano; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public Time getDurata() { return durata; }
    public void setDurata(Time durata) { this.durata = durata; }

    public Date getDataRegistrazione() { return dataRegistrazione; }
    public void setDataRegistrazione(Date dataRegistrazione) { this.dataRegistrazione = dataRegistrazione; }

    public String getLuogoRegistrazione() { return luogoRegistrazione; }
    public void setLuogoRegistrazione(String luogoRegistrazione) { this.luogoRegistrazione = luogoRegistrazione; }

    public Time getSegmentoInizio() { return segmentoInizio; }
    public void setSegmentoInizio(Time segmentoInizio) { this.segmentoInizio = segmentoInizio; }

    public Time getSegmentoFine() { return segmentoFine; }
    public void setSegmentoFine(Time segmentoFine) { this.segmentoFine = segmentoFine; }

    public String getTestoLibero() { return testoLibero; }
    public void setTestoLibero(String testoLibero) { this.testoLibero = testoLibero; }

    public List<String> getStrumenti() { return strumenti; }
    public void setStrumenti(List<String> strumenti) { this.strumenti = strumenti; }

    public List<String> getEsecutori() { return esecutori; }
    public void setEsecutori(List<String> esecutori) { this.esecutori = esecutori; }
}

