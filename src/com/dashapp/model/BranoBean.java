package com.dashapp.model;

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
    private Integer anno;
    private boolean concerto;
    private Ruoli ruolo;
    private FileType tipo;

    public BranoBean() {}
//
    private void controlTipo()
        {
            String lowered = file.toLowerCase();
        if (lowered.startsWith("https://www.youtube.com") || lowered.contains("youtu.be"))
           tipo = FileType.YOUTUBE;
        else if (lowered.endsWith(".pdf"))
            tipo = FileType.PDF;
        else if (lowered.endsWith(".jpg"))
            tipo = FileType.JPG;
        else if (lowered.endsWith(".jpeg"))
            tipo = FileType.JPEG;
        else if (lowered.endsWith(".wav"))
            tipo = FileType.WAV;
        else if (lowered.endsWith(".mp3"))
            tipo = FileType.MP3;
        else if (lowered.endsWith(".mp4"))
            tipo = FileType.MP4;
    }

    public BranoBean(String titolo, int utente, Genere genere, String file , Integer anno, boolean concerto, String... autorix) {
        this.titolo = titolo;
        this.utente = utente;
        this.genere = genere;
        autori = new ArrayList<>();
        autori.addAll(Arrays.asList(autorix));
        this.file = file;
        this.concerto = concerto;
        this.anno = anno;
        controlTipo();
    }

    public BranoBean(int id, int utente, String titolo, Genere genere, String file, Integer anno,  boolean concerto, String... autorix) {
        this.id = id;
        this.utente = utente;
        this.titolo = titolo;
        this.genere = genere;
        this.file = file;
        this.anno = anno;
        autori.addAll(Arrays.asList(autorix));
        this.concerto = concerto;
        controlTipo();
    }

    public BranoBean(String titolo, Genere genere, String file , String... autorix) {
        this.titolo = titolo;
        this.genere = genere;
        autori = new ArrayList<>();
        autori.addAll(Arrays.asList(autorix));
        this.file = file;
        controlTipo();
    }

    public String getTitolo() {return titolo;}
    public String getAutori() {return String.join(",", autori);}
    public Genere getGenere() {return genere;}
    public String getGenere2() {return genere.toString();}
    public String getFile() {return file;}
    public Integer getAnno() {return anno==null? null:anno;}
    public void setFile(String file) {this.file = file;}
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public boolean isConcerto() {return concerto;}
    public void setConcerto(boolean concerto) {this.concerto = concerto;}
    public int getUtente() {return utente;}
    public void setUtente(int utente) {this.utente = utente;}
    public Ruoli getRuolo() {return ruolo;}
    public void setRuolo(Ruoli ruolo) {this.ruolo = ruolo;}
    public String getTipo() {return tipo.toString();}
    public void setTipo(FileType tipo) {this.tipo = tipo;}

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
                ", ruolo=" + ruolo +
                ", tipo=" + tipo +
                '}';
    }
}
enum FileType {
    MP4,
    MP3,
    PDF,
    JPG,
    JPEG,
    YOUTUBE,
    WAV
}