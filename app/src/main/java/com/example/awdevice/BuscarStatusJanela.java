package com.example.awdevice;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BuscarStatusJanela extends AsyncTask<String, Void, String> {

    HttpURLConnection conexao;

    public interface AsyncResponse {
        void processFinishBuscarStatus(String output);
    }

    public AsyncResponse delegate = null;

    public BuscarStatusJanela(AsyncResponse delegate) {
        this.delegate = delegate;
    }


    @Override
    protected String doInBackground(String... strings) {
        String stringUrl = strings[0];
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        StringBuffer buffer = new StringBuffer();

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
    }

    @Override
    protected void onPostExecute(String resultado) {
        super.onPostExecute(resultado);

        if (resultado != null) {
            delegate.processFinishBuscarStatus(resultado);
        }

    }


}
