package com.example.awdevice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BuscarStatusJanela.AsyncResponse, AlterarStatusJanela.AsyncResponse {

    private Button botaoRecuperar;
    private Button botaoConfiguracoes;

    private static String ip = "";
    private static String porta = "";
    private static boolean isConectado = false;

    private static boolean OPCAO_USUARIO;
    private boolean configPreenchidas = false;

    private Service service;
    //private BuscarStatusJanela buscarStatus;
    private AlterarStatusJanela alterarStatusJanelaClass;
    private Componentes componentes;
    private ConfiguracaoActivity configuracao;

    private String sensorProximidade = "";
    private String sensorChuva = "";
    private String statusAtualJanela = "";
    private String retornoStatusJanela = "";

    String janela = "aberto";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("  AWDevice");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.awdevicelogo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        componentes = new Componentes();
        //buscarStatus = new BuscarStatusJanela(this);
        configuracao = new ConfiguracaoActivity();
        service = new Service();

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        botaoConfiguracoes = findViewById(R.id.btConfiguracao);
        botaoConfiguracoes.setBackground(getResources().getDrawable(R.drawable.conf));

        if (configuracao.fileExists(getApplicationContext(), "config.txt")) {
            String[] dados = configuracao.recuperarDadosConexao(getApplicationContext());
            ip = dados[0];
            porta = dados[1];
            isConectado = true;
        } else {
            botaoRecuperar.setText("Faça a configuração para visualizar");
        }

        if (isConectado) {

            try {
                buscarStatusComponentes();

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        } else {
            buildAlerta("É necessário configurar o IP e a PORTA");
        }

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConectado) {

                    sensorProximidade = componentes.getSensorProximidade();
                    sensorChuva = componentes.getSensorChuva();
                    statusAtualJanela = componentes.getStatusJanela();

                    alteraStatusJanela();
/*

                    if(janela.equals("aberto")){
                        botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.fechar));
                        janela = "fechada";
                    }else{
                        botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.abrir));
                        janela = "aberto";
                    }

*/

                } else {
                    buildAlerta("É necessário configurar o IP e a PORTA");
                }
            }
        });

        botaoConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Criar um Intent dizendo que Activity deve ser iniciada
                Intent intent = new Intent(MainActivity.this, ConfiguracaoActivity.class);

                // Chamar Activity usando o Intent
                startActivityForResult(intent, 1);
                finish();

            }
        });

    }

    public void buscarStatusComponentes() throws NullPointerException {
        String url = service.pathStatusComponente(ip, porta);
        new BuscarStatusJanela(this).execute(url);
    }

    @Override
    public void processFinishBuscarStatus(String output) {

        try {
            JSONObject jsonObject = new JSONObject(output);
            componentes.setSensorProximidade(jsonObject.getString("ultrasom"));
            componentes.setSensorChuva(jsonObject.getString("chuva"));
            componentes.setStatusJanela(jsonObject.getString("janela"));

            if (componentes.getStatusJanela().equals("0")) {
                alteraStatusBotao();
            } else if (componentes.getStatusJanela().equals("1")) {
                alteraStatusBotao();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinishAlterarStatus(String output) {

        try {
            JSONObject jsonObject = new JSONObject(output);
            componentes.setStatusJanela(jsonObject.getString("retorno"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void alteraStatusJanela() {

        componentes.setSensorProximidade("longe");

        // JANELA RETORNANDO: ABERTA

        if(componentes != null){
            if(componentes.getSensorProximidade().equals("perto")){

                // SE = 1, AÇÃO: FECHAR
                // SE = 0, AÇÃO: ABRIR
                if(componentes.getStatusJanela().equals("1")){
                    buildAlerta("Há algo no caminho e não foi possível fechar a janela!");
                }
            }else if(componentes.getSensorChuva().equals("1")){
                if(componentes.getStatusJanela().equals("0")){
                    buildAlertaConfirmacao("Está chovendo. Tem certeza que quer abrir a janela?");
                    if(OPCAO_USUARIO){
                        new AlterarStatusJanela(this).execute(service.pathAbrir(ip, porta), componentes.getStatusJanela());
                        alteraStatusBotao();
                    }
                }else if(componentes.getStatusJanela().equals("1")){
                    new AlterarStatusJanela(this).execute(service.pathFechar(ip, porta), componentes.getStatusJanela());
                    alteraStatusBotao();
                }
            }else{
                if(componentes.getStatusJanela().equals("0")){
                    new AlterarStatusJanela(this).execute(service.pathAbrir(ip, porta), componentes.getStatusJanela());
                    alteraStatusBotao();
                }else if(componentes.getStatusJanela().equals("1")){
                    new AlterarStatusJanela(this).execute(service.pathFechar(ip, porta), componentes.getStatusJanela());
                }else{
                    buildAlerta("Ocorreu um erro. Janela: " + componentes.getStatusJanela() + ", " +
                            "Ultrassom: " + componentes.getSensorProximidade() + ", Chuva: " + componentes.getSensorChuva());
                }
            }
        }
    }

    public void buildAlertaConfirmacao(String mensagem) {
        AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);

        alerta.setMessage(mensagem);
        alerta.setCancelable(true);

        alerta.setPositiveButton(
                "SIM",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OPCAO_USUARIO = true;
                        dialog.cancel();

                    }
                });

        alerta.setNegativeButton(
                "NÃO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OPCAO_USUARIO = false;
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = alerta.create();
        alert11.show();
    }

    public void buildAlerta(String mensagem) {
        AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);

        alerta.setMessage(mensagem);
        alerta.setCancelable(true);

        alerta.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        AlertDialog alert11 = alerta.create();
        alert11.show();
    }

    public void alteraStatusBotao(){
        if (componentes.getStatusJanela().equals("0")) {
            botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.abrir));
        } else if (componentes.getStatusJanela().equals("1")) {
            botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.fechar));
        }
    }
}