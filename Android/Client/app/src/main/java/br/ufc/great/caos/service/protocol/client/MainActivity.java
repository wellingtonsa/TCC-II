package br.ufc.great.caos.service.protocol.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.util.concurrent.ExecutionException;

import br.ufc.great.caos.service.protocol.client.model.entity.Client;
import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.client.util.network.DiscoveryServer;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.HTTP;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.MQTT;
import br.ufc.great.caos.service.protocol.client.util.protocol.transport.QUIC;
import br.ufc.great.caos.service.protocol.client.util.protocol.transport.TCP;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        System.loadLibrary("quiche_jni");

        super.onCreate(savedInstanceState);

        ProtocolService clientProtocolMQTT = new MQTT();
        ProtocolService clientProtocolTCP = new TCP();
        ProtocolService clientProtocolHTTP = new HTTP();
        //ProtocolService clientProtocolQUIC = new QUIC();

        String serverIP;
        try {
            serverIP = new DiscoveryServer().execute().get();
            if(!serverIP.isEmpty()){

                Client client = null;


                client = new Client(clientProtocolMQTT).execute(serverIP, "8045").get();
                Log.i(clientProtocolMQTT.isInstanceOf(), client.sendMessage("Teste"));

                client = new Client(clientProtocolTCP).execute(serverIP, "8046").get();
                Log.i(clientProtocolTCP.isInstanceOf(), client.sendMessage("Teste"));

                client = new Client(clientProtocolHTTP).execute(serverIP, "8047").get();
                Log.i(clientProtocolHTTP.isInstanceOf(), client.sendMessage("Teste"));

                //client = new Client(clientProtocolMQTT).execute(serverIP, "8048").get();
                //Log.i(clientProtocolMQTT.isInstanceOf(), client.sendMessage("Teste"));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}