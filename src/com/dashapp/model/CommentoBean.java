package com.dashapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
    /*Classe dato per salvare i commenti*/
public class CommentoBean implements Serializable
{
        private int id;
        private Integer parentId;
        private int branoId;
        private String autore;
        private String testo;
        private LocalDateTime data;
        private List<CommentoBean> risposte = new ArrayList<>();

    public int getAutoreId() {
        return autoreId;
    }

    public void setAutoreId(int autoreId) {
        this.autoreId = autoreId;
    }

    private int autoreId;
        // Getters & setters

        public void aggiungiRisposta(CommentoBean risposta) {
            risposte.add(risposta);
        }

        public List<CommentoBean> getRisposte() {
            return risposte;
        }

    public int getId() {
        return id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public int getBranoId() {
        return branoId;
    }

    public String getAutore() {return autore;}

    public String getTesto() {
        return testo;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public void setBranoId(int branoId) {
        this.branoId = branoId;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public void setRisposte(List<CommentoBean> risposte) {
        this.risposte = risposte;
    }
}
