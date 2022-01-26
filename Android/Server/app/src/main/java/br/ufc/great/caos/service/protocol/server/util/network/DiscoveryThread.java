package br.ufc.great.caos.service.protocol.server.util.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs discovery of cloudlet to device
 */
public class DiscoveryThread implements Runnable {
    DatagramSocket mSocket;

    @Override
    public void run() {
        try {

            mSocket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
            mSocket.setBroadcast(true);

            while (true) {
                Log.i("Discovery",getClass().getName() + ">>>Ready to receive broadcast packets!");

                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                mSocket.receive(packet);


                Log.i("Discovery",getClass().getName() + ">>>Discovery packet received from: "
                        + packet.getAddress().getHostAddress());
                Log.i("Discovery",getClass().getName() + ">>>Packet received; data: "
                        + new String(packet.getData()));

                String message = new String(packet.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                    byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                    DatagramPacket sendPacket =
                            new DatagramPacket(sendData, sendData.length, packet.getAddress(),
                                    packet.getPort());
                    mSocket.send(sendPacket);

                    Log.i("Discovery",getClass().getName() + ">>>Sent packet to: "
                            + sendPacket.getAddress().getHostAddress());
                    mSocket.close();
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static DiscoveryThread getInstance() {
        return DiscoveryThreadHolder.INSTANCE;
    }

    private static class DiscoveryThreadHolder {

        private static final DiscoveryThread INSTANCE = new DiscoveryThread();
    }
}
