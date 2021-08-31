package br.ufc.great.caos.service.protocol.server.util.protocol.transport;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import io.quiche4j.Connection;
import io.quiche4j.ConnectionFailureException;
import io.quiche4j.PacketHeader;
import io.quiche4j.Quiche;
import io.quiche4j.Utils;
import io.quiche4j.http3.Http3;


public class QUIC implements ProtocolService {

	static final int MAX_DATAGRAM_SIZE = 1350;
	final byte[] buf = new byte[65535];
	final byte[] out = new byte[MAX_DATAGRAM_SIZE];

	Config config;
	Connection conn;

	DatagramSocket socket;
	byte[] connIdSeed;
	AtomicBoolean running;


	@Override
	public boolean init() {
		try {
			config = new ConfigBuilder(Quiche.PROTOCOL_VERSION)
					.withApplicationProtos(Http3.APPLICATION_PROTOCOL)
					.withVerifyPeer(false)
					.loadCertChainFromPemFile(Utils.copyFileFromJAR("certs", "/cert.crt"))
					.loadPrivKeyFromPemFile(Utils.copyFileFromJAR("certs", "/cert.key"))
					.withMaxIdleTimeout(5_000)
					.withMaxUdpPayloadSize(MAX_DATAGRAM_SIZE)
					.withInitialMaxData(10_000_000)
					.withInitialMaxStreamDataBidiLocal(1_000_000)
					.withInitialMaxStreamDataBidiRemote(1_000_000)
					.withInitialMaxStreamDataUni(1_000_000)
					.withInitialMaxStreamsBidi(100)
					.withInitialMaxStreamsUni(100)
					.withDisableActiveMigration(true)
					.enableEarlyData()
					.build();
			connIdSeed = Quiche.newConnectionIdSeed();
			running = new AtomicBoolean(true);
			return true;
		}catch (Exception e){
			Log.i("QUIC", "Initialization Error (Exception): "+e.getMessage());
			return false;
		}
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			socket = new DatagramSocket(port, InetAddress.getByName(ip));
			Log.i("QUIC", "Server started on port " + port);
			while (running.get()) {
				while (true) {
					final DatagramPacket packet = new DatagramPacket(buf, buf.length);
					final int offset = packet.getOffset();
					final int len = packet.getLength();
					final byte[] packetBuf = Arrays.copyOfRange(packet.getData(), offset, len);
					final PacketHeader hdr;
					try {
						hdr = PacketHeader.parse(packetBuf, Quiche.MAX_CONN_ID_LEN);
					} catch (Exception e) {
						Log.i("QUIC", "Connection Error (Exception): "+e.getMessage());
						continue;
					}


					final byte[] connId = Quiche.signConnectionId(connIdSeed, hdr.destinationConnectionId());
					conn = Quiche.accept(connId, hdr.destinationConnectionId(), config);

					Log.i("QUIC", new String(packetBuf));
					try {
						socket.receive(packet);
						int read = conn.recv(packetBuf);
						if(read <= 0) break;
					} catch (SocketTimeoutException e) {
						Log.i("QUIC", "Connection Error (SocketTimeout): "+e.getMessage());
						conn.onTimeout();
						break;
					} catch (IOException e) {
						Log.i("QUIC", "Connection Error (IOException): "+e.getMessage());
						e.printStackTrace();
					}

					try {
						int l = conn.send(buf);
						Log.i("QUIC", l+"");
						if (l <= 0) break;
						DatagramPacket response = new DatagramPacket("HELLO".getBytes(), l, InetAddress.getByName(ip), port);
						socket.send(response);

					} catch (SocketTimeoutException e) {
						Log.i("QUIC", "Connection Error (SocketTimeout): "+e.getMessage());
						break;
					} catch (IOException e) {
						Log.i("QUIC", "Connection Error (IOException): "+e.getMessage());
						e.printStackTrace();
					}
				}

		}
		return true;
	} catch (UnknownHostException e) {
			Log.i("QUIC", "Connection Error (UnknownHost): "+e.getMessage());
			e.printStackTrace();
		} catch (SocketException e) {
			Log.i("QUIC", "Connection Error (Socket): "+e.getMessage());
			e.printStackTrace();
		} catch (ConnectionFailureException e) {
			Log.i("QUIC", "Connection Error (Socket): "+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
		@Override
	public boolean disconnect() {
		return false;
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}
}

