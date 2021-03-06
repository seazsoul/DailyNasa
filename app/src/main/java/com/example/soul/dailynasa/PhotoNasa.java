package com.example.soul.dailynasa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.soul.dailynasa.Network.GetPicApi;
import com.example.soul.dailynasa.Network.NasaData;
import com.example.soul.dailynasa.R;
import com.squareup.picasso.Picasso;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.os.AsyncTask;
import java.io.IOException;
import static android.net.Uri.*;

public class PhotoNasa extends AppCompatActivity {

    private static final String TAG = "PhotoNasa.Activity";
    private static String dia;
    private static final String key = "AG0bdbRJFcygFGWDfL6BK6Ju3PzNV8Z5ms8kzGJf";
    private ImageView im_apod;
    private TextView load;
    private ProgressBar progressBar;
    private Typeface roboto;
    Integer counter = 1;
    TextView title;
    TextView explanation;

    Button botonyoutube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_nasa);

        //declarar fuentes
        String fuenteRobo = "fuentes/Roboto-BoldCondensedItalic.ttf";
        this.roboto = Typeface.createFromAsset(getAssets(), fuenteRobo);
        botonyoutube = (Button) findViewById(R.id.botonyoutube);
        //para hacerlo invisible si el resultado es una imagen y visible si es un video
        botonyoutube.setVisibility(View.GONE);

        title = findViewById(R.id.title);
        title.setTypeface(roboto);

        explanation = findViewById(R.id.explanation);
        //sacar la fecha seleccionada


        Intent i2 = getIntent();
        Bundle extras = i2.getExtras();

        //preguntar si el extra viene vacío => buena practica
        if(extras != null){
            dia = extras.getString("FECHA");
        }

        //final de sacar la fecha seleccionada

        //introducir la fecha en el 'titulo' de la activity
        String titulo2 = "Imatge del dia " + dia;
        this.setTitle(titulo2);

        im_apod = findViewById(R.id.nasa_image);
        load = findViewById(R.id.loading);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(7);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        new Peticion(this).execute();
    }

    private class Peticion extends AsyncTask<Void,Integer,String[]> {

        private Context contex;
        public Peticion(Context context) {
            contex = context;
        }


        /**
         * Fem la crida amb Retrofit a la API i inicialitzem el progress bar
         * @param voids
         * @return
         */
        @Override
        protected String[] doInBackground(Void... voids) {
            publishProgress(counter);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GetPicApi.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GetPicApi client = retrofit.create(GetPicApi.class);
            Call<NasaData> call = client.crida(key, dia);
            try {
                NasaData s = call.execute().body();
                Log.d("PhotoNasa.Activity", s.getUrl());
                String[] result = new String[4];
                result[0] = s.getTitle();
                result[1] = s.getExplanation();
                result[2] = s.getMedia_type();
                result[3] = s.getUrl();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * per imprimir el progress bar mentre s'executa la tasca en background
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values){
            progressBar.setProgress(values[0]);
        }


        /**
         * Un cop tenim la url de la imatge o video, mirem que es i actuem (mostrant si es imatge, i obrint youtube si no)
         *
         * @param message
         */
        @Override
        protected void onPostExecute(String[] message){
            Log.d(TAG, "onPostExecute entrant");

            title.setText(message[0]);
            explanation.setText(message[1]);



            switch(message[2]) {
                case "image":
                    Log.d(TAG, "onPostExecute es una imatge");
                    Picasso.with(contex).load(message[3]).error(R.drawable.nasa).into(im_apod);
                    progressBar.setVisibility(View.GONE);
                    load.setVisibility(View.GONE);
                    explanation.setVisibility(View.VISIBLE);

                    break;

                case "video":

                    Log.d(TAG, "onPostExecute es un video");

                    progressBar.setVisibility(View.GONE);
                    load.setVisibility(View.GONE);
                    explanation.setVisibility(View.VISIBLE);
                    botonyoutube.setVisibility(View.VISIBLE);

                    final String aux;

                    String comprovacio = message[3].substring(0,4);

                    Log.d(TAG, "***" + comprovacio + "***");

                    if(comprovacio.equals("http")) {
                        Log.d(TAG, "diu que te el http ja");
                        aux = message[3];
                        Log.wtf(TAG, aux);
                    }
                    else{
                        Log.d(TAG, "no te http, l'afegeixo");
                        aux = "https://" + message[3];
                        Log.wtf(TAG, aux);
                    }


                    botonyoutube.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = parse(aux);
                            Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);

                            startActivity(intent3);
                        }
                    });
            }
        }
    }
}
