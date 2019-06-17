package com.example.awdevice;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Conexao extends AsyncTask<String, Void, String>{

    public interface AsyncResponse {
        void processFinishConexao(String output);
    }

    public AsyncResponse delegate = null;

    public Conexao(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {

        String stringUrl = strings[0];
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        StringBuffer buffer = null;
        boolean isNulo = false;

        try {

            URL url = new URL(stringUrl);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

            // Recupera os dados em Bytes
            if(conexao.getInputStream() != null){
                inputStream = conexao.getInputStream();
            }else{
                return "NULO";
            }

            //inputStreamReader lê os dados em Bytes e decodifica para caracteres
            inputStreamReader = new InputStreamReader(inputStream);

            //Objeto utilizado para leitura dos caracteres do InpuStreamReader
            BufferedReader reader = new BufferedReader(inputStreamReader);
            buffer = new StringBuffer();
            String linha = "";

            while ((linha = reader.readLine()) != null) {
                buffer.append(linha);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            isNulo = true;
            e.printStackTrace();
        }

        if (isNulo) {
            return "NULO";
        } else {
            return buffer.toString();
        }

    }

    @Override
    protected void onPostExecute(String resultado) {
        super.onPostExecute(resultado);

        if (resultado != null) {
            delegate.processFinishConexao(resultado);

        }
    }
}
