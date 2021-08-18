package br.ufc.great.caos.service.protocol.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.HTTP;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProtocolService ps = new HTTP();

        if(ps.init()) {
            if(ps.connect("192.168.0.3", 8045)){
                ps.sendMessage("TEST QUIC TO SEE WHY ITS BREAKING ALL THE TIME");
            }
        }
    }
}