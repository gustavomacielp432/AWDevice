package com.example.awdevice;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class AlterarStatusJanela extends AsyncTask<String, Void, String> {

    HttpURLConnection conexao;

    public interface AsyncResponse {
        void processFinishAlterarStatus(String output);
    }

    public AsyncResponse delegate = null;

    public AlterarStatusJanela(AsyncResponse delegate) {
        this.delegate = delegate;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String stringUrl = strings[0];
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;

        try {

            URL url = new URL(stringUrl);
            conexao = (HttpURLConnection) url.openConnection();


            inputStream = conexao.getInputStream();

            inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader reader = new BufferedReader(inputStreamReader);
            buffer = new StringBuffer();
            String linha = "";

            while ((linha = reader.readLine()) != null) {
                buffer.append(linha);
                Log.i("I", linha);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conexao.disconnect();
        }

        return buffer.toString();

        /*

        if(statusJanela.equals("0")){
            novoStatusJanela = "1";
        }else if(novoStatusJanela.equals("1")){
            novoStatusJanela = "0";
        }else{
            // ******* SE O STATUS DA JANELA ESTIVER ERRADO
            novoStatusJanela = "ERRO";
        }



        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");//tipo de dados de envio
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"status\":\"" + novoStatusJanela + "\"\n}");//body de envio
        Request request = new Request.Builder()
                .url(stringUrl)
                .post(body)//tipo de envio
                .addHeader("Content-Type", "application/json")//tipo de retorno
                .addHeader("cache-control", "no-cache")
                .build();
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();//pega body do retorno
            String retorno = responseBody.string();
            responseBody.close();
            Log.i("informacao_retorno", retorno);
            return retorno;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

        */
    }

    @Override
    protected void onPostExecute(String resultado) {
        super.onPostExecute(resultado);

        if (resultado != null) {
            delegate.processFinishAlterarStatus(resultado);
            Log.i("********", resultado);

        }

    }

}
