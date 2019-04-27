package com.example.awdevice;

import android.icu.text.IDNA;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private Button botaoRecuperar;
    private TextView textoResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        textoResultado = findViewById(R.id.textResultado);
        textoResultado.setVisibility(View.INVISIBLE);
        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTask task = new MyTask();
                //String urlApi = "https://blockchain.info/ticker";
                String urlApi = "http://192.168.43.130:8080/ArduinoWeb/desligar";
                //String cep = "01310100";
                //String urlCep = "https://viacep.com.br/ws/" + cep + "/json/";
                task.execute(urlApi);
            }
        });



    }

    class MyTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

//            String stringUrl = strings[0];
//            InputStream inputStream = null;
//            InputStreamReader inputStreamReader = null;
//            StringBuffer buffer = new StringBuffer();
//
//            try {
//
//                URL url = new URL(stringUrl);
//                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
//
//
//                inputStream = conexao.getInputStream();
//
//                inputStreamReader = new InputStreamReader( inputStream );
//
//                BufferedReader reader = new BufferedReader( inputStreamReader );
//                buffer = new StringBuffer();
//                String linha = "";
//
//                while((linha = reader.readLine()) != null){
//                    buffer.append( linha );
//                    Log.i("I",linha);
//                }
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return  buffer.toString();
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");//tipo de dados de envio
            RequestBody body = RequestBody.create(mediaType, "{\n\t\"teste\":\"envioTesteAndroid\"\n}");//body de envio
            Request request = new Request.Builder()
                    .url("http://192.168.43.130:8080/ArduinoWeb/status?token=teste")
                    .post(body)//tipo de envio
                    .addHeader("Content-Type", "application/json")//tipo de retorno
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                ResponseBody responseBody = client.newCall(request).execute().body();//pega body do retorno
                String retorno = responseBody.string();
                responseBody.close();
                Log.i("informacao_retorno",retorno);
                return retorno;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String resultado) {
            super.onPostExecute(resultado);
            if (resultado!=null) {
                try {


                    JSONObject jsonObject = new JSONObject(resultado);
                    String objetoValor = jsonObject.getString("retorno");
                    botaoRecuperar.setText(objetoValor);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }




        }
    }

}
