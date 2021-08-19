package br.ufc.great.caos.service.protocol.client.util.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DiscoveryServer extends AsyncTask<Void, Void, String>{
    private  String discovery() {
        try {
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 8888);
                c.send(sendPacket);
            } catch (Exception e) {
            }

            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData,
                                sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) { }
                }
            }
           Log.i("Discovery", DiscoveryServer.class.getName() + ">>> Done looping over all network "
                    + "interfaces. Now waiting for a reply!");
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);
            //We have a response
            Log.i("Discovery",DiscoveryServer.class.getName() + ">>> Broadcast response from server: " +
                    receivePacket.getAddress().getHostAddress());
            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                return receivePacket.getAddress().getHostAddress();
            }

            //Close the port!
            c.close();
        } catch (IOException ex) {
            Log.i("Discovery","Discovery Cloudlet error");
        }

        return null;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return discovery();
    }
}
