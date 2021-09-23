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

        ProtocolService sp = new QUIC();

        new Server(sp).execute(Utils.getIPAddress(true), "8045");

    }
}