package br.ufc.great.caos.service.protocol.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.concurrent.ExecutionException;

import br.ufc.great.caos.service.protocol.client.model.entity.Client;
import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.client.util.network.DiscoveryServer;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.HTTP;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.MQTT;
import br.ufc.great.caos.service.protocol.client.util.protocol.transport.QUIC;
import br.ufc.great.caos.service.protocol.client.util.protocol.transport.TCP;

public class MainActivity extends AppCompatActivity {

    ProtocolService clientProtocol = null;
    Client client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinnerProtocol = (Spinner) findViewById(R.id.s_protocols);
        Button btnSend = (Button) findViewById(R.id.btn_enviar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.protocols, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol.setAdapter(adapter);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        System.loadLibrary("quiche_jni");



        String serverIP = "192.168.1.5";
        //serverIP = new DiscoveryServer().execute().get();
            if(!serverIP.isEmpty()){


                spinnerProtocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                        switch (adapterView.getItemAtPosition(i).toString()){
                            case "MQTT":
                                clientProtocol = new MQTT();
                                client = new Client(clientProtocol).execute(serverIP, "8045").get();
                                break;
                            case "TCP":
                                clientProtocol = new TCP();
                                client = new Client(clientProtocol).execute(serverIP, "8046").get();
                                break;
                            case "HTTP":
                                clientProtocol = new HTTP();
                                client = new Client(clientProtocol).execute(serverIP, "8047").get();
                                break;
                            default:
                                break;
                        }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        client.sendMessage("Teste");
                    }
                });
/*

                //MQTT
                clientProtocol = new MQTT();
                client = new Client(clientProtocol).execute(serverIP, "8045").get();

                client.sendMessage("Teste");



                //TCP
                clientProtocol = new TCP();
                client = new Client(clientProtocol).execute(serverIP, "8046").get();

                client.sendMessage("Teste");



                //HTTP
                clientProtocol = new HTTP();
                client = new Client(clientProtocol).execute(serverIP, "8047").get();

                client.sendMessage("Teste");

*/

                //client = new Client(clientProtocolMQTT).execute(serverIP, "8048").get();
                //Log.i(clientProtocolMQTT.isInstanceOf(), client.sendMessage("Teste"));


            }


    }
}