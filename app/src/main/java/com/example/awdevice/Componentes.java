package com.example.awdevice;

public class Componentes {

    private String sensorChuva;
    private String sensorProximidade;
    private String statusJanela; // aberto ou fechado

    public String getSensorChuva() {
        return sensorChuva;
    }

    public void setSensorChuva(String sensorChuva) {
        this.sensorChuva = sensorChuva;
    }

    public String getSensorProximidade() {
        return sensorProximidade;
    }

    public void setSensorProximidade(String sensorProximidade) {
        this.sensorProximidade = sensorProximidade;
    }

    public String getStatusJanela() {
        return statusJanela;
    }

    public void setStatusJanela(String statusJanela) {
        this.statusJanela = statusJanela;
    }
}
