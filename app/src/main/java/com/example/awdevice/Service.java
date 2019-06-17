package com.example.awdevice;

public class Service {

    public static final String HTTP = "http://";
    public static final String DISPLAY_NAME = "/ArduinoWeb";
    public static final String STATUS_CONEXAO = "/conexao";
    //public static final String STATUS_COMPONENTES = "/status";
    public static final String STATUS_COMPONENTES = "/testeRetorno";
    //public static final String ABRE = "/abrir";
    public static final String ABRE = "/testeAbrir";
    //public static final String FECHA = "/fechar";
    public static final String FECHA = "/testeFechar";

    public String pathStatusConexao(String ip, String porta){
        return HTTP + ip + ":" + porta + DISPLAY_NAME + STATUS_CONEXAO;
    }


    public String pathStatusComponente(String ip, String porta){
        return HTTP + ip + ":" + porta + DISPLAY_NAME + STATUS_COMPONENTES;
    }

    public String pathAbrir(String ip, String porta){
        return HTTP + ip + ":" + porta + DISPLAY_NAME + ABRE;
    }

    public String pathFechar(String ip, String porta){
        return HTTP + ip + ":" + porta + DISPLAY_NAME + FECHA;
    }

}
