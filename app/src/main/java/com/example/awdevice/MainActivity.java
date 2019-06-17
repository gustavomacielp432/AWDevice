package com.example.awdevice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BuscarStatusJanela.AsyncResponse, AlterarStatusJanela.AsyncResponse, Conexao.AsyncResponse {

    private Button botaoRecuperar;
    private Button botaoConfiguracoes;
    private ProgressBar progressBar;

    private static String ip = "";
    private static String porta = "";
    private static boolean isConectado = false;

    private static boolean OPCAO_USUARIO;
    private boolean configPreenchidas = false;
    private boolean carregado = false;

    private Service service;
    private Componentes componentes;
    private ConfiguracaoActivity configuracao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("  AWDevice");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.awdevicelogo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        componentes = new Componentes();
        configuracao = new ConfiguracaoActivity();
        service = new Service();

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        botaoConfiguracoes = findViewById(R.id.btConfiguracao);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        botaoConfiguracoes.setBackground(getResources().getDrawable(R.drawable.conf));

        if (configuracao.fileExists(getApplicationContext(), configuracao.getFileName())) {
            String[] dados = configuracao.recuperarDadosConexao(getApplicationContext());
            ip = dados[0];
            porta = dados[1];
            executarTaskTestarConexao();

        } else {
            botaoRecuperar.setText("INDISPONÍVEL");
            buildAlerta("É necessário configurar o IP e a PORTA");
        }

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConectado) {

                    botaoRecuperar.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    buscarStatusComponentes();
                    botaoRecuperar.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    alteraStatusJanela();

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

    public void alteraStatusJanela() {

        if (componentes != null) {
            if (componentes.getSensorProximidade().equals("perto")) {
                // SE = 1, AÇÃO: FECHAR
                // SE = 0, AÇÃO: ABRIR
                if (componentes.getStatusJanela().equals("1")) {
                    buildAlerta("Há algo no caminho e não foi possível fechar a janela!");
                }
            } else if (componentes.getSensorChuva().equals("1")) {
                if (componentes.getStatusJanela().equals("0")) {
                    buildAlertaConfirmacao("Está chovendo. Tem certeza que quer abrir a janela?");
                } else if (componentes.getStatusJanela().equals("1")) {
                    executarTaskFecharJanela();
                }
            } else {
                if (componentes.getStatusJanela().equals("0")) {
                    executarTaskAbrirJanela();
                } else if (componentes.getStatusJanela().equals("1")) {
                    executarTaskFecharJanela();
                } else {
                    buildAlerta("Ocorreu um erro. Janela: " + componentes.getStatusJanela() + ", " +
                            "Ultrassom: " + componentes.getSensorProximidade() + ", Chuva: " + componentes.getSensorChuva());
                }
            }
        }
    }

    @Override
    public void processFinishBuscarStatus(String output) {

        try {
            JSONObject jsonObject = new JSONObject(output);
            componentes.setSensorProximidade(jsonObject.getString("ultrasom"));
            componentes.setSensorChuva(jsonObject.getString("chuva"));
            componentes.setStatusJanela(jsonObject.getString("janela"));
            alteraStatusBotao();
            progressBar.setVisibility(View.INVISIBLE);
            botaoRecuperar.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinishAlterarStatus(String output) {

        try {
            JSONObject jsonObject = new JSONObject(output);
            componentes.setStatusJanela(jsonObject.getString("retorno"));
            alteraStatusBotao();
            progressBar.setVisibility(View.INVISIBLE);
            botaoRecuperar.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processFinishConexao(String output) {

        String objetoTesteConexao = "";

        if (output.equals("NULO")) {
            isConectado = false;
            botaoRecuperar.setText("INDISPONÍVEL");
            buildAlerta("Conexão não realizada. IP ou PORTA inválidos.");

        } else {

            try {
                JSONObject jsonObject = new JSONObject(output);
                objetoTesteConexao = jsonObject.getString("retorno");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (objetoTesteConexao.equals("200")) {
                isConectado = true;
                carregado = true;

                try {
                    botaoRecuperar.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    buscarStatusComponentes();

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }else{
                buildAlerta("Falha na conexão|");
                botaoRecuperar.setText("INDISPONÍVEL");
            }
        }
    }

    public void executarTaskAbrirJanela() {
        new AlterarStatusJanela(this).execute(service.pathAbrir(ip, porta), componentes.getStatusJanela());
    }

    public void executarTaskFecharJanela() {
        new AlterarStatusJanela(this).execute(service.pathFechar(ip, porta), componentes.getStatusJanela());
    }

    public void executarTaskTestarConexao() {
        new Conexao(this).execute(service.pathStatusConexao(ip, porta));
    }

    public void alteraStatusBotao() {
        if (componentes.getStatusJanela().equals("0")) {
            botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.abrir));
        } else if (componentes.getStatusJanela().equals("1")) {
            botaoRecuperar.setBackground(getResources().getDrawable(R.drawable.fechar));
        } else {
            botaoRecuperar.setText("INDISPONÍVEL");
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
                        executarTaskAbrirJanela();
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
}