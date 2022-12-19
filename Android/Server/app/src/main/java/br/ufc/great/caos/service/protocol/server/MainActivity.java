package br.ufc.great.caos.service.protocol.server;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import br.ufc.great.caos.service.protocol.server.model.entity.Server;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.server.util.Utils;
import br.ufc.great.caos.service.protocol.server.util.injection.MetadataReceiver;
import br.ufc.great.caos.service.protocol.server.util.network.DiscoveryThread;
import br.ufc.great.caos.service.protocol.server.util.protocol.application.HTTP;
import br.ufc.great.caos.service.protocol.server.util.protocol.application.MQTT;
import br.ufc.great.caos.service.protocol.server.util.protocol.network.IPv4;
import br.ufc.great.caos.service.protocol.server.util.protocol.transport.RUDP;
import br.ufc.great.caos.service.protocol.server.util.protocol.transport.TCP;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission granted!");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            Log.i("Permission", "Permission invoked. Showing permission popup.");
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission granted!");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            Log.i("Permission", "Permission invoked. Showing permission popup.");
        }


        ProtocolService serviceProtocolMQTT = new MQTT();
        ProtocolService serviceProtocolTCP = new TCP();
        ProtocolService serviceProtocolHTTP = new HTTP();
        ProtocolService serviceProtocolRUDP = new RUDP();
        ProtocolService serviceProtocolIPv4 = new IPv4();

        String IP = Utils.getIPAddress(true);

        new DiscoveryThread().run();;

        new MetadataReceiver().start();

        new Server(serviceProtocolMQTT, getApplicationContext()).execute(IP, "8045");;
        new Server(serviceProtocolTCP, getApplicationContext()).execute(IP, "8046");
        new Server(serviceProtocolHTTP, getApplicationContext()).execute(IP, "8047");
        new Server(serviceProtocolRUDP, getApplicationContext()).execute(IP, "8048");
        new Server(serviceProtocolIPv4, getApplicationContext()).execute(IP, "8049");

    }
}