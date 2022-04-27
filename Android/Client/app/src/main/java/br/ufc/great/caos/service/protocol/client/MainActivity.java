package br.ufc.great.caos.service.protocol.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import br.ufc.great.caos.service.protocol.client.model.entity.Client;
import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.client.util.injection.MetadataExtractor;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.HTTP;
import br.ufc.great.caos.service.protocol.client.util.protocol.application.MQTT;
import br.ufc.great.caos.service.protocol.client.util.protocol.transport.TCP;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

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
        Image.setImageResource(R.drawable.paradise_8mb);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.protocols, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol.setAdapter(adapter);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        System.loadLibrary("quiche_jni");

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
           Log.i("Permission", "Permission granted!");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            Log.i("Permission", "Permission invoked. Showing permission popup.");
        }



        String serverIP = "192.168.1.4";

        MetadataExtractor extractor = new MetadataExtractor(MainActivity.this, getApplicationContext().getPackageName(), getPackageManager());
        extractor.extract();

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
                                    Image.setImageResource(R.drawable.paradise_05mb);
                                    break;
                                case "TCP":
                                    clientProtocol = new TCP();
                                    if(client != null) client.disconnect();
                                    client = new Client(clientProtocol).execute(serverIP, "8046").get();
                                    Image.setImageResource(R.drawable.paradise_05mb);
                                    break;
                                case "HTTP":
                                    clientProtocol = new HTTP();
                                    if(client != null) client.disconnect();
                                    client = new Client(clientProtocol).execute(serverIP, "8047").get();
                                    Image.setImageResource(R.drawable.paradise_05mb);
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
                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.paradise_05mb);
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                        byte[] byteArray = byteStream.toByteArray();
                        String baseString = Base64.encodeToString(byteArray,Base64.DEFAULT);

                        Object[] params = new Object[1];

                        params[0] = baseString;

                        InvocableMethod invocableMethod = new InvocableMethod("br.ufc.great.caos.service.protocol.client.util.image", "BlackAndWhite",
                                "Effects", (String) getPackageManager().getApplicationLabel(getApplicationContext().getApplicationInfo()),  params);

                        String encodedImage = (String) client.executeOffload(invocableMethod);
                        byte[] decodedImageByteArray = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.length);

                        Image.setImageBitmap(decodedImage);
                    }
                });

            }

    }
}