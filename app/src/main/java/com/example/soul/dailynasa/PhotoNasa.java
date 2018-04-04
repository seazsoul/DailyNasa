package com.example.soul.dailynasa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soul.dailynasa.Network.GetPicApi;
import com.example.soul.dailynasa.Network.NasaData;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.AsyncTask;

import java.io.IOException;

public class PhotoNasa extends AppCompatActivity {

    private static String dia;
    private static final String key = "AG0bdbRJFcygFGWDfL6BK6Ju3PzNV8Z5ms8kzGJf";
    private ImageView im_apod;
    private TextView load;
    private ProgressBar progressBar;
    Integer counter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_nasa);


        //sacar la fecha seleccionada
        TextView date = findViewById(R.id.title);

        Intent i2 = getIntent();
        Bundle extras = i2.getExtras();

        //preguntar si el extra viene vacío => buena practica
        if(extras != null){
            String fecha = extras.getString("FECHA");
            String aux = date.getText() + " " + fecha;
            date.setText(aux);
            dia = fecha;
        }

        //final de sacar la fecha seleccionada
        im_apod = findViewById(R.id.nasa_image);
        load = findViewById(R.id.loading);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(7);
        progressBar.setProgress(0);
        new Peticion(this).execute();
    }

    //TODO: https://www.youtube.com/watch?v=s99e62gnva8 mirar link. Hi ha Asynctask + Retrofit

    private class Peticion extends AsyncTask<Void,Integer,String> {

        private Context contex;
        public Peticion(Context context) {
            contex = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            publishProgress(counter++);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GetPicApi.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            publishProgress(counter++);
            GetPicApi client = retrofit.create(GetPicApi.class);
            publishProgress(counter++);
            Call<NasaData> call = client.crida(key, dia);
            publishProgress(counter++);
            try {
                publishProgress(counter++);
                NasaData s = call.execute().body();
                Log.d("PhotoNasa.Activity", s.getUrl());
                publishProgress(counter++);
                return s.getUrl();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            //TODO: Implementar el loading. mira a: https://android-arsenal.com/details/1/6802
            //networkCall().compose(RxLoading.<>create(loadingLayout)).subscribe(...);
            progressBar.setProgress(values[0]);
        }


        @Override
        protected void onPostExecute(String message){
            progressBar.setVisibility(View.GONE);
            Picasso.with(contex).load(message).error(R.drawable.nasa).into(im_apod);
            load.setVisibility(View.GONE);
        }
    }

}
