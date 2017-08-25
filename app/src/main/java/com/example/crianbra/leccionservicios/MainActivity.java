package com.example.crianbra.leccionservicios;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener{

    Button button;
    TextView info, x, y, z;
    EditText txtid;

    private long last_update = 0, last_movement = 0;
    private float prevX = 0, prevY = 0, prevZ = 0;
    private float curX = 0, curY = 0, curZ = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        txtid = (EditText)findViewById(R.id.txt_id);
        info = (TextView) findViewById(R.id.textview);
        x = (TextView)findViewById(R.id.x);
        y = (TextView)findViewById(R.id.y);
        z = (TextView)findViewById(R.id.z);
        button = (Button)findViewById(R.id.button);


        button.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        Thread thread = new Thread(){
            @Override
            public void run() {
                final String resultado = enviarDatosGET(txtid.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int r = obtDatosJSON(resultado);
                        if(r > 0){

                            Bundle bundle = new Bundle();
                            bundle.putString("numero", resultado);
                            /*Intent i = new Intent(getApplicationContext(),Main2Activity.class);
                            i.putExtra("cod",txtid.getText().toString());
                            startActivity(i);*/
                        }else{
                            Toast.makeText(getApplicationContext(), "Usuario Incorrecto",Toast.LENGTH_LONG).show();
                            //Log.d("creando","entrando");
                        }
                    }
                });
            }
        };
        thread.start();

    }

    public String enviarDatosGET(String usu){

        URL url= null;
        String linea="";
        int respuesta = 0;
        StringBuilder result = null;

        try{

            url= new URL("http://jsonplaceholder.typicode.com/posts"+usu);
            HttpURLConnection conection=(HttpURLConnection)url.openConnection();
            respuesta = conection.getResponseCode(); //codigo de la respuesta, un numero 200 si es qhay respuesta

            result = new StringBuilder();   //si hay respuesta tomamos o estamos consumiendo el json de la respuesta

            Toast.makeText(getApplicationContext(), "Hola", Toast.LENGTH_SHORT).show();
            if(respuesta == HttpURLConnection.HTTP_OK){  //

                Toast.makeText(getApplicationContext(), "se ha conectado", Toast.LENGTH_SHORT).show();
                //InputStream in = new BufferedInputStream(conection.getInputStream()); //trae la respuesta
                //BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //el BufferedReader se encarga de leer la respuesta

                //while((linea = reader.readLine())!= null){   //linea que trae como respuesta, esta guardada en resutl
                  //  result.append(linea); //lineas que retornan
                //}

            }else {

                Toast.makeText(getApplicationContext(), "no se ha conectado", Toast.LENGTH_SHORT).show();
            }


        }catch (Exception e){}

        return result.toString();   //retorna el json del archivo php de nuestro servicio


    }

    public int obtDatosJSON(String response){
        int res = 0;

        try{

            JSONArray json= new JSONArray(response);
            if(json.length()>0){
                res=1;
                Log.d("creando","entrando");
            }
        }catch (Exception e){}

        return res;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            long current_time = event.timestamp;

            curX = event.values[0];
            curY = event.values[1];
            curZ = event.values[2];

            if (prevX == 0 && prevY == 0 && prevZ == 0) {
                last_update = current_time;
                last_movement = current_time;
                prevX = curX;
                prevY = curY;
                prevZ = curZ;
            }

            long time_difference = current_time - last_update;
            if (time_difference > 0) {
                float movement = Math.abs((curX + curY + curZ) - (prevX - prevY - prevZ)) / time_difference;
                int limit = 1500;
                float min_movement = 1E-6f;
                if (movement > min_movement) {
                    if (current_time - last_movement >= limit) {
                        Toast.makeText(getApplicationContext(), "Hay movimiento de " + movement, Toast.LENGTH_SHORT).show();
                    }
                    last_movement = current_time;
                }
                prevX = curX;
                prevY = curY;
                prevZ = curZ;
                last_update = current_time;
            }


            ((TextView) findViewById(R.id.x)).setText("Acelerómetro X: " + curX);
            ((TextView) findViewById(R.id.y)).setText("Acelerómetro Y: " + curY);
            ((TextView) findViewById(R.id.z)).setText("Acelerómetro Z: " + curZ);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
        super.onStop();
    }
}
