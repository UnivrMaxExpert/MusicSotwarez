package com.dashapp.view;

public enum ViewState {
    LOGIN(0), REGISTRAZIONE(1), HOME(2), CARICA(3), CATALOGO(4), VISUALIZZAZIONE(5);

    public final int index;

    ViewState(int index) {
        this.index = index;
    }
}

