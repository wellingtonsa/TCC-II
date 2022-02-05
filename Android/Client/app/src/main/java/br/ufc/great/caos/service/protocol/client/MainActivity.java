package br.ufc.great.caos.service.protocol.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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
        ImageView Image = (ImageView) findViewById(R.id.image);

        Image.setBackgroundColor(Color.WHITE);
        Image.setImageResource(R.drawable.paradise_extralarge);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.protocols, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol.setAdapter(adapter);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        System.loadLibrary("quiche_jni");



        String serverIP = "192.168.0.53";
        //serverIP = new DiscoveryServer().execute().get();
            if(!serverIP.isEmpty()){


                spinnerProtocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                        switch (adapterView.getItemAtPosition(i).toString()){
                            case "MQTT":
                                clientProtocol = new MQTT();
                                if(client != null) client.disconnect();
                                client = new Client(clientProtocol).execute(serverIP, "8045").get();
                                Image.setImageResource(R.drawable.paradise_extralarge);
                                break;
                            case "TCP":
                                clientProtocol = new TCP();
                                if(client != null) client.disconnect();
                                client = new Client(clientProtocol).execute(serverIP, "8046").get();
                                Image.setImageResource(R.drawable.paradise_extralarge);
                                break;
                            case "HTTP":
                                clientProtocol = new HTTP();
                                if(client != null) client.disconnect();
                                client = new Client(clientProtocol).execute(serverIP, "8047").get();
                                Image.setImageResource(R.drawable.paradise_extralarge);
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
                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.paradise_extralarge);
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                        byte[] byteArray = byteStream.toByteArray();
                        String baseString = Base64.encodeToString(byteArray,Base64.DEFAULT);


                        String encodedImage = client.sendMessage(baseString);
                        byte[] decodedImageByteArray = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.length);

                        Image.setImageBitmap(decodedImage);
                    }
                });

            }

    }
    public Bitmap convertImage(Bitmap original){
        Bitmap converted = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        int A, R, G, B;
        int colorPixel;
        int width = original.getWidth();
        int height = original.getHeight();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                colorPixel = original.getPixel(x, y);
                A = Color.alpha(colorPixel);
                R = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                R = (R + G + B)  / 3;
                G = R;
                B = R;

                converted.setPixel(x, y, Color.argb(A,R,G,B));
            }
        }


        return converted;
    }
}