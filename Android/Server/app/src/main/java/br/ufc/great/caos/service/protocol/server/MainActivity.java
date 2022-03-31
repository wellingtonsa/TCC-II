package br.ufc.great.caos.service.protocol.server;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import br.ufc.great.caos.service.protocol.server.model.entity.Server;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.server.util.Utils;
import br.ufc.great.caos.service.protocol.server.util.network.DiscoveryThread;
import br.ufc.great.caos.service.protocol.server.util.protocol.application.HTTP;
import br.ufc.great.caos.service.protocol.server.util.protocol.application.MQTT;
import br.ufc.great.caos.service.protocol.server.util.protocol.transport.QUIC;
import br.ufc.great.caos.service.protocol.server.util.protocol.transport.TCP;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        System.loadLibrary("quiche_jni");

        super.onCreate(savedInstanceState);

        ProtocolService serviceProtocolMQTT = new MQTT();
        //ProtocolService serviceProtocolTCP = new TCP();
        ProtocolService serviceProtocolHTTP = new HTTP();
        //ProtocolService serviceProtocolQUIC = new QUIC();

        String IP = Utils.getIPAddress(true);

        //DiscoveryThread dt = new DiscoveryThread();
        //dt.run();

        new Server(serviceProtocolMQTT).execute(IP, "8045");
        //new Server(serviceProtocolTCP).execute(IP, "8046");
        new Server(serviceProtocolHTTP).execute(IP, "8047");
        //new Server(serviceProtocolQUIC).execute(IP, "8048");

    }
}