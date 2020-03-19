package br.com.fatec_pdm_calcula_percurso_e_tempo_com_gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Controle de GPS ativado ou desativado
    private boolean gpsAtivado;
    public boolean getGpsAtivado(){return gpsAtivado;}
    public void setGpsAtivado(boolean a){this.gpsAtivado = a;}
    //Controle de Percurso ativo
    private boolean percursoAtivo;
    public boolean getPercursoAtivo() {return percursoAtivo;}
    public void setPercursoAtivo(boolean percursoAtivo) {this.percursoAtivo = percursoAtivo;}

    //textos
    TextView distanciaPercorridaTextView;

    // botões
    Button concederPermissaoGpsButton;
    Button ativarGpsButton;
    Button desativarGpsButton;
    Button iniciarPercursoButton;
    Button terminarPercursoButton;
    ImageButton buscarButton;
    //TextInputLayout Buscar
    TextInputLayout buscaTextInputLayout;
    TextInputEditText buscarTextInputEditText;
    TextView tempoPercursoTextView;
    TextView distanciaPercursoTextView;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int GPS_REQUEST_CODE = 1001;

    private double latitude;
    private double longitude;
    private float distanciaPercorrida = 0f;

    private Chronometer chronometer;

    Location posicaoInicial = new Location("PosicaoInicial");
    Location posicaoFinal = new Location("PosicaoFinal");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configurarGPS();
        setGpsAtivado(false); //inicializando controle do GPS como desativado
        setPercursoAtivo(false); //inicializando controle do GPS como desativado

        // botões
        concederPermissaoGpsButton = findViewById(R.id.concederPermissaoGpsButton);
        ativarGpsButton = findViewById(R.id.ativarGpsButton);
        desativarGpsButton = findViewById(R.id.desativarGpsButton);
        iniciarPercursoButton = findViewById(R.id.iniciarPercursoButton);
        terminarPercursoButton = findViewById(R.id.terminarPercursoButton);
        buscarButton = findViewById(R.id.buscarButton);
        distanciaPercorridaTextView = findViewById(R.id.distanciaPercorridaTextView);
        buscaTextInputLayout = findViewById(R.id.buscarTextInputLayout);
        buscarTextInputEditText = findViewById(R.id.buscarTextInputEditText);
        tempoPercursoTextView = findViewById(R.id.tempoPercursoTextView);
        distanciaPercursoTextView = findViewById(R.id.distanciaPercursoTextView);
        chronometer = findViewById(R.id.cronometro);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                distanciaPercorrida = posicaoInicial.distanceTo(posicaoFinal);
                distanciaPercorridaTextView.setText(String.format("%.2f", distanciaPercorrida));
            }
        });

        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            // Se permissão concedida, muda texto do botão para verde ativo
            concederPermissaoGpsButton.setTextColor(getColor(R.color.colorActiveButton));
        }

//botao conceder permissao GPS
        concederPermissaoGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                concederPermissao();
            }
        });

// botao ativar GPS
        ativarGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getGpsAtivado()){
                    Toast.makeText(MainActivity.this, R.string.gps_ja_ativado, Toast.LENGTH_SHORT).show();
                }else{
                    ativarGPS(); //ativa GPS
                }
            }
        });

// botao desativar GPS
        desativarGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getGpsAtivado()){
                    desativarGPS();
                }else{
                    Toast.makeText(MainActivity.this, R.string.gps_nao_ativado, Toast.LENGTH_SHORT).show();
                }
            }
        });

// botao iniciar percurso
        iniciarPercursoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getGpsAtivado()){
                    if(getPercursoAtivo()){
                        Toast.makeText(MainActivity.this, R.string.percurso_em_andamento, Toast.LENGTH_SHORT).show();
                    }else{
                        iniciarPercurso();
                        setPercursoAtivo(true);
                    }
                }else{
                    Toast.makeText(MainActivity.this, R.string.preciso_gps_inicioPercurso, Toast.LENGTH_SHORT).show();
                }
            }
        });

// botao terminar percurso
        terminarPercursoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPercursoAtivo()){
                    terminarPercurso();
                    setPercursoAtivo(false);
                }else{
                    Toast.makeText(MainActivity.this, R.string.percurso_sem_andamento, Toast.LENGTH_SHORT).show();
                }
            }
        });

// botao buscar
        buscarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getGpsAtivado()){
                    Uri uri = Uri.parse(
                            String.format(
                                    Locale.getDefault(),
                                    "geo:%f,%f?q=%s",
                                    latitude,
                                    longitude,
                                    buscarTextInputEditText.getText()// adiciona na query o que foi digitado
                            )
                    );
                    Intent intent = new Intent (
                            Intent.ACTION_VIEW,
                            uri
                    );
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, R.string.gps_nao_ativado, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //METODOS
    // inicializa o GPS
    private void configurarGPS() {
        locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                if(percursoAtivo) {
                    posicaoFinal.setLatitude(location.getLatitude());
                    posicaoFinal.setLongitude(location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}

        };
    }

    private void concederPermissao(){
        //checando se permissão já está concedida
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){ // Se permissao já concedida
            Toast.makeText(MainActivity.this, R.string.permissao_ja_concedida, Toast.LENGTH_LONG).show();
            concederPermissaoGpsButton.setTextColor(getColor(R.color.colorActiveButton));
        }else{
            ActivityCompat.requestPermissions( // se não concedida, pede permissão
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == GPS_REQUEST_CODE){
            if(grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    );
                    // Se permissão concedida, muda texto do botão para verde  ativo
                    concederPermissaoGpsButton.setTextColor(getColor(R.color.colorActiveButton));
                    Toast.makeText(MainActivity.this, R.string.permissao_concedida, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, getString(R.string.no_gps_no_app),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void ativarGPS(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, //tempo minimo em milisegundos para atualizar a app
                    0, //distancia em metro
                    locationListener
            );
            setGpsAtivado(true);
            ativarGpsButton.setTextColor(getColor(R.color.colorActiveButton)); // Texto botão GPS fica verde
            Toast.makeText(MainActivity.this, R.string.gps_ativado_sucesso, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.precisa_permissao, Toast.LENGTH_SHORT).show();
        }

    }

    private void desativarGPS(){
        locationManager.removeUpdates(locationListener);
        Toast.makeText(this, R.string.gps_desativado_sucesso, Toast.LENGTH_SHORT).show();
        setGpsAtivado(false);
        ativarGpsButton.setTextColor(getColor(R.color.colorInactiveButton)); // Texto botão GPS fica branco
    }

    private void iniciarPercurso(){
        Toast.makeText(MainActivity.this, R.string.percurso_iniciado, Toast.LENGTH_SHORT).show();
        //Cronometro
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        // Posição ao acionar botao iniciar percurso;
        posicaoInicial.setLatitude(latitude);
        posicaoInicial.setLongitude(longitude);
        // botão Iniciar percurso fica com Texto verde ( ativado )
        iniciarPercursoButton.setTextColor(getColor(R.color.colorActiveButton));
    }

    @SuppressLint("DefaultLocale")
    private void terminarPercurso(){
        chronometer.stop();
        tempoPercursoTextView.setText(chronometer.getText());
        // botão Iniciar percurso fica com Texto branco ( desativado )
        iniciarPercursoButton.setTextColor(getColor(R.color.colorInactiveButton));

        distanciaPercursoTextView.setText(String.format("%.2f",posicaoInicial.distanceTo(posicaoFinal)));

        Toast.makeText(MainActivity.this,
                getString(R.string.distanciaTotal) + " " +
                        String.format("%.2f", posicaoInicial.distanceTo(posicaoFinal))+ " " +
                        getString(R.string.metros) + getString(R.string.tempoTotal) + " " + chronometer.getText(), Toast.LENGTH_LONG).show();

        chronometer.setBase(SystemClock.elapsedRealtime()); // zerar cronometro
        distanciaPercorridaTextView.setText(R.string.hintDistanciaPercorrida); // zera campo distancia


    }
}
