package com.example.awdevice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.support.v7.app.AppCompatActivity;

public class ConfiguracaoActivity extends AppCompatActivity implements Conexao.AsyncResponse {

    private EditText etIp;
    private EditText etPorta;
    private TextView tvTesteConexao;
    private Button btTestarConexao;
    private Button btSalvar;

    private String ip = "";
    private String porta = "";

    Service service;

    private static final String FILE_NAME = "config.txt";

    private static boolean isConectado = false;
    private static boolean conexaoTestada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracao_activity);
        setTheme(R.style.AppTheme);

        getSupportActionBar().setTitle("  AWDevice");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.awdevicelogo);


        etIp = findViewById(R.id.etIp);
        etPorta = findViewById(R.id.etPorta);
        tvTesteConexao = findViewById(R.id.tvTesteConexao);
        btTestarConexao = findViewById(R.id.btTestarConexao);
        btSalvar = findViewById(R.id.btSalvar);
        btTestarConexao.setText("");
        btTestarConexao.setBackground(getResources().getDrawable(R.drawable.testar));
        btSalvar.setText("");
        btSalvar.setBackground(getResources().getDrawable(R.drawable.salvar));
        service = new Service();


        if (fileExists(getApplicationContext(), FILE_NAME)) {
            String[] dados = recuperarDadosConexao(getApplicationContext());
            etIp.setText(dados[0]);
            etPorta.setText(dados[1]);
            ip = dados[0];
            porta = dados[1];
            testarConexao();
        }


        etPorta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ip = etIp.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etPorta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                porta = etPorta.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        btTestarConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ip.isEmpty() && !porta.isEmpty()) {
                    ip = etIp.getText().toString();
                    porta = etPorta.getText().toString();
                    testarConexao();
                    if(isConectado){

                    }

                } else {
                    buildAlerta("Preencha todos os campos antes de fazer a conexão.");
                }
            }
        });

        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ip.isEmpty() && !porta.isEmpty()) {
                    if (isConectado) {
                        onBackPressed();

                    } else{
                        if(!conexaoTestada){
                            buildAlerta("É preciso testar a conexão antes de salvar!");
                        }else{
                            buildAlerta("Dados de conexão inválidos!");
                        }
                    }

                } else {
                    buildAlerta("Preencha todos os campos antes de salvar");
                }
            }
        });
    }

    public void testarConexao() {
        new Conexao(this).execute(service.pathStatusConexao(ip, porta));
    }

    @Override
    public void processFinishConexao(String output) {

        String objetoTesteConexao = "";

        if (output.equals("NULO")) {
            tvTesteConexao.setVisibility(View.INVISIBLE);
            isConectado = false;
            buildAlerta("Conexão não realizada. IP ou PORTA inválidos.");

        } else {

            try {
                JSONObject jsonObject = new JSONObject(output);
                objetoTesteConexao = jsonObject.getString("retorno");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (objetoTesteConexao.equals("200")) {
                tvTesteConexao.setVisibility(View.VISIBLE);
                tvTesteConexao.setTextColor(getResources().getColor(R.color.ColorTesteConexao));
                tvTesteConexao.setText("Conexão realizada com sucesso!");
                isConectado = true;
                conexaoTestada = true;
                String dadosConexao = ip + "\n" + porta;
                salvarDadosConexao(dadosConexao, getApplicationContext());

            }else{
                buildAlerta("Falha na conexão|");
            }
        }
    }

    public String getIp() {
        return ip;
    }

    public String getPorta() {
        return porta;
    }

    public String getFileName(){return FILE_NAME; }


    private void makeToast(CharSequence mensagem) {
        Toast toast = Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void salvarDadosConexao(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String[] recuperarDadosConexao(Context context) {
        String[] camposConexao = new String[2];
        try {
            InputStream inputStream = context.openFileInput(FILE_NAME);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                int index = 0;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    camposConexao[index] = receiveString;
                    index++;
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return camposConexao;
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
    }

    private void deleteFile(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        file.delete();
    }

    @Override
    public void onBackPressed() {
        if (isConectado) {
            Intent intent = new Intent(ConfiguracaoActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            buildAlertaConfirmacao("É necessário fazer a conexão. Deseja conectar agora?");
        }
    }

    private void buildAlerta(String mensagem) {
        AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracaoActivity.this);

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

    private void buildAlertaConfirmacao(String mensagem) {
        AlertDialog.Builder alerta = new AlertDialog.Builder(ConfiguracaoActivity.this);

        alerta.setMessage(mensagem);
        alerta.setCancelable(true);

        alerta.setPositiveButton(
                "SIM",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        alerta.setNegativeButton(
                "NÃO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(ConfiguracaoActivity.this, MainActivity.class);
                        startActivity(intent);
                        dialog.cancel();
                        finish();
                    }
                });

        AlertDialog alert11 = alerta.create();
        alert11.show();
    }

}
